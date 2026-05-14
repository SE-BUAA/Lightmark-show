package top.ortus.timemark.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiConnectionProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {
    OpenAiChatAutoConfiguration.class
})
@EnableConfigurationProperties()
public class BackendApplication {

    @PostConstruct
    public void init() {
        System.out.println("BackendApplication init");
        System.out.println("Test whatever you can");
        System.out.println("BackendApplication init end");
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}