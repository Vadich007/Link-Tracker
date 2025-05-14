package backend.academy.bot;

import backend.academy.bot.configs.ApiConfig;
import backend.academy.bot.configs.BotConfig;
import backend.academy.bot.configs.DbConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({BotConfig.class, DbConfig.class, ApiConfig.class})
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
