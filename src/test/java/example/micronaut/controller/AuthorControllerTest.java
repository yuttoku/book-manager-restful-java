package example.micronaut.controller;

import example.micronaut.controller.request.AuthorSaveCommand;
import example.micronaut.controller.request.AuthorUpdateCommand;
import example.micronaut.domain.entity.Author;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * テストケースの前提条件: authorテーブル、bookテーブルにはレコードが存在しないこと
 *
 * @author Yudai Tokunaga
 */
public class AuthorControllerTest {

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
    public void 著者の新規登録と削除() {

        // 著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long firstAuthorId = entityId(response);
        request = HttpRequest.GET("/authors/" + firstAuthorId);
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 本ケースで作成したエンティティを削除
        request = HttpRequest.DELETE("/authors/" + firstAuthorId);
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    @Test(expected = HttpClientResponseException.class)
    public void 著者の登録が重複したら409() {

        // 1件目の著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long firstAuthorId = entityId(response);
        request = HttpRequest.GET("/authors/" + firstAuthorId);
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 1件目の著者名をもう一度登録
        try {
            request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
            client.toBlocking().exchange(request);
            fail();
        } catch (HttpClientResponseException e) {
            // ステータス検証
            assertEquals(HttpStatus.CONFLICT, e.getResponse().getStatus());
            // 本ケースで作成したエンティティを削除
            request = HttpRequest.DELETE("/authors/" + firstAuthorId);
            response = client.toBlocking().exchange(request);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
            throw e;
        }
    }

    @Test
    public void 著者の一覧を取得() {

        List<Long> authorIds = new ArrayList<>();

        // 1件目の著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        authorIds.add(entityId(response));
        request = HttpRequest.GET("/authors/" + authorIds.get(0));
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 2件目の著者を登録
        request = HttpRequest.POST("/authors", new AuthorSaveCommand("森見登美彦"));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        authorIds.add(entityId(response));
        request = HttpRequest.GET("/authors/" + authorIds.get(1));
        author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森見登美彦", author.getName());

        // 3件目の著者を登録
        request = HttpRequest.POST("/authors", new AuthorSaveCommand("夏目漱石"));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        authorIds.add(entityId(response));
        request = HttpRequest.GET("/authors/" + authorIds.get(2));
        author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("夏目漱石", author.getName());

        // 登録した全ての著者を取得
        request = HttpRequest.GET("/authors");
        List authors = client.toBlocking().retrieve(request, Argument.of(List.class, Author.class));
        assertEquals(3, authors.size());

        // 本ケースで作成したエンティティを削除
        for (Long genreId : authorIds) {
            request = HttpRequest.DELETE("/authors/" + genreId);
            response = client.toBlocking().exchange(request);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
        }
    }

    @Test
    public void 著者をキーワードで検索() {

        List<Long> authorIds = new ArrayList<>();

        // 1件目の著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        authorIds.add(entityId(response));
        request = HttpRequest.GET("/authors/" + authorIds.get(0));
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 2件目の著者を登録
        request = HttpRequest.POST("/authors", new AuthorSaveCommand("森見登美彦"));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        authorIds.add(entityId(response));
        request = HttpRequest.GET("/authors/" + authorIds.get(1));
        author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森見登美彦", author.getName());

        // 3件目の著者を登録
        request = HttpRequest.POST("/authors", new AuthorSaveCommand("夏目漱石"));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        authorIds.add(entityId(response));
        request = HttpRequest.GET("/authors/" + authorIds.get(2));
        author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("夏目漱石", author.getName());

        // 名前に"森"が含まれる著者を検索する
        String keyword = null;
        keyword = URLEncoder.encode("森", StandardCharsets.UTF_8);
        request = HttpRequest.GET("/authors/search?keyword=" + keyword);
        List authors = client.toBlocking().retrieve(request, Argument.of(List.class, Author.class));
        assertEquals(2, authors.size());

        // 本ケースで作成したエンティティを削除
        for (Long genreId : authorIds) {
            request = HttpRequest.DELETE("/authors/" + genreId);
            response = client.toBlocking().exchange(request);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
        }
    }

    @Test
    public void 著者を更新() {

        // 著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        Long authorId = entityId(response);
        request = HttpRequest.GET("/authors/" + authorId);
        Author before = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", before.getName());

        // 著者の名前を更新
        request = HttpRequest.PUT("/authors", new AuthorUpdateCommand(authorId, "Hiroshi Mori"));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());

        request = HttpRequest.GET("/authors/" + authorId);
        Author after = client.toBlocking().retrieve(request, Author.class);
        assertEquals("Hiroshi Mori", after.getName());

        // 本ケースで作成したエンティティを削除
        request = HttpRequest.DELETE("/authors/" + authorId);
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }

    @Test(expected = HttpClientResponseException.class)
    public void 著者の更新で重複したら409() {

        List<Long> authorIds = new ArrayList<>();

        // 1件目の著者を登録
        HttpRequest request = HttpRequest.POST("/authors", new AuthorSaveCommand("森博嗣"));
        HttpResponse response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        authorIds.add(entityId(response));
        request = HttpRequest.GET("/authors/" + authorIds.get(0));
        Author author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森博嗣", author.getName());

        // 2件目の著者を登録
        request = HttpRequest.POST("/authors", new AuthorSaveCommand("森見登美彦"));
        response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        authorIds.add(entityId(response));
        request = HttpRequest.GET("/authors/" + authorIds.get(1));
        author = client.toBlocking().retrieve(request, Author.class);
        assertEquals("森見登美彦", author.getName());

        // 1件目の著者名を2件目の著者名に変更
        try {
            request = HttpRequest.PUT("/authors", new AuthorUpdateCommand(authorIds.get(0), "森見登美彦"));
            response = client.toBlocking().exchange(request);
            fail();
        } catch (HttpClientResponseException e) {
            // ステータス検証
            assertEquals(HttpStatus.CONFLICT, e.getResponse().getStatus());
            // 本ケースで作成したエンティティを削除
            for (Long genreId : authorIds) {
                request = HttpRequest.DELETE("/authors/" + genreId);
                response = client.toBlocking().exchange(request);
                assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
            }
            throw e;
        }
    }

    private Long entityId(HttpResponse response) {
        String path = "/authors/";
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
