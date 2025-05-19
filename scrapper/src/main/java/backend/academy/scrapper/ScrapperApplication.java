package backend.academy.scrapper;

import backend.academy.scrapper.configs.ApiConfig;
import backend.academy.scrapper.configs.DbConfig;
import backend.academy.scrapper.configs.KafkaTopicConfig;
import backend.academy.scrapper.configs.ScrapperConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableKafka
@EnableConfigurationProperties({ScrapperConfig.class, ApiConfig.class, DbConfig.class, KafkaTopicConfig.class})
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
