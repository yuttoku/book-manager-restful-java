## book-manager-restful-java
書籍と著者を管理するシンプルなAPIを提供します

## インストールと起動
以下のコマンドで起動します
```
$ git clone https://github.com/yuttoku/book-manager-restful-java.git
$ cd book-manager-restful-java
$ ./gradlew run
```
サンプルはhttpieで通信しています
```
$ brew install httpie
```

## 著者API

・著者を登録します ```POST localhost:8080/authors```　
```
$ http POST localhost:8080/authors name="森博嗣"

HTTP/1.1 201 Created
Date: Mon, 11 Nov 2019 10:51:57 GMT
Location: /authors/1
connection: keep-alive
content-length: 27
content-type: application/json

{
    "id": 1,
    "name": "森博嗣"
}
```

・著者の一覧を取得します ```GET localhost:8080/authors```
```
$ http GET localhost:8080/authors

HTTP/1.1 200 OK
Date: Mon, 11 Nov 2019 10:54:21 GMT
connection: keep-alive
content-length: 63
content-type: application/json

[
    {
        "id": 1,
        "name": "森博嗣"
    },
    {
        "id": 2,
        "name": "森見登美彦"
    },
    {
        "id": 3,
        "name": "夏目漱石"
    }
]
```

・著者を検索します ```GET localhost:8080/authors/search keyword=="森"```
```
$ http GET localhost:8080/authors/search keyword=="森"

HTTP/1.1 200 OK
Date: Mon, 11 Nov 2019 11:08:32 GMT
connection: keep-alive
content-length: 63
content-type: application/json

[
    {
        "id": 1,
        "name": "森博嗣"
    },
    {
        "id": 2,
        "name": "森見登美彦"
    }
]
```


・著者を更新します ```PUT localhost:8080/authors```
```
$ http PUT localhost:8080/authors id=1 name="Hiroshi Mori"

HTTP/1.1 204 No Content
Date: Mon, 11 Nov 2019 11:01:00 GMT
Location: /authors/1
connection: keep-alive
```

・著者を削除します ```DELETE localhost:8080/authors/:id```
```
$ http DELETE localhost:8080/authors/1

HTTP/1.1 204 No Content
Date: Mon, 11 Nov 2019 10:56:59 GMT
connection: keep-alive
```

## 書籍API

・書籍を登録します ```POST localhost:8080/books```
```
$ http POST localhost:8080/books isbn=xxx title="すべてがFになる" authorId=1

HTTP/1.1 201 Created
Date: Mon, 11 Nov 2019 11:04:27 GMT
Location: /books/4
connection: keep-alive
content-length: 91
content-type: application/json

{
    "author": {
        "id": 1,
        "name": "Hiroshi Mori"
    },
    "id": 4,
    "isbn": "xxx",
    "title": "すべてがFになる"
}
```

・書籍の一覧を取得します ```GET localhost:8080/books```
```
$ http GET localhost:8080/books

HTTP/1.1 200 OK
Date: Mon, 11 Nov 2019 11:12:59 GMT
connection: keep-alive
content-length: 269
content-type: application/json

[
    {
        "author": {
            "id": 1,
            "name": "森博嗣"
        },
        "id": 4,
        "isbn": "xxx",
        "title": "すべてがFになる"
    },
    {
        "author": {
            "id": 1,
            "name": "森博嗣"
        },
        "id": 5,
        "isbn": "yyy",
        "title": "黒猫の三角"
    },
    {
        "author": {
            "id": 1,
            "name": "森博嗣"
        },
        "id": 6,
        "isbn": "zzz",
        "title": "彼女は一人で歩くのか"
    }
]
```

・書籍を検索します ```GET localhost:8080/books/search keyword=="の"```
```
$ http GET localhost:8080/books/search keyword=="の"

HTTP/1.1 200 OK
Date: Mon, 11 Nov 2019 11:20:17 GMT
connection: keep-alive
content-length: 186
content-type: application/json

[
    {
        "author": {
            "id": 1,
            "name": "森博嗣"
        },
        "id": 5,
        "isbn": "yyy",
        "title": "黒猫の三角"
    },
    {
        "author": {
            "id": 1,
            "name": "森博嗣"
        },
        "id": 6,
        "isbn": "zzz",
        "title": "彼女は一人で歩くのか"
    }
]
```

・書籍を更新します ```PUT localhost:8080/books```
```
$ http PUT localhost:8080/books id=4 isbn=XXX title="THE PERFECT INSIDER" authorId=2

HTTP/1.1 204 No Content
Date: Mon, 11 Nov 2019 11:23:12 GMT
Location: /books/4
connection: keep-alive
```

・著者を削除します ```DELETE localhost:8080/books/:id```
```
$ http DELETE localhost:8080/books/4

HTTP/1.1 204 No Content
Date: Mon, 11 Nov 2019 11:26:01 GMT
connection: keep-alive
```

## DBとテーブル
Gradle起動時にインメモリH2が起動し、Hibernateが以下の設定で起動します

```yaml:Application.yml
jpa:
  default:
    packages-to-scan:
      - 'example.micronaut.domain.entity'
    properties:
      hibernate:
        hbm2ddl:
          auto: update
        show_sql: true
```

Hibernateがexample.micronaut.domain.entityをスキャンした結果、H2に以下のテーブルを作成します

・著者テーブル

| id | name |
| --- | --- |

・書籍テーブル
  
| id | isbn | title | authorId |
| --- | --- | --- | --- |
