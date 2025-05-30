package backend.academy.scrapper.schemas.responses.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Item(
    @JsonProperty("creation_date") Long creationDate,
    @JsonProperty("post_id") Long postId,
    @JsonProperty("timeline_type") String timelineType,
    Owner owner,
    String body) {
}
