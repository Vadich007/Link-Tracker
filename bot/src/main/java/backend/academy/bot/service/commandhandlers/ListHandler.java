package backend.academy.bot.service.commandhandlers;

import backend.academy.bot.schemas.models.Link;
import backend.academy.bot.schemas.responses.ApiErrorResponse;
import backend.academy.bot.service.BotCommands;
import backend.academy.bot.service.scrapper.ScrapperService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@AllArgsConstructor
@Component
public class ListHandler implements CommandHandler {
    private ScrapperService scrapperService;

    @Override
    public String command() {
        return BotCommands.LIST.name();
    }

    @Override
    public String handle(Long chatId, String message) {
        List<Link> links;
        try {
            links = scrapperService.listLink(chatId);
        } catch (HttpClientErrorException e) {
            ApiErrorResponse response = e.getResponseBodyAs(ApiErrorResponse.class);
            if (response != null
                && (response.exceptionMessage().equals("Отсутствует чат с таким id")
                || response.code().equals(HttpStatus.BAD_REQUEST.toString()))) {
                return "Отсутствует чат с таким id.";
            } else {
                return "Произошла ошибка. Повторите попытку позже.";
            }
        } catch (HttpServerErrorException e) {
            return "Ошибка со стороны сервера. Повторите отправку позже.";
        }

        if (links.isEmpty()) {
            return "Список отслеживаемых ссылок пуст :(";
        } else {
            StringBuilder messageBuilder = new StringBuilder();
            long number = 1;
            for (Link link : links) {
                messageBuilder.append(tagProcessing(link, number)).append(filterProcessing(link));
                number++;
            }
            return messageBuilder.toString();
        }
    }

    private String tagProcessing(Link link, long number) {
        if (link.tags() == null || link.tags().isEmpty()) {
            return number + ". " + link.url() + "\nТеги: нет\n";
        } else {
            return number + ". " + link.url() + "\nТеги: " + String.join(" ", link.tags()) + "\n";
        }
    }

    private String filterProcessing(Link link) {
        if (link.filters() == null || link.filters().isEmpty()) {
            return "Фильтры: нет\n";
        } else {
            return "Фильтры: " + String.join(" ", link.filters()) + "\n";
        }
    }
}
