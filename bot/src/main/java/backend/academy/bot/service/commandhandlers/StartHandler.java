package backend.academy.bot.service.commandhandlers;

import backend.academy.bot.repository.UserRepository;
import backend.academy.bot.schemas.responses.ApiErrorResponse;
import backend.academy.bot.service.BotCommands;
import backend.academy.bot.service.scrapper.ScrapperService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Component
@AllArgsConstructor
public class StartHandler implements CommandHandler {
    private ScrapperService scrapperService;
    private UserRepository userRepository;

    @Override
    public String command() {
        return BotCommands.START.name();
    }

    @Override
    public String handle(Long chatId, String message) {
        try {
            scrapperService.registrationChat(chatId);
            userRepository.addUser(chatId);
            return "Вы успешно зарегистрированы.";
        } catch (HttpClientErrorException e) {
            ApiErrorResponse response = e.getResponseBodyAs(ApiErrorResponse.class);
            if (response != null
                && (response.exceptionMessage().equals("Чат с таким id уже зарегистрирован")
                || response.code().equals(HttpStatus.BAD_REQUEST.toString()))) {
                return "Вы уже зарегистрированы.";
            } else {
                return "Произошла ошибка. Повторите попытку позже.";
            }
        } catch (DataIntegrityViolationException e) {
            return "Вы уже зарегистрированы.";
        } catch (ResourceAccessException e) {
            return "Сервис недоступен. Повторите попытку позже.";
        } catch (HttpServerErrorException e) {
            return "Ошибка со стороны сервера. Повторите отправку позже.";
        }
    }
}
