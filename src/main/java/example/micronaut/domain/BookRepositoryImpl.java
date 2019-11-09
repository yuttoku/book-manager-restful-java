package example.micronaut.domain;

import example.micronaut.domain.entity.Author;
import example.micronaut.domain.entity.Book;
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of book repository interface
 *
 * @author Yudai Tokunaga
 */
public class BookRepositoryImpl implements BookRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public BookRepositoryImpl(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return entityManager
                .createQuery("select book from Book book", Book.class)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findById(@NotNull Long id) {
        return Optional.ofNullable(entityManager.find(Book.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByKeyword(@NotNull String keyword) {
        return entityManager
                .createQuery("select book from Book as book where book.title like :keyword", Book.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    @Override
    @Transactional
    public Book save(@NotNull String isbn, @NotNull String title, @NotNull Author author) {
        Book book = new Book(isbn, title, author);
        entityManager.persist(book);
        return book;
    }

    @Override
    @Transactional
    public void deleteById(@NotNull Long id) {
        findById(id).ifPresent(book -> entityManager.remove(book));
    }

    @Override
    @Transactional
    public int update(@NotNull Long id, String isbn, String title, Long authorId) {
        Optional<Book> book = findById(id);
        return entityManager
                .createQuery("update Book book set book.isbn = :isbn, book.title = :title, book.author.id = :authorId where book.id = :id")
                .setParameter("isbn", isbn)
                .setParameter("title", title)
                .setParameter("authorId", authorId)
                .setParameter("id", id)
                .executeUpdate();
    }
}
