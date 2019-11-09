package example.micronaut.controller;

import example.micronaut.controller.request.AuthorSaveCommand;
import example.micronaut.controller.request.BookSaveCommand;
import example.micronaut.controller.request.BookUpdateCommand;
import example.micronaut.domain.entity.Author;
import example.micronaut.domain.entity.Book;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * テストケースの前提条件: authorテーブル、bookテーブルにはレコードが存在しないこと
 *
 * @author Yudai Tokunaga
 */
public class BookControllerTest {

    private static EmbeddedServer server;
    private static HttpClient client;

    @BeforeClass
    public static void setupServer() {
        server = ApplicationContext
                .build()
                .run(EmbeddedServer.class);
        client = server.getApplicationContext().createBean(HttpClient.class, server.getURL());
    }

    @AfterClass
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stop();
        }
    }

    @Test
    public void 書籍の新規登録と削除() {

        // 著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long authorId = entityId(response, "authors");
        request = HttpRequest.GET("/authors/" + authorId);
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 著者に紐づく書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("1", "すべてがFになる", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId);
        Book book = client.toBlocking().retrieve(request, Book.class);
        assertEquals("1", book.getIsbn());
        assertEquals("すべてがFになる", book.getTitle());
        assertEquals(authorId, book.getAuthor().getId());

        // 登録した著者を削除
        request = HttpRequest.DELETE("/books/" + bookId);
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());

        request = HttpRequest.GET("/books");
        List books = client.toBlocking().retrieve(request, Argument.of(List.class, Book.class));
        assertEquals(0, books.size());

        // 本ケースで作成したエンティティを削除
        request = HttpRequest.DELETE("/authors/" + authorId);
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    @Test(expected = HttpClientResponseException.class)
    public void 書籍を存在しない著者で登録したら409() {

        // 存在しない著者Idで書籍を登録
        try {
            HttpRequest request = HttpRequest.POST("/books", new BookSaveCommand("1", "すべてがFになる", 999L));
            client.toBlocking().exchange(request);
            fail();
        } catch (HttpClientResponseException e) {
            // ステータス検証
            assertEquals(HttpStatus.BAD_REQUEST, e.getResponse().getStatus());
            throw e;
        }
    }

    @Test(expected = HttpClientResponseException.class)
    public void 書籍の登録で重複したら409() {

        // 著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long authorId = entityId(response, "authors");
        request = HttpRequest.GET("/authors/" + authorId);
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 1件目の書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("1", "すべてがFになる", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId);
        Book book = client.toBlocking().retrieve(request, Book.class);
        assertEquals("1", book.getIsbn());
        assertEquals("すべてがFになる", book.getTitle());
        assertEquals(authorId, book.getAuthor().getId());

        // 1件目の書籍と同じISBNでもう一度登録
        try {
            request = HttpRequest.POST("/books", new BookSaveCommand("1", "黒猫の三角", authorId));
            client.toBlocking().exchange(request);
            fail();
        } catch (HttpClientResponseException e) {
            // ステータス検証
            assertEquals(HttpStatus.CONFLICT, e.getResponse().getStatus());
            // 本ケースで作成したエンティティを削除
            request = HttpRequest.DELETE("/authors/" + authorId);
            response = client.toBlocking().exchange(request);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
            throw e;
        }
    }

    @Test
    public void 書籍の一覧を取得() {

        // 著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long authorId = entityId(response, "authors");
        request = HttpRequest.GET("/authors/" + authorId);
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 1件目の書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("1", "すべてがFになる", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId1 = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId1);
        Book book1 = client.toBlocking().retrieve(request, Book.class);
        assertEquals("1", book1.getIsbn());
        assertEquals("すべてがFになる", book1.getTitle());
        assertEquals(authorId, book1.getAuthor().getId());

        // 2件目の書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("2", "黒猫の三角", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId2 = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId2);
        Book book2 = client.toBlocking().retrieve(request, Book.class);
        assertEquals("2", book2.getIsbn());
        assertEquals("黒猫の三角", book2.getTitle());
        assertEquals(authorId, book2.getAuthor().getId());

        // 3件目の書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("3", "彼女は一人で歩くのか", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId3 = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId3);
        Book book3 = client.toBlocking().retrieve(request, Book.class);
        assertEquals("3", book3.getIsbn());
        assertEquals("彼女は一人で歩くのか", book3.getTitle());
        assertEquals(authorId, book3.getAuthor().getId());

        // 登録した全ての著者を取得
        request = HttpRequest.GET("/books");
        List books = client.toBlocking().retrieve(request, Argument.of(List.class, Book.class));
        assertEquals(3, books.size());

        // 本ケースで作成したエンティティを削除
        request = HttpRequest.DELETE("/authors/" + authorId);
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    @Test
    public void 書籍をキーワードで検索() {

        // 著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long authorId = entityId(response, "authors");
        request = HttpRequest.GET("/authors/" + authorId);
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 1件目の書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("1", "すべてがFになる", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId1 = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId1);
        Book book1 = client.toBlocking().retrieve(request, Book.class);
        assertEquals("1", book1.getIsbn());
        assertEquals("すべてがFになる", book1.getTitle());
        assertEquals(authorId, book1.getAuthor().getId());

        // 2件目の書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("2", "黒猫の三角", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId2 = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId2);
        Book book2 = client.toBlocking().retrieve(request, Book.class);
        assertEquals("2", book2.getIsbn());
        assertEquals("黒猫の三角", book2.getTitle());
        assertEquals(authorId, book2.getAuthor().getId());

        // 3件目の書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("3", "彼女は一人で歩くのか", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId3 = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId3);
        Book book3 = client.toBlocking().retrieve(request, Book.class);
        assertEquals("3", book3.getIsbn());
        assertEquals("彼女は一人で歩くのか", book3.getTitle());
        assertEquals(authorId, book3.getAuthor().getId());

        // タイトルに"の"が含まれる著者を検索する
        String keyword = null;
        keyword = URLEncoder.encode("の", StandardCharsets.UTF_8);
        request = HttpRequest.GET("/books/search?keyword=" + keyword);
        List books = client.toBlocking().retrieve(request, Argument.of(List.class, Book.class));
        assertEquals(2, books.size());

        // 本ケースで作成したエンティティを削除
        request = HttpRequest.DELETE("/authors/" + authorId);
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    @Test
    public void 書籍を更新() {

        // 著者1を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long authorId1 = entityId(response, "authors");
        request = HttpRequest.GET("/authors/" + authorId1);
        Author author1 = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author1.getName());

        // 著者2を登録
        request = HttpRequest.POST("/authors", new AuthorSaveCommand("Hiroshi Mori"));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long authorId2 = entityId(response, "authors");
        request = HttpRequest.GET("/authors/" + authorId2);
        Author author2 = client.toBlocking().retrieve(request, Author.class);
        assertEquals("Hiroshi Mori", author2.getName());

        // 書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("1", "すべてがFになる", authorId1));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId);
        Book before = client.toBlocking().retrieve(request, Book.class);
        assertEquals("1", before.getIsbn());
        assertEquals("すべてがFになる", before.getTitle());
        assertEquals(authorId1, before.getAuthor().getId());

        // 書籍を更新
        request = HttpRequest.PUT("/books", new BookUpdateCommand(bookId, "2", "THE PERFECT INSIDER", authorId2));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());

        request = HttpRequest.GET("/books/" + bookId);
        Book after = client.toBlocking().retrieve(request, Book.class);
        assertEquals("2", after.getIsbn());
        assertEquals("THE PERFECT INSIDER", after.getTitle());
        assertEquals(authorId2, after.getAuthor().getId());

        // 本ケースで作成したエンティティを削除
        request = HttpRequest.DELETE("/authors/" + authorId1);
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
        request = HttpRequest.DELETE("/authors/" + authorId2);
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    @Test(expected = HttpClientResponseException.class)
    public void 書籍の更新が重複したら409() {

        // 著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long authorId = entityId(response, "authors");
        request = HttpRequest.GET("/authors/" + authorId);
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 1件目の書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("1", "すべてがFになる", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId1 = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId1);
        Book book1 = client.toBlocking().retrieve(request, Book.class);
        assertEquals("1", book1.getIsbn());
        assertEquals("すべてがFになる", book1.getTitle());
        assertEquals(authorId, book1.getAuthor().getId());

        // 2件目の書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("2", "黒猫の三角", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId2 = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId2);
        Book book2 = client.toBlocking().retrieve(request, Book.class);
        assertEquals("2", book2.getIsbn());
        assertEquals("黒猫の三角", book2.getTitle());
        assertEquals(authorId, book2.getAuthor().getId());

        // 1件目の書籍を2件目の書籍のISBNに変更
        try {
            request = HttpRequest.PUT("/books", new BookUpdateCommand(bookId1, "2", "すべてがFになる", authorId));
            response = client.toBlocking().exchange(request);
            fail();
        } catch (HttpClientResponseException e) {
            // ステータス検証
            assertEquals(HttpStatus.CONFLICT, e.getResponse().getStatus());
            // 本ケースで作成したエンティティを削除
            request = HttpRequest.DELETE("/authors/" + authorId);
            response = client.toBlocking().exchange(request);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
            throw e;
        }
    }

    @Test(expected = HttpClientResponseException.class)
    public void 書籍を存在しない著者に更新したら409() {

        // 著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long authorId = entityId(response, "authors");
        request = HttpRequest.GET("/authors/" + authorId);
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 書籍を登録
        request = HttpRequest.POST("/books", new BookSaveCommand("1", "すべてがFになる", authorId));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long bookId = entityId(response, "books");
        request = HttpRequest.GET("/books/" + bookId);
        Book book = client.toBlocking().retrieve(request, Book.class);
        assertEquals("1", book.getIsbn());
        assertEquals("すべてがFになる", book.getTitle());
        assertEquals(authorId, book.getAuthor().getId());

        // 存在しない著者で書籍を更新
        try {
            request = HttpRequest.PUT("/books", new BookUpdateCommand(bookId, "2", "THE PERFECT INSIDER", 99L));
            response = client.toBlocking().exchange(request);
            fail();
        } catch (HttpClientResponseException e) {
            // ステータス検証
            assertEquals(HttpStatus.CONFLICT, e.getResponse().getStatus());
            // 本ケースで作成したエンティティを削除
            request = HttpRequest.DELETE("/authors/" + authorId);
            response = client.toBlocking().exchange(request);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
            throw e;
        }
    }

    private Long entityId(HttpResponse response, String entity) {
        String path = "/" + entity + "/";
        String value = response.header(HttpHeaders.LOCATION);
        if (value == null) {
            return null;
        }
        int index = value.indexOf(path);
        if (index != -1) {
            return Long.valueOf(value.substring(index + path.length()));
        }
        return null;
    }
}
