package example.micronaut.controller.request;

import javax.validation.constraints.NotNull;

/**
 * Data class of request body at book resource updating
 *
 * @author Yudai Tokunaga
 */
public class BookUpdateCommand {

    @NotNull
    private Long id;

    private String isbn;

    private String title;

    private Long authorId;

    public BookUpdateCommand() {
    }

    public BookUpdateCommand(Long id, String isbn, String title, Long authorId) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.authorId = authorId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
