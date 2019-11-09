package example.micronaut.controller.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Data class of request body at author resource updating
 *
 * @author Yudai Tokunaga
 */
public class AuthorUpdateCommand {

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    public AuthorUpdateCommand() {
    }

    public AuthorUpdateCommand(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}