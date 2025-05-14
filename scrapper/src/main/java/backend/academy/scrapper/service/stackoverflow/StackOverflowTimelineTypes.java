package backend.academy.scrapper.service.stackoverflow;

import java.util.Arrays;

public enum StackOverflowTimelineTypes {
    COMMENT("comment"),
    ANSWER("answer"),
    NONE("none");

    public final String type;

    StackOverflowTimelineTypes(String type) {
        this.type = type;
    }

    public static StackOverflowTimelineTypes getEnum(String type) {
        var enumEvent = Arrays.stream(values()).filter(e -> e.type.equals(type)).findFirst();
        return enumEvent.orElse(NONE);
    }
}
