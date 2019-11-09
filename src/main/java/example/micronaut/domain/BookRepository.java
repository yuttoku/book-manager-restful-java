package example.micronaut.domain;

import example.micronaut.domain.entity.Author;
import example.micronaut.domain.entity.Book;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Interface of book repository
 *
 * @author Yudai Tokunaga
 */
public interface BookRepository {

    List<Book> findAll();

    Optional<Book> findById(@NotNull Long id);

    List<Book> findByKeyword(@NotNull String keyword);

    Book save(@NotNull String isbn, @NotNull String title, @NotNull Author author);

    void deleteById(@NotNull Long id);

    int update(@NotNull Long id, String isbn, String title, Long authorId);
}
