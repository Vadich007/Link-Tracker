package backend.academy.scrapper.service.bot;

import java.util.List;

public interface BotService {
    void sendUpdate(String url, List<Long> ids, String message);
}
