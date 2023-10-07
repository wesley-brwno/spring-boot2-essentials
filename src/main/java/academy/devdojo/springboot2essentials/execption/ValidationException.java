package academy.devdojo.springboot2essentials.execption;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ValidationException extends ExceptionDetails {
    private final String fields;
    private final String fieldsMessage;
}
