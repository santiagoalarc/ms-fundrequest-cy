package co.com.crediya.api.config;

import co.com.crediya.api.exceptions.ValidateModelException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.http.HttpStatus;

import java.util.Set;

public class BaseValidator {
    private static final Validator validator;

    static {
        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private BaseValidator() {
        throw new UnsupportedOperationException("BaseValidator is a utility class and cannot be instantiated");
    }

    /**
     * Validates a DTO object using the constraints defined in its annotations.
     *
     * @param object Object to validate.
     * @param <T> Type of the object.
     * @throws RuntimeException if the object has validation violations.
     */
    public static <T> void validate(T object, String message) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            StringBuilder errors = new StringBuilder(message);
            for (ConstraintViolation<T> violation : violations) {
                errors.append("\n- ").append(violation.getPropertyPath()).append(": ").append(violation.getMessage());
            }
            throw new ValidateModelException(errors.toString(), HttpStatus.BAD_REQUEST);
        }
    }
}