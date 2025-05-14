package backend.academy.scrapper.schemas.responses.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StackOverflowResponse(
        List<Item> items,
        @JsonProperty("has_more") boolean hasMore,
        @JsonProperty("quota_max") int quotaMax,
        @JsonProperty("quota_remaining") int quotaRemaining) {}
