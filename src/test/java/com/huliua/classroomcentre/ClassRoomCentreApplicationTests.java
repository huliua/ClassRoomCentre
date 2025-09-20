package com.huliua.classroomcentre;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClassRoomCentreApplicationTests {


    @Test
    void testEncrypt() {
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        /*配置文件中配置如下的算法*/
        standardPBEStringEncryptor.setAlgorithm("PBEWithMD5AndDES");
        /*配置文件中配置的password*/
        standardPBEStringEncryptor.setPassword("huliua");
        //加密
        String jasyptPasswordEN = standardPBEStringEncryptor.encrypt("123456");
        //解密
        String jasyptPasswordDE = standardPBEStringEncryptor.decrypt(jasyptPasswordEN);
        System.out.println("加密后密码：" + jasyptPasswordEN);
        System.out.println("解密后密码：" + jasyptPasswordDE);
    }
}
