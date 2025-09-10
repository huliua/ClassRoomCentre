package com.huliua;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.huliua.classroomcentre.mapper")
public class ClassRoomCentreApplication implements ApplicationRunner, CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ClassRoomCentreApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("调用ApplicationRunner.run");
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("调用CommandLineRunner.run");
    }
}
