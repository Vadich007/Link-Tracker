package backend.academy.scrapper.service.stackoverflow;

import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.schemas.responses.stackoverflow.Item;
import backend.academy.scrapper.schemas.responses.stackoverflow.StackOverflowResponse;
import backend.academy.scrapper.service.TrackedService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StackOverflowService implements TrackedService<Item> {
    private final LinkRepository linkRepository;
    private final StackOverflowClient stackOverflowClient;

    @Override
    public String getServicePrefix() {
        return "https://stackoverflow.com/";
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @SuppressWarnings("StringSplitter")
    public boolean hasUpdate(String url) {
        String id = url.split("/")[4];

        ResponseEntity<StackOverflowResponse> response = stackOverflowClient.sendTimelineRequest(id);

        if (response != null
                && response.getBody() != null
                && response.getBody().items() != null
                && !response.getBody().items().isEmpty()
                && response.getBody().items().getFirst().postId() != null) {

            boolean result = !linkRepository.isLastEvent(
                    url, response.getBody().items().getFirst().postId());

            if (result)
                linkRepository.setLastEvent(url, response.getBody().items().getFirst());

            return result;
        } else {
            return false;
        }
    }

    @Override
    @SuppressFBWarnings(value = {"VA_FORMAT_STRING_USES_NEWLINE", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
    @SuppressWarnings("StringSplitter")
    public String getLastUpdateMessage(String url, Item lastItem) {
        String label = url.split("/")[5].replace('-', ' ');

        String message;
        ResponseEntity<StackOverflowResponse> response =
                stackOverflowClient.sendPostsRequest(String.valueOf(lastItem.postId()));

        String preview = "";
        if (response != null
                && response.hasBody()
                && response.getBody() != null
                && response.getBody().items() != null
                && !response.getBody().items().isEmpty()
                && !response.getBody().items().getFirst().body().isEmpty()) {

            String text = response.getBody()
                    .items()
                    .getFirst()
                    .body()
                    .replaceAll("<p>", "")
                    .replaceAll("<code>", "")
                    .replaceAll("</p>", "")
                    .replaceAll("</code>", "");

            preview = text.length() < 200 ? text : text.substring(0, 199);
        }

        switch (StackOverflowTimelineTypes.getEnum(lastItem.timelineType())) {
            case StackOverflowTimelineTypes.ANSWER:
                message = String.format(
                        """
                        Новый ответ
                        %s
                        %s
                        %s
                        %s
                        %s""",
                        label.substring(0, 1).toUpperCase(Locale.ROOT) + label.substring(1),
                        lastItem.owner().displayName(),
                        lastItem.creationDate(),
                        preview,
                        url);
                break;
            case StackOverflowTimelineTypes.COMMENT:
                message = String.format(
                        """
                        Новый комментарий
                        %s
                        %s
                        %s
                        %s
                        %s""",
                        label.substring(0, 1).toUpperCase(Locale.ROOT) + label.substring(1),
                        lastItem.owner().displayName(),
                        lastItem.creationDate(),
                        preview,
                        url);
                break;
            default:
                message = String.format("""
                    Новое событие
                    %s""", url);
                break;
        }
        return message;
    }
}
