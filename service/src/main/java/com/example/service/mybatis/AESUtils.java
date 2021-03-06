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
            LOG.error("?????????????????????-static block", e);
            throw new RuntimeException(errorString, e);
        }
        if (newMaxKeyLength < 256) {
            LOG.error("????????????????????? < 256");

            throw new RuntimeException(errorString); // hack failed
        }
    }

    /**
     * ????????????
     */
    private static final String ALGORITHM = "AES";
    /**
     * ???????????????/????????????/????????????
     */
    private static final String ALGORITHM_MODE_PADDING = "AES/ECB/PKCS7Padding";
    /**
     * ??????key
     * key ???????????????????????????????????????????????????????????????????????????????????????
     * ??????????????????????????????????????????????????????????????? key ???????????????????????????????????? key ???????????????
     */
    private static String ek = "asff23dgsdfsf34234dsfsdfsdfs5324324324324safafaf";
    private static SecretKeySpec secretKeySpec = new SecretKeySpec(MD5Util.MD5Encode(ek, "UTF-8").toLowerCase().getBytes(), ALGORITHM);
    private static ThreadLocal<Cipher> encryptionCipher = ThreadLocal.withInitial(() -> {
        Cipher encryptionCipher = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            // ???????????????
            encryptionCipher = Cipher.getInstance(ALGORITHM_MODE_PADDING, "BC");
            // ?????????
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        } catch (Throwable e) {
            LOG.error("????????????????????? encryptionCipher", e);
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
            LOG.error("????????????????????? decryptionCipher", e);
        }
        return decryptionCipher;
    });

    /**
     * AES??????
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
     * AES??????
     * <p>
     * ???1???????????????A???base64????????????????????????B
     * ???2??????key*????????????B???AES-256-ECB?????????PKCS7Padding???
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
            LOG.error("????????????", "source", base64Data, e);
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