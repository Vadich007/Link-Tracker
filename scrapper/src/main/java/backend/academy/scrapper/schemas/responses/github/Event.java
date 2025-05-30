package backend.academy.scrapper.schemas.responses.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Event(String id, String type, Payload payload, @JsonProperty("created_at") String createdAt) {
}
