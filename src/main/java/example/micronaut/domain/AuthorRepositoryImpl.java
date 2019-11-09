package example.micronaut.domain;

import example.micronaut.domain.entity.Author;
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of author repository interface
 *
 * @author Yudai Tokunaga
 */
@Singleton
public class AuthorRepositoryImpl implements AuthorRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public AuthorRepositoryImpl(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findAll() {
        return entityManager
                .createQuery("select author from Author as author", Author.class)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Author> findById(@NotNull Long id) {
        return Optional.ofNullable(entityManager.find(Author.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findByKeyword(@NotNull String keyword) {
        return entityManager
                .createQuery("select author from Author as author where author.name like :keyword", Author.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    @Override
    @Transactional
    public Author save(@NotBlank String name) {
        Author author = new Author(name);
        entityManager.persist(author);
        return author;
    }

    @Override
    @Transactional
    public void deleteById(@NotNull Long id) {
        findById(id).ifPresent(author -> entityManager.remove(author));
    }

    @Override
    @Transactional
    public int update(@NotNull Long id, @NotBlank String name) {
        return entityManager
                .createQuery("update Author author set author.name = :name where author.id = :id")
                .setParameter("name", name)
                .setParameter("id", id)
                .executeUpdate();
    }
}