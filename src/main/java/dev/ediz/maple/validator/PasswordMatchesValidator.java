package dev.ediz.maple.validator;

import dev.ediz.maple.model.Account;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator
        implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){
        Account account = (Account) obj;
        return account.getPassword().equals(account.getConfirmPassword());
    }
}
