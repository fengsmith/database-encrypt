package com.example.service.mybatis;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import java.util.Map;

public class AESUtils {
    private static final Logger LOG = LoggerFactory.getLogger("AESUtils");

    static {
        String errorString = "Failed manually overriding key-length permissions.";
        int newMaxKeyLength;
        try {
            if ((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
                Class c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
                Constructor con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissionCollection = con.newInstance();
                Field f = c.getDeclaredField("all_allowed");
                f.setAccessible(true);
                f.setBoolean(allPermissionCollection, true);

                c = Class.forName("javax.crypto.CryptoPermissions");
                con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissions = con.newInstance();
                f = c.getDeclaredField("perms");
                f.setAccessible(true);
                ((Map) f.get(allPermissions)).put("*", allPermissionCollection);

                c = Class.forName("javax.crypto.JceSecurityManager");
                f = c.getDeclaredField("defaultPolicy");
                f.setAccessible(true);
                Field mf = Field.class.getDeclaredField("modifiers");
                mf.setAccessible(true);
                mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                f.set(null, allPermissions);

                newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
            }
        } catch (Throwable e) {
            LOG.error("加密初始化异常-static block", e);
            throw new RuntimeException(errorString, e);
        }
        if (newMaxKeyLength < 256) {
            LOG.error("加密初始化异常 < 256");

            throw new RuntimeException(errorString); // hack failed
        }
    }

    /**
     * 密钥算法
     */
    private static final String ALGORITHM = "AES";
    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String ALGORITHM_MODE_PADDING = "AES/ECB/PKCS7Padding";
    /**
     * 生成key
     * key 可以修改，但好像有位数长度限制，不能太长了，也不能太短了。
     * 注意，一旦确定之后就不能再修改了。用同一个 key 加密过的数据只能用同样的 key 进行解密。
     */
    private static String ek = "asff23dgsdfsf34234dsfsdfsdfs5324324324324safafaf";
    private static SecretKeySpec secretKeySpec = new SecretKeySpec(MD5Util.MD5Encode(ek, "UTF-8").toLowerCase().getBytes(), ALGORITHM);
    private static ThreadLocal<Cipher> encryptionCipher = ThreadLocal.withInitial(() -> {
        Cipher encryptionCipher = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            // 创建密码器
            encryptionCipher = Cipher.getInstance(ALGORITHM_MODE_PADDING, "BC");
            // 初始化
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        } catch (Throwable e) {
            LOG.error("加密初始化异常 encryptionCipher", e);
        }
        return encryptionCipher;
    });

    private static ThreadLocal<Cipher> decryptionCipher = ThreadLocal.withInitial(() -> {
        Cipher decryptionCipher = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            decryptionCipher = Cipher.getInstance(ALGORITHM_MODE_PADDING, "BC");
            decryptionCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        } catch (Throwable e) {
            LOG.error("解密初始化异常 decryptionCipher", e);
        }
        return decryptionCipher;
    });

    /**
     * AES加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encryptData(String data) throws Exception {
        if (StringUtils.isEmpty(data)) {
            return data;
        }
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = encryptionCipher.get().doFinal(bytes);
        return encode(encryptedBytes);
    }

    /**
     * AES解密
     * <p>
     * （1）对加密串A做base64解码，得到加密串B
     * （2）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）
     *
     * @param base64Data
     * @return
     * @throws Exception
     */
    public static String decryptData(String base64Data) throws Exception {
        if (StringUtils.isEmpty(base64Data)) {
            return base64Data;
        }
        try {
            byte[] decodedBytes = decoder.decode(base64Data);
            byte[] decryptedBytes = decryptionCipher.get().doFinal(decodedBytes);
            String originalContent = new String(decryptedBytes, StandardCharsets.UTF_8);
            return originalContent;
        } catch (Throwable e) {
            LOG.error("解密异常", "source", base64Data, e);
            throw e;
        }
    }
    public static void main(String[] args) throws Exception {
        String s = "hello,world";
        String encryptionData = encryptData(s);
        String decryptionData = decryptData(encryptionData);
        System.out.println("");
    }

    private static String encode(byte[] bytes) {
        String r = encoder.encodeToString(bytes);
        return r;
    }

    private static Base64.Encoder encoder = Base64.getEncoder();
    private static Base64.Decoder decoder = Base64.getDecoder();

}