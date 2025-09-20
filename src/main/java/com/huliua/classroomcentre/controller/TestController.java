package com.huliua.classroomcentre.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Value("${test.encodePassword}")
    private String password;
    @Value("${test.plainPassword}")
    private String plainPassword;

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @GetMapping("/getPassword")
    public String getPassword() {
        return "plainPassword:" + plainPassword + "\t encodePassword:" + password;
    }

}
