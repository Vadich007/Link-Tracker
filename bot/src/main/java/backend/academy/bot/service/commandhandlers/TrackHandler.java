package backend.academy.bot.service.commandhandlers;

import backend.academy.bot.repository.UserRepository;
import backend.academy.bot.schemas.models.UserStates;
import backend.academy.bot.schemas.responses.ApiErrorResponse;
import backend.academy.bot.service.BotCommands;
import backend.academy.bot.service.ScrapperClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Component
@AllArgsConstructor
public class TrackHandler implements CommandHandler {
    private final UserRepository userRepository;
    private final ScrapperClient scrapperClient;

    @Override
    public String command() {
        return BotCommands.TRACK.name();
    }

    @Override
    public String handle(Long chatId, String message) {
        final String rejectionLine = "нет";
        switch (userRepository.getUser(chatId).state()) {
            case UserStates.FREE:
                userRepository.updateState(chatId, UserStates.WAIT_TRACK_LINK);
                return "Введите ссылку которую хотите отслеживать.";

            case UserStates.WAIT_TRACK_LINK:
                userRepository.updateState(chatId, UserStates.WAIT_TAGS);
                userRepository.addUrl(chatId, message);
                return "Введите через пробел теги, которые ходите присвоить ссылке или '" + rejectionLine
                        + "', если хотите оставить теги пустые.";

            case UserStates.WAIT_TAGS:
                userRepository.updateState(chatId, UserStates.WAIT_FILTERS);
                if (!message.equals(rejectionLine) && !message.isEmpty())
                    userRepository.addTags(chatId, Arrays.asList(message.split(" ")));
                return "Введите через пробел фильтры, которые ходите присвоить ссылке или '" + rejectionLine
                        + "', если хотите оставить фильтры пустые.";

            case WAIT_FILTERS:
                userRepository.updateState(chatId, UserStates.FREE);
                if (!message.equals(rejectionLine) && !message.isEmpty())
                    userRepository.addFilters(chatId, Arrays.asList(message.split(" ")));
                try {
                    scrapperClient.trackLink(chatId, userRepository.getAddLinkRequest(chatId));
                    userRepository.deleteAddLinkRequest(chatId);
                    return "Ссылка добавлена.";
                } catch (HttpClientErrorException e) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    userRepository.deleteAddLinkRequest(chatId);
                    try {
                        ApiErrorResponse apiErrorResponse = objectMapper.readValue(
                                e.getMessage().substring(e.getMessage().indexOf('{')), ApiErrorResponse.class);
                        return apiErrorResponse.description();
                    } catch (JsonProcessingException ex) {
                        return "Произошла ошибка";
                    }
                } catch (HttpServerErrorException e) {
                    return "Ошибка со стороны сервера. Повторите отправку позже.";
                }

            default:
                userRepository.deleteAddLinkRequest(chatId);
                return "";
        }
    }
}
