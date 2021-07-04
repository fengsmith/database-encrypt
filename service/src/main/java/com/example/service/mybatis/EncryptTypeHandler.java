package com.example.service.mybatis;

import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * 注意不能把 BaseTypeHandler 改为 BaseTypeHandler<String> ，如果改为 BaseTypeHandler<String> 的话，
 * 所有 String 类型的字段都会给加密了。改为 BaseTypeHandler 在需要加密的地方加上 typeHandler 就好了
 */
public class EncryptTypeHandler extends BaseTypeHandler {
    @SneakyThrows
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Object o, JdbcType jdbcType) {
        preparedStatement.setString(i, AESUtils.encryptData((String) o));
    }


    /**
     * 用于在Mybatis获取数据结果集时如何把数据库类型转换为对应的Java类型
     *
     * @param rs         当前的结果集
     * @param columnName 当前的字段名称
     * @return 转换后的Java对象
     * @throws SQLException
     */
    @SneakyThrows
    @Override
    public String getNullableResult(ResultSet rs, String columnName) {
        String r = rs.getString(columnName);
        return r == null ? null : AESUtils.decryptData(r);
    }

    /**
     * 用于在Mybatis通过字段位置获取字段数据时把数据库类型转换为对应的Java类型
     *
     * @param rs          当前的结果集
     * @param columnIndex 当前字段的位置
     * @return 转换后的Java对象
     * @throws SQLException
     */
    @SneakyThrows
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) {
        String r = rs.getString(columnIndex);
        return r == null ? null : AESUtils.decryptData(r);
    }

    /**
     * 用于Mybatis在调用存储过程后把数据库类型的数据转换为对应的Java类型
     *
     * @param cs          当前的CallableStatement执行后的CallableStatement
     * @param columnIndex 当前输出参数的位置
     * @return
     * @throws SQLException
     */
    @SneakyThrows
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) {
        String r = cs.getString(columnIndex);
        // 兼容待修复的数据
        return r == null ? null : AESUtils.decryptData(r);
    }

}