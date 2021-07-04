1. 数据库字段加密的需求:
有些敏感字段，比如手机号、姓名、身份证、住址、邮箱等需要加密。万一数据库被拖库了，黑客拿到加密的数据也不会造成用户的关键信息泄密。
有些网站要做等保评级，数据库里的关键信息必须要加密。我们的网站就是因为要做等保评级，所以数据库中的敏感数据必须要加密。

2. 加密、解密原理。
orm 框架用的是 mybatis ，在数据插入前对数据进行加密，数据查询出来的时候再解密。底层代码统一做了这些事，上层的业务层不需要关注加密、解密。
   
具体就是利用了 mybatis 的 typeHandler ，在数据更新到数据库前对数据进行加密。在数据查询出来的时候再解密。具体设置细节参见 CustomUserMapper.xml 的相关配置。加密、解密的 handler 是 com.example.service.mybatis.EncryptTypeHandler 。

3. 如果是用 mybatis generator 自动生成相关数据库访问代码的话，参见 database-encrypt/dao/src/main/resources/generatorConfig.xml 。需要加、解密的列需要增加 <columnOverride/> 然后指定 typeHandler 来处理加、解密。
4. 如果是想在自己写的 sql 中进行加密、解密，可以参见 database-encrypt/dao/src/main/resources/mybatis/manual/CustomUserMapper.xml 。主要还是在插入/更新到数据库前对需要加密的字段进行处理，在查询后再对需要解密的字段进行解密。   
5. demo 用例在 com.example.service.UserServiceTest 。
6. 需要数据库的用户名、密码、ip 等。包括 generatorConfig.xml、 application.properties 。
7. 修改加密、解密 key 。在 com.example.service.mybatis.AESUtils 中的第 76 行。   
8. user 建表 sql :

```sql

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nick_name` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `real_name` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `age` int(11) NOT NULL,
  `crate_date` datetime NOT NULL,
  `update_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



```



