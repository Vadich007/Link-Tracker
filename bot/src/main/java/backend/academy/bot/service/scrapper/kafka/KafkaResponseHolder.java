package backend.academy.bot.service.scrapper.kafka;

import backend.academy.bot.schemas.responses.ListLinksResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KafkaResponseHolder {
    private final CountDownLatch latch = new CountDownLatch(1);
    private ListLinksResponse response;

    public void setResponse(ListLinksResponse response) {
        this.response = response;
        latch.countDown();
    }

    public ListLinksResponse awaitResponse(long timeout, TimeUnit unit) {
        try {
            latch.await(timeout, unit);
            return response;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
