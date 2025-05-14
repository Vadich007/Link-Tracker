package backend.academy.bot.db;

import backend.academy.bot.schemas.requests.AddLinkRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AddLinkRequestConverter implements AttributeConverter<AddLinkRequest, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(AddLinkRequest attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert AddLinkRequest to JSON", e);
        }
    }

    @Override
    public AddLinkRequest convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, AddLinkRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to AddLinkRequest", e);
        }
    }
}
