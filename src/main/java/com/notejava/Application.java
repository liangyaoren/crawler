package com.notejava;

import com.notejava.bilibili.Bilibili;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(Application.class, args);
        //Bmw.test();
        Bilibili.test();
    }
}
