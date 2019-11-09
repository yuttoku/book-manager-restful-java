package example.micronaut.domain.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Entity of book table
 *
 * @author Yudai Tokunaga
 */
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "isbn", nullable = false, unique = true)
    private String isbn;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @ManyToOne
    private Author author;

    public Book() {
    }

    public Book(@NotNull String isbn, @NotNull String title, @NotNull Author author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Book{");
        sb.append("id='");
        sb.append(id);
        sb.append(", title='");
        sb.append(title);
        sb.append(", isbn=");
        sb.append(isbn);
        sb.append(", title='");
        sb.append(title);
        sb.append("', author='");
        sb.append(author);
        sb.append("'}");
        return sb.toString();
    }
}