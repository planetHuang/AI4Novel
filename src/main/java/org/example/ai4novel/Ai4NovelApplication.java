package org.example.ai4novel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.ai4novel.mapper")
public class  Ai4NovelApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ai4NovelApplication.class, args);
    }

}
