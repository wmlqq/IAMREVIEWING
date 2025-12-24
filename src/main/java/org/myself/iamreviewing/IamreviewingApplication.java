package org.myself.iamreviewing;

import javafx.application.Application;
import lombok.Getter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@MapperScan("org.myself.iamreviewing.mapper")
@SpringBootApplication
public class IamreviewingApplication {

    // 提供获取Spring上下文的静态方法
    //保存上下文供FX使用
    @Getter
    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        // 1. 启动Spring Boot容器
        springContext = SpringApplication.run(IamreviewingApplication.class, args);

        // 2. 启动JavaFX应用
        Application.launch(JavaFxApplication.class, args);
    }


}
