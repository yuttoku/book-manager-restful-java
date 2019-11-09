package example.micronaut.controller.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Data class of request body at book resource saving
 *
 * @author Yudai Tokunaga
 */
public class BookSaveCommand {

    @NotBlank
    private String isbn;

    @NotBlank
    private String title;

    @NotNull
    private Long authorId;

    public BookSaveCommand() {
    }

    public BookSaveCommand(String isbn, String title, Long authorId) {
        this.isbn = isbn;
        this.title = title;
        this.authorId = authorId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
}
