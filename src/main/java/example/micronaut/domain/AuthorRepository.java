package example.micronaut.domain;

import example.micronaut.domain.entity.Author;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Interface of author repository
 *
 * @author Yudai Tokunaga
 */
public interface AuthorRepository {

    List<Author> findAll();

    Optional<Author> findById(@NotNull Long id);

    List<Author> findByKeyword(@NotNull String keyword);

    Author save(@NotBlank String name);

    void deleteById(@NotNull Long id);

    int update(@NotNull Long id, @NotBlank String name);
}