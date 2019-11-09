package example.micronaut.controller;

import example.micronaut.controller.request.BookSaveCommand;
import example.micronaut.controller.request.BookUpdateCommand;
import example.micronaut.domain.AuthorRepository;
import example.micronaut.domain.BookRepository;
import example.micronaut.domain.entity.Author;
import example.micronaut.domain.entity.Book;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Controller of Book
 *
 * @author Yudai Tokunaga
 */
@Validated
@Controller("/books")
public class BookController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookController(BookRepository bookRepository,
                          AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    /**
     * 登録済みの書籍を返却します
     *
     * @return 登録済みの書籍
     */
    @Get("/{id}")
    public Book show(Long id) {
        return bookRepository
                .findById(id)
                .orElse(null);
    }

    /**
     * 登録済みの書籍を返却します
     *
     * @return 登録済みの書籍リスト
     */
    @Get("/")
    public List<Book> list() {
        return bookRepository.findAll();
    }

    /**
     * 登録済みの書籍のうち、名前にキーワードを含む書籍を返却します
     *
     * @return 登録済みの書籍リスト
     */
    @Get("/search")
    public List<Book> search(@QueryValue(value = "keyword") @NotNull String keyword) {
        return bookRepository.findByKeyword(keyword);
    }

    /**
     * 書籍を登録します
     * 登録できた場合はOKを返却します
     * 著者が登録済みでない場合はBAD_REQUEST、
     * それ以外の理由で登録できない場合はCONFLICTを返却します
     *
     * @return OK、またはCONFLICT
     */
    @Post("/")
    public HttpResponse<Book> save(@Body @Valid BookSaveCommand cmd) {

        // 著者は存在するか?
        Optional<Author> author = authorRepository.findById(cmd.getAuthorId());
        if (!author.isPresent()) {
            return HttpResponse.status(HttpStatus.BAD_REQUEST);
        }

        // 書籍を登録する
        try {
            Book book = bookRepository.save(cmd.getIsbn(), cmd.getTitle(), author.get());
            return HttpResponse.created(book).headers(headers -> headers.location(location(book.getId())));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return HttpResponse.status(HttpStatus.CONFLICT);
        }
    }

    /**
     * 書籍を削除します
     * 削除が成功しても失敗してもNO_CONTENTを返却します
     *
     * @return NO_CONTENT
     */
    @Delete("/{id}")
    public HttpResponse delete(Long id) {
        bookRepository.deleteById(id);
        return HttpResponse.noContent();
    }

    /**
     * 書籍を更新します
     * 更新が成功しても失敗してもNO_CONTENTを返却します
     *
     * @return NO_CONTENT
     */
    @Put("/")
    public HttpResponse update(@Body @Valid BookUpdateCommand cmd) {

        try {
            // 現在登録されている書籍(現在パラメータ)を取得する
            Optional<Book> book = bookRepository.findById(cmd.getId());

            // 現在パラメータと更新パラメータをマージしたマージパラメータを作成
            String margeIsbn = cmd.getIsbn() == null ? book.get().getIsbn() : cmd.getIsbn();
            String margeTitle = cmd.getTitle() == null ? book.get().getTitle() : cmd.getTitle();
            Long margeAuthorId = cmd.getAuthorId() == null ? book.get().getAuthor().getId() : authorRepository.findById(cmd.getAuthorId()).get().getId();

            // 書籍を登録する
            bookRepository.update(cmd.getId(), margeIsbn, margeTitle, margeAuthorId);
        } catch (Exception e) {
            return HttpResponse.status(HttpStatus.CONFLICT);
        }
        return HttpResponse.noContent().header(HttpHeaders.LOCATION, location(cmd.getId()).getPath());
    }

    private URI location(Long id) {
        return URI.create("/books/" + id);
    }
}
