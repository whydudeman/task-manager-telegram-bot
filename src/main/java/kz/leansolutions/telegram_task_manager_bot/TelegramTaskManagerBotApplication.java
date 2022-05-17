package kz.leansolutions.telegram_task_manager_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoRepositories
@EnableMongoAuditing
@EnableScheduling
public class TelegramTaskManagerBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramTaskManagerBotApplication.class, args);
    }

}
