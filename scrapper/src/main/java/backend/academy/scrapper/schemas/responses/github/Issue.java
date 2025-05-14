package backend.academy.scrapper.schemas.responses.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Issue(String title, String body, User user) {}
