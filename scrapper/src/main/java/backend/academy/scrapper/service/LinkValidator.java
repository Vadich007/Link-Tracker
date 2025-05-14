package backend.academy.scrapper.service;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LinkValidator implements ConstraintValidator<ValidateLink, String> {
    @Override
    @SuppressWarnings("StringSplitter")
    public boolean isValid(String link, ConstraintValidatorContext constraintValidatorContext) {
        if (link == null) return false;

        String[] splittedLink = link.split("/");

        if (!splittedLink[0].equals("https:") || !splittedLink[1].isEmpty()) return false;

        if (splittedLink[2].equals("github.com") && splittedLink.length == 5) {
            return !splittedLink[3].isEmpty() && !splittedLink[4].isEmpty();
        } else if (splittedLink[2].equals("stackoverflow.com")
                && splittedLink.length == 6
                && splittedLink[3].equals("questions")) {
            return splittedLink[4].chars().allMatch(Character::isDigit) && !splittedLink[5].isEmpty();
        } else {
            return false;
        }
    }
}
