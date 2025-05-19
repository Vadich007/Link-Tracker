package backend.academy.bot;

import backend.academy.bot.configs.ApiConfig;
import backend.academy.bot.configs.BotConfig;
import backend.academy.bot.configs.DbConfig;
import backend.academy.bot.configs.KafkaTopicConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableConfigurationProperties({BotConfig.class, DbConfig.class, ApiConfig.class, KafkaTopicConfig.class})
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
