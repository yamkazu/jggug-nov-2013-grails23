# Grails 2.3: RESTハンズオン

## 自己紹介

* @yamkazu
* Kazuki YAMAMOTO

## アジェンダ

* @Resourceアノテーション
* RESTリソースのマッピング
* スキャフォルド
* RestfulController
* ネストリソース
* レンダリングのカスタマイズ
* テスト

## 進め方

* 質問は随時受け付けます
* 一応演習のようなものをベースに進めます

## 今日使うもの

* Grails 2.3.2
* IDE(好きなもの)
    * IntelliJ IDEA 13 Beta
    * GGTS 3.4.0.RELEASE
* cURL
    * RESTクライアントであればなんでも良い

## はじめに

まずはじめにプロジェクトを作りましょう。

```
$ grails create-app myApp
$ cd myApp
$ grails run-app
```

うまく起動できたらブラウザで<http://localhost:8080/myApp>にアクセスしてください。
Grailsの画面が表示されればOKです。

> **NOTE:** 停止するときは`CTRL+C`

## IDEへインポート

好みのテキストエディタやIDEでプロジェクトを開きます。

### IntelliJ IDEA

以下のどちらかでインポートできます。

* Welcome画面から`Import Project`
* `File > Import Project`

> **NOTE:** Grails 2.3.2をIntelliJ IDEAに設定していない場合はインポートウィザードの途中で設定してください

### GGTS

Groovy 2.1のコンパイラをインストールしていない場合は、DashboardのExtentionsタブから以下をインストールします。

* Groovy 2.1 Compiler for Groovy-Eclipse

`File > Import > Grails Project`でGrailsプロジェクトをインポートします。

> **NOTE:** Grails 2.3.2をGGTSに設定していない場合は`Configure Grails Installations...`から追加してください。

## @Resourceアノテーション

Grails 2.3から新しく追加されたアノテーションです。
ドメインクラスに指定して使います。

`@Resource`をドメインクラスに付与することで

* REST対応コントローラの生成
* URLマッピングへの設定の追加

が自動的に行われます。

```
import grails.rest.Resource

@Resource(uri = "/statuses", formats = ["json", "xml"])
class Tweet {
    String text
}
```

> **NOTE:** @ResourceはAST変換で処理されます。そのため、実行時ではなくコンパイル時にコントローラが生成されます。

`@Resource`には次の属性が設定できます。

<dl>
  <dt>uri</dt>
  <dd>リソースを公開するURIです。</dd>
</dl>
<dl>
  <dt>readOnly</dt>
  <dd><code>true</code>に設定すると読み取り専用リソースになります。デフォルトは<code>false</code>です。</dd>
</dl>
<dl>
  <dt>formats</dt>
  <dd>許可するフォーマットを指定します。デフォルトは<code>["xml", 'json']</code>です。最初の要素がデフォルトのフォーマットになります。</dd>
</dl>

> **NOTE:** `uri`の設定を省略するとコントローラだけが生成されます。URLマッピングの設定は追加されません。ただし、デフォルトで`UrlMappings.groovy`に設定されている`"/$controller/$action?/$id?(.${format})?"`の設定経由ではアクセスできます。

URLマッピングで公開されるURIと生成されるコントローラの対応は次のとおりです。

| HTTP Method | URI                   | Grails Action |
|-------------|-----------------------|---------------|
| GET         | /statuses             | index         |
| GET         | /statuses/create      | create        |
| POST        | /statuses             | save          |
| GET         | /statuses/${id}       | show          |
| GET         | /statuses/${id}/edit  | edit          |
| PUT         | /statuses/${id}       | update        |
| DELETE      | /statuses/${id}       | delete        |

> **NOTE:** `create`と`edit`は`formats`で`html`をサポートする場合のみ有効です。

### やってみよう: @Resourceを使う

1. ドメインクラスを作る
    ```
    $ grails create-domain-class Tweet
    ```
2. `Tweet`を次のように実装する
    ```
    import grails.rest.Resource
    
    @Resource(uri = "/statuses", formats = ["json", "xml"])
    class Tweet {
        String text
    }
    ```
3. インタラクティブモードで`run-app`で起動する
    ```
    $ grails
    grails> run-app
    ```
4. cURLでいろいろ実行してみる
    ```
    // 一覧取得
    $ curl -i localhost:8080/myApp/statuses
    $ curl -i localhost:8080/myApp/statuses.xml
    $ curl -i -H "Accept: application/xml" localhost:8080/myApp/statuses

    // 追加
    $ curl -i -X POST -H "Content-Type: application/json" -d '{"text":"Hello Grails"}' localhost:8080/myApp/statuses

    // 取得
    $ curl -i localhost:8080/myApp/statuses/1

    // 更新
    $ curl -i -X PUT -H "Content-Type: application/json" -d '{"text":"Bye Grails"}' localhost:8080/myApp/statuses/1

    // 削除
    $ curl -i -X DELETE localhost:8080/myApp/statuses/1
    ```

## URLマッピングでリソースを公開する

`@Resource`アノテーションの`uri`でリソースを公開する以外に、明示的な`UrlMappings.groovy`の設定でリソースを公開できます。
`@Resource`アノテーションの`uri`の設定を消して、`UrlMappings.groovy`に次の一行を追加します。

```
"/statuses"(resources: "tweet")
```

### url-mappings-reportコマンド

Grails 2.3から新しく`url-mappings-report`コマンドが追加されました。
現在のURLマッピング設定から、カラフルでかっこいいレポートを出力してくれます。

```
$ grails url-mappings-report
| URL Mappings Configured for Application
| ---------------------------------------

Dynamic Mappings
 |    *     | /${controller}/${action}?/${id}?(.${format)?              | Action: (default action) |
 |    *     | /                                                         | View:   /index           |
 |    *     | ERROR: 500                                                | View:   /error           |

Controller: dbdoc
 |    *     | /dbdoc/${section}?/${filename}?/${table}?/${column}?      | Action: (default action) |

Controller: tweet
 |   GET    | /statuses                                                 | Action: index            |
 |   GET    | /statuses/create                                          | Action: create           |
 |   POST   | /statuses                                                 | Action: save             |
 |   GET    | /statuses/${id}                                           | Action: show             |
 |   GET    | /statuses/${id}/edit                                      | Action: edit             |
 |   PUT    | /statuses/${id}                                           | Action: update           |
 |  DELETE  | /statuses/${id}                                           | Action: delete           |
```

今のところ`@Resource`アノテーションの`uri`で公開するリソースはレポートに含まれていません。

### includes、excludes

`includes`、`excludes`を使って特定のAPIのみを公開したり、非公開にしたりできます。

```
// 取得系のAPIのみを公開する
"/statuses"(resources: "tweet", includes: ["index", "show"])

// 更新系のAPIを非公開する
"/statuses"(resources: "tweet", excludes: ["save", "update", "delete"])
```

### やってみよう: URLマッピングでリソースを公開する

1. `url-mappings-report`コマンドを実行してみる
    ```
    $ grails url-mappings-report
    ```
2. `Tweet`クラスに設定した`@Resource`の`uri`を削除する
    ```
    @Resource(formats = ["json", "xml"])
    ```
3. `grails-app/conf/UrlMappings.groovy`でリソースを公開する
    ```
    "/statuses"(resources: "tweet")
    ```
4. `url-mappings-report`コマンドを実行してみる
    ```
    $ grails url-mappings-report
    ```
5. `excludes`で`create`、`edit`、`update`を非公開にする
    ```
    "/statuses"(resources: "tweet", excludes: ["create", "edit", "update"])
    ```
6. `url-mappings-report`コマンドを実行してみる
    ```
    $ grails url-mappings-report
    ```
7. 非公開にしたAPIを叩いてみる
    ```
    $ curl -i -X PUT -H "Content-Type: application/json" -d '{"text":"Dummy"}' localhost:8080/myApp/statuses/1
    ```

## RESTコントローラを作成する

`@Resource`アノテーションをドメインクラスに付与すると、自動的にコントローラを生成してくれますが、コントローラを自前で作ることもできます。
ユーザ自身でコントローラを作ることで任意のカスタマイズが可能になります。

Grails 2.3では、RESTコントローラを作るために2つのサポートが追加されました。

* スキャフォルドによって生成されるコントローラのREST対応
* `RestfulController`の追加

### やってみよう: スキャフォルでコントローラを生成する

1. ドメインクラスを新しく作る
    ```
    $ grails create-domain-class user
    ```
2. ドメインクラスの中身を実装する
    ```
    package myapp
    
    class User {
        String username
    }
    ```
3. スキャフォルドでコントローラを生成する
    ```
    $ grails generate-all myapp.User
    ```
4. ブラウザからアクセスする
5. cURLから叩いてみる

### respondメソッド

`responst`メソッドはGrails 2.3から追加されたメソッドです。
Grailsのコンテンツネゴシエーションを使って、リクエストに応じて最も最適なレスポンスを返します。

```
def index(Integer max) {
    params.max = Math.min(max ?: 10, 100)
    respond User.list(params), model:[userInstanceCount: User.count()]
}
```

コンテンツネゴシエーションは、ACCEPTヘッダまたはリソースの拡張子を使って最適なレスポンスを判断します。

### RestfulController

`RestfulController`はGrails 2.3で新たに追加されたクラスです。
これはREST対応のコントローラを作るためのベースコントローラで、このクラスを継承することで簡単にREST対応のコントローラが作れます。

```
package myapp

import grails.rest.RestfulController

class TweetController extends RestfulController {
    TweetController() {
        super(Tweet)
    }
}
```

`RestfulController`の中身はスキャフォルドで生成されるコントローラのコードほぼ同じです。
必要に応じてアクションをオーバーライドしたり、追加したりできます。

### やってみよう: RestfulControllerを使う

1. `Tweet`クラスから`@Resource`を削除
2. `Tweet`用のコントローラを作成

    ```
    $ grails create-controller myapp.Tweet
    ```

3. RestfulControllerを継承したコントローラにする

    ```
    package myapp
    
    import grails.rest.RestfulController
    
    class TweetController extends RestfulController {
        TweetController() {
            super(Tweet)
        }
    }
    ```

4. cURLから叩いてみる

## ネストリソース

URLマッピングでネストしたリソースを定義できます。

```
"/users"(resources: "user") {
    "/statuses"(resources: "tweet", includes: ["index"])
}
```

このように定義すると`/users/${userId}/statuses`というURLでアクセスできます。
`userId`は`params.userId`で取得できます。

### やってみよう: ネストリソースを作ってみる

1. `grails-app/conf/UrlMappings.groovy`にネストリソースを追加

    ```
    "/users"(resources: "user") {
        "/statuses"(resources: "tweet", includes: ["index"])
    }
    ```

2. url-mappings-reportコマンドを実行する

    ```
    $ grails url-mappings-report
    ```

3. `Tweet`クラスに`User`の関連を追加

    ```
    package myapp
    
    class Tweet {
        String text
        User user
    }
    ```

4. `TweetController`に以下を追加して`userId`が指定されている場合は対象ユーザの`tweet`だけを取得するようにする

    ```
    @Override
    protected List listAllResources(Map params) {
        def c = Tweet.createCriteria()
        c.list(params) {
            if (params.userId) {
                user {
                    eq 'username', params.userId
                }
            }
        }
    }

    @Override
    protected Integer countResources() {
        def c = Tweet.createCriteria()
        c.count {
            if (params.userId) {
                user {
                    eq 'username', params.userId
                }
            }
        }
    }
    ```

5. 画面も生成してHTMLに対応する

    ```
    $ grails generate-views myapp.Tweet
    ```

6. `grails-app/conf/BootStrap.groovy`で適当にデータを突っ込んでおく

    ```
    init = { servletContext ->
        environments {
            development {
                def nobeans = new User(username: "nobeans").save()
                def yamkazu = new User(username: "yamkazu").save()
                
                new Tweet(text: "ビール", user: nobeans).save()
                new Tweet(text: "にゃんぱすー", user: yamkazu).save()
                new Tweet(text: "温泉いきたい", user: yamkazu).save()
            }
        }
    }
    ```

7. `/users/${userId}/statuses`を叩く

    ```
    // 一覧取得
    $ curl -i -H "Accept: application/json" localhost:8080/myApp/statuses
    $ curl -i -H "Accept: application/json" localhost:8080/myApp/users/nobeans/statuses
    $ curl -i -H "Accept: application/json" localhost:8080/myApp/users/yamkazu/statuses
    ```

8. 関連を含んだリソースの登録

    `curl -i -X POST -H "Content-Type: application/json" -H "Accept: application/json" -d '{"text":"Hello Grails", "user":{"id":"1"}}' localhost:8080/myApp/statuses`

9. ブラウザからも開いてみる
    * <http://localhost:8080/myApp/users/nobeans/statuses>
    * <http://localhost:8080/myApp/users/yamkazu/statuses>

## レンダリングのカスタマイズ

GrailsではJSONなどのレスポンスのレンダリングをカスタマイズする多くの方法が提供されています。

* コントローラで`render(contentType: "text/json") { ... }`を使ってカスタマイズする
* デフォルトレンダラーのカスタマイズする
* カスタムマーシャラーを実装する
* カスタムレンダラーの実装する
* GSPを使う

詳しくは[リファレンス](http://grails.jp/doc/latest/guide/webServices.html#renderers)を参照してください。

今日は`InstanceMethodBasedMarshaller`を使った方法を紹介します。
`InstanceMethodBasedMarshaller`はドメインクラスの`toJSON(JSON converter)`メソッド(JSONの場合)の実装にを基に動作するマーシャラーです。

### やってみよう: InstanceMethodBasedMarshallerを使ってレンダリングをカスタマイズする

`Tweet`クラスのレンダリングをカスタマイズします。

1. `InstanceMethodBasedMarshaller`を登録する

    `grails-app/conf/spring/resources.groovy`に以下のように追加する
    ```
    import grails.converters.JSON
    import org.codehaus.groovy.grails.web.converters.configuration.ObjectMarshallerRegisterer
    import org.codehaus.groovy.grails.web.converters.marshaller.json.InstanceMethodBasedMarshaller
    beans = {
        instanceMethodBasedMarshallerRegister(ObjectMarshallerRegisterer) {
            marshaller = new InstanceMethodBasedMarshaller()
            converterClass = JSON
        }
    }
    ```

2. `Tweet`に`toJSON`メソッドを実装する

    ```
    import grails.converters.JSON

    ....

    def toJSON(JSON converter) {
        converter.build {
            id id
            text text
            username user.username
        }
        
        // MapでもOK
        // [id: id, text: text, username: user.username]
    }
    ```

3. `/statuses`を叩く

    ```
    $ curl -i -H "Accept: application/json" localhost:8080/myApp/statuses
    ```

## テスト

REST APIをテストするにはユニットを使う方法もありますが、今日はファンクショナルテストを使う方法紹介します。
ちなみに`@Resoruceアノテーション`で公開したリソースは、コントローラのクラスが存在しないためユニット、インテグレーションではテストできません（自動生成されたコントローラをテストする価値があるのかという話はありますが）。

### 何を使うか？

Spockと連携して使うのは決まり。
RESTクライアントは何を使うか？

* REST Client Builder Plugin
* Grails REST Plugin
* Async Http Client

好きなものを使えば良い。

### 従来のファンクショナルテストの問題点

* フィードバックが遅い
* インタラクティブモードでテストできない
* トランザクションがきない
    * インテグレーションテストではテスト終了時にデータベースへの操作が全てロールバックする

### 2.3からのファンクショナルテスト

* `run-app`で起動済みのサーバがある場合は、そのサーバに対してテストが実行される
* 2.3のフォーク実行対応で？インタラクティブモードでテストができるようになった
* ただしフォーク実行になったためファンクショナルテスト内でGrailsの内部にアクセスできなくなった
    * サーバが動くJVMとテストが動くJVMが異なるため

### 今思うベストな構成

* Spockは決まり
* デフォルトではファンクショナルテストのサポートがないため、とりあえずGebは入れとく
* Grailsの内部にアクセスする必要がある場合はRemote Controlを使う
    * ただし2.3.2時点ではRemote Controlが正しく動作しない（回避方法はある）
* トランザクションが効かないのでRemote Control経由でデータベースの中身を全て削除する仕組みを作る
    * 参考: https://github.com/geb/geb-talks/blob/master/talks/grails/src/demos/06-db-cleaner/src/groovy/test/DbCleaner.groovy
* フィクスチャもRemote Control経由で構築する、以下のプラグインを検討するとよい
    * Build Test Data Plugin
    * Grails Fixtures Plugin
* Grailsに依存する部分をすべてRemote Control経由で行うことで、テストの起動がGrailsに依存しなくなる
    * IDEから通常のテストのように起動できる!
* なので普段は裏で`run-app`しておいて、IDEからテストを作成すると効率が良い

## まとめ

* だしぽんAPIなら`@Resource`ですぐ作るれる
* ロジックが必要ならコントローラを作る
    * スキャフォルド
    * RestfulController
* レンダリングのカスタマイズも柔軟
* テストもしやすい
* 今日話せなかったこともまだいくつかあるので詳しくは[リファレンス](http://grails.jp/doc/latest/guide/webServices.html)

