package backend.academy.bot.service.commandhandlers;

import backend.academy.bot.repository.UserRepository;
import backend.academy.bot.schemas.models.UserStates;
import backend.academy.bot.schemas.requests.RemoveLinkRequest;
import backend.academy.bot.service.BotCommands;
import backend.academy.bot.service.ScrapperClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Component
@AllArgsConstructor
public class UntrackHandler implements CommandHandler {
    private UserRepository userRepository;
    private ScrapperClient scrapperClient;

    @Override
    public String command() {
        return BotCommands.UNTRACK.name();
    }

    @Override
    public String handle(Long chatId, String message) {
        switch (userRepository.getUser(chatId).state()) {
            case UserStates.FREE:
                userRepository.updateState(chatId, UserStates.WAIT_UNTRACK_LINK);
                return "Введите ссылку которую хотите перестать отслеживать.";

            case UserStates.WAIT_UNTRACK_LINK:
                userRepository.updateState(chatId, UserStates.FREE);
                try {
                    scrapperClient.untrackLink(chatId, new RemoveLinkRequest(message));
                    return "Ссылка удалена.";
                } catch (HttpClientErrorException e) {
                    return "Некорректные параметры запроса.";
                } catch (HttpServerErrorException e) {
                    return "Ошибка со стороны сервера. Повторите отправку позже.";
                }

            default:
                return "";
        }
    }
}
