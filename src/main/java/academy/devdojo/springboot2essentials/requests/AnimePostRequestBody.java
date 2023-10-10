package academy.devdojo.springboot2essentials.requests;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class AnimePostRequestBody {
    @NotEmpty(message = "The name connot be empty")
    private String name;
}
