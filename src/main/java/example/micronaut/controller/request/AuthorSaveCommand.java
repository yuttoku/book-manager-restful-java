package example.micronaut.controller.request;

import javax.validation.constraints.NotBlank;

/**
 * Data class of request body at author resource saving
 *
 * @author Yudai Tokunaga
 */
public class AuthorSaveCommand {

    @NotBlank
    private String name;

    public AuthorSaveCommand() {
    }

    public AuthorSaveCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}