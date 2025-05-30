package backend.academy.scrapper;

import backend.academy.scrapper.service.LinkValidator;
import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LinkValidatorTests {

    ConstraintValidatorContext context = new ConstraintValidatorContext() {
        @Override
        public void disableDefaultConstraintViolation() {
        }

        @Override
        public String getDefaultConstraintMessageTemplate() {
            return "";
        }

        @Override
        public ClockProvider getClockProvider() {
            return null;
        }

        @Override
        public ConstraintViolationBuilder buildConstraintViolationWithTemplate(String s) {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> aClass) {
            return null;
        }
    };

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://stackoverflow.com/questions/79538960/maui",
            "https://github.com/Vadich007/FractalImageGenerator"
        })
    void validLinkTest(String link) {
        LinkValidator validator = new LinkValidator();

        Assertions.assertTrue(validator.isValid(link, context));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://sta1ckoverflow.com/questions/79538960/maui",
            "https://stackoverflow.com/questions/79538960//",
            "https://stackoverflow.com/questions/7953896a0/maui",
            "ht1tps://stackoverflow.com/questions/79538960/maui",
            "https:/1/stackoverflow.com/questions/79538960/maui",
            "https://stackoverflow.com/question1s/79538960/maui"
        })
    void notValidLinkTest1(String link) {
        LinkValidator validator = new LinkValidator();

        Assertions.assertFalse(validator.isValid(link, context));
    }
}
