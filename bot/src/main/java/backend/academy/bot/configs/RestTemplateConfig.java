package backend.academy.bot.configs;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Autowired
    Environment environment;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        int timeout = Integer.parseInt(environment.getProperty("timeout"));

        builder.connectTimeout(Duration.ofSeconds(timeout));
        builder.readTimeout(Duration.ofSeconds(timeout));

        return builder.build();
    }
}
