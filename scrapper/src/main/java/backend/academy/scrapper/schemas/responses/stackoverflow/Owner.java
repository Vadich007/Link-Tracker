package backend.academy.scrapper.schemas.responses.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Owner(@JsonProperty("display_name") String displayName) {
}
