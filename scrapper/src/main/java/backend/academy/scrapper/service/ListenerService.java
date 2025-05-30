package backend.academy.scrapper.service;

import backend.academy.scrapper.configs.ScrapperConfig;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.service.bot.BotService;
import backend.academy.scrapper.service.github.GitHubService;
import backend.academy.scrapper.service.stackoverflow.StackOverflowService;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ListenerService {
    private BotService botService;
    private GitHubService gitHubService;
    private StackOverflowService stackOverflowService;
    private LinkRepository linkRepository;
    private ScrapperConfig scrapperConfig;

    @Scheduled(fixedRateString = "${app.interval}")
    public void checkUpdate() {
        int linkSize = linkRepository.size();
        int limit = scrapperConfig.limit();
        for (int offset = 0; offset < linkSize; offset += limit) {
            Set<String> links = linkRepository.getUrls(limit, offset);
            for (String url : links) {
                log.info("Links received {}, offset links {}", links.size(), offset);

                if (url.startsWith("https://github.com/") && gitHubService.hasUpdate(url))
                    botService.sendUpdate(
                        url,
                        linkRepository.getChats(url).stream().toList(),
                        gitHubService.getLastUpdateMessage(url, linkRepository.getLastGitHubEvent(url)));
                else if (url.startsWith("https://stackoverflow.com/") && stackOverflowService.hasUpdate(url))
                    botService.sendUpdate(
                        url,
                        linkRepository.getChats(url).stream().toList(),
                        stackOverflowService.getLastUpdateMessage(
                            url, linkRepository.getLastStackOverflowEvent(url)));
            }
        }
    }
}
