package com.sbt.hakaton.task8.config;

import com.sbt.hakaton.task8.Pipe;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackageClasses = Pipe.class)
public class PipeConfig {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(PipeConfig.class, args);
        Pipe pipe = context.getBean(Pipe.class);
        pipe.pipe();

        Thread.sleep(100000);
    }
}
