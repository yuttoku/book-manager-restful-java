package example.micronaut.controller;

import example.micronaut.controller.request.AuthorSaveCommand;
import example.micronaut.controller.request.AuthorUpdateCommand;
import example.micronaut.domain.AuthorRepository;
import example.micronaut.domain.entity.Author;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

/**
 * Controller of Author
 *
 * @author Yudai Tokunaga
 */
@Validated
@Controller("/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    /**
     * 登録済みの著者を返却します
     *
     * @return 登録済みの著者
     */
    @Get("/{id}")
    public Author show(Long id) {
        return authorRepository
                .findById(id)
                .orElse(null);
    }

    /**
     * 登録済みの全ての著者を返却します
     *
     * @return 登録済みの著者リスト
     */
    @Get("/")
    public List<Author> list() {
        return authorRepository.findAll();
    }

    /**
     * 登録済みの著者のうち、名前にキーワードを含む著者を返却します
     *
     * @return 登録済みの著者リスト
     */
    @Get("/search")
    public List<Author> search(@QueryValue(value = "keyword") @NotNull String keyword) {
        return authorRepository.findByKeyword(keyword);
    }


    /**
     * 著者を登録します
     * 登録できた場合はOK、できない場合はCONFLICTを返却します
     *
     * @return OK、またはCONFLICT
     */
    @Post("/")
    public HttpResponse<Author> save(@Body @Valid AuthorSaveCommand cmd) {
        try {
            Author author = authorRepository.save(cmd.getName());
            return HttpResponse.created(author).headers(headers -> headers.location(location(author.getId())));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return HttpResponse.status(HttpStatus.CONFLICT);
        }
    }

    /**
     * 著者を削除します
     * 削除が成功しても失敗してもNO_CONTENTを返却します
     *
     * @return NO_CONTENT
     */
    @Delete("/{id}")
    public HttpResponse delete(Long id) {
        authorRepository.deleteById(id);
        return HttpResponse.noContent();
    }

    /**
     * 著者を更新します
     * 更新が成功したらNO_CONTENT、失敗したらCONFLICTを返却します
     *
     * @return NO_CONTENT
     */
    @Put("/")
    public HttpResponse update(@Body @Valid AuthorUpdateCommand command) {
        try {
            authorRepository.update(command.getId(), command.getName());
        } catch (Exception e) {
            return HttpResponse.status(HttpStatus.CONFLICT);
        }
        return HttpResponse.noContent().header(HttpHeaders.LOCATION, location(command.getId()).getPath());
    }

    private URI location(Long id) {
        return URI.create("/authors/" + id);
    }
}