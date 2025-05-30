package backend.academy.scrapper;

import backend.academy.scrapper.configs.ApiConfig;
import backend.academy.scrapper.service.bot.HttpBotService;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@EnableRetry
public class RetryBotTests {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private HttpBotService httpBotService = new HttpBotService(new RestTemplate(),
        new ApiConfig(
            null,
            null,
            new ApiConfig.BotApi("http://localhost:" + wireMock.getPort() + "/api/v1/updates")
        )
    );

    @Test
    void postSentUpdateTest() {
        String scenario = "postSentUpdateTest";
        String firstState = "Fail";
        String secondState = "OK";
        String url = "/api/v1/updates";

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(firstState));

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(firstState)
            .willReturn(aResponse()
                .withStatus(503))
            .willSetStateTo(secondState));

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(secondState)
            .willReturn(aResponse()
                .withStatus(200)));

        Assertions.assertDoesNotThrow(() -> httpBotService.sendUpdate("url", null, null));

        wireMock.verify(3, postRequestedFor(urlEqualTo(url)));
    }


    @Test
    void postSentUpdateDontRetryTest() {
        String scenario = "postSentUpdateDontRetryTest";
        String url = "/api/v1/updates";

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(404)));

        Assertions.assertThrows(Exception.class, () -> httpBotService.sendUpdate("url", null, null));

        wireMock.verify(1, postRequestedFor(urlEqualTo(url)));

    }
}
