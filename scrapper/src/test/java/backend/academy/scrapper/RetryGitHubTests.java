package backend.academy.scrapper;

import backend.academy.scrapper.configs.ApiConfig;
import backend.academy.scrapper.service.bot.HttpBotService;
import backend.academy.scrapper.service.github.GitHubClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@EnableRetry
public class RetryGitHubTests {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private GitHubClient gitHubClient = new GitHubClient(new RestTemplate(),
        null,
        new ApiConfig(
            new ApiConfig.GitHubApi("https://api.github.com/repos/{owner}/{repos}/events"),
            null,
            null
        )
    );

    @Test
    void sendEventResponse() {
        String scenario = "postSentUpdateTest";
        String firstState = "Fail";
        String secondState = "OK";
        String url = "https://api.github.com/repos/owner/repos/events";

        wireMock.stubFor(post(url)
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

        Assertions.assertDoesNotThrow(() -> gitHubClient.sendEventResponse("owner", "repos"));

        wireMock.verify(3, postRequestedFor(urlEqualTo(url)));
    }


    @Test
    void postSentUpdateDontRetryTest() {
        String scenario = "postSentUpdateDontRetryTest";
        String url = "https://api.github.com/repos/owner/repos/events";

        wireMock.stubFor(post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(404)));

        Assertions.assertThrows(Exception.class, () -> gitHubClient.sendEventResponse("owner", "repos"));

        wireMock.verify(1, postRequestedFor(urlEqualTo(url)));

    }
}
