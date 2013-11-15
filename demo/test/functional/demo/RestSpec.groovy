package demo

import geb.buildadapter.SystemPropertiesBuildAdapter
import grails.plugins.rest.client.RequestCustomizer
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.util.Metadata
import groovyx.remote.client.RemoteControl
import groovyx.remote.transport.http.HttpTransport
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource

import static org.springframework.http.HttpMethod.*

class RestSpec extends Specification {

    @Shared
    RemoteControl remoteControl

    @Shared
    RestClient client

    def setupSpec() {
        // Grails 2.3.1ではgrails.plugin.remotecontrol.RemoteControlが正しく動作しない
        // そのため直接groovyx.remote.client.RemoteControlにURLを指定して使う
        remoteControl = new RemoteControl(new HttpTransport("${getBaseUrl()}${grails.plugin.remotecontrol.RemoteControl.RECEIVER_PATH}"))
        client = new RestClient(getBaseUrl())
    }

    def setup() {
        // cleanupで綺麗にしてもよいがrun-appした状態で
        // テストする場合はテスト終了後にデータが確認できるという利点があるため
        // setupでクリアする
        cleanupDatabase()
    }

    private String getBaseUrl() {
        // コンソールからファンクショナルテストとして実行する場合は
        // システムプロパティに設定されるベースURLを使う
        // IDEからGrailsの環境を起動せずに直接Spockのテストとして実行する場合は
        // 固定のURLを使う
        System.properties[SystemPropertiesBuildAdapter.BASE_URL_PROPERTY_NAME] ?:
            "http://localhost:8080/${Metadata.getInstance(new File(Metadata.FILE)).getApplicationName()}/"
    }

    protected Object remote(Closure closure) {
        remoteControl.exec(closure)
    }

    private void cleanupDatabase() {
        remote {
            def sessionFactory = ctx.sessionFactory
            sessionFactory.currentSession.flush()

            def dataSource = ctx.dataSource
            def db = new groovy.sql.Sql(dataSource as DataSource)
            db.withBatch { stmt ->
                stmt.addBatch("SET REFERENTIAL_INTEGRITY FALSE")
                db.eachRow("SELECT table_name FROM INFORMATION_SCHEMA.tables WHERE table_schema = 'PUBLIC' AND table_type = 'TABLE'") {
                    if (!it.table_name.toLowerCase().startsWith('databasechangelog'))
                        stmt.addBatch("TRUNCATE table " + it.table_name)
                }
                stmt.addBatch("SET REFERENTIAL_INTEGRITY TRUE")
            }

            sessionFactory.currentSession.clear()
        }
    }

    protected RestResponse get(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.get(url, customizer)
    }

    protected RestResponse get(String url, Map<String, Object> urlVariables,
                               @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.get(url, urlVariables, customizer)
    }

    protected RestResponse put(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.put(url, customizer)
    }

    protected RestResponse put(String url, Map<String, Object> urlVariables,
                               @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.put(url, urlVariables, customizer)
    }

    protected RestResponse post(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.post(url, customizer)
    }

    protected RestResponse post(String url, Map<String, Object> urlVariables,
                                @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.post(url, urlVariables, customizer)
    }

    protected RestResponse patch(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.patch(url, customizer)
    }

    protected RestResponse patch(String url, Map<String, Object> urlVariables,
                                 @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.patch(url, urlVariables, customizer)
    }

    protected RestResponse delete(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.delete(url, customizer)
    }

    protected RestResponse delete(String url, Map<String, Object> urlVariables,
                                  @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.delete(url, urlVariables, customizer)
    }

    protected RestResponse head(String url, Map<String, Object> urlVariables,
                                @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.head(url, urlVariables, customizer)
    }

    protected RestResponse head(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.head(url, customizer)
    }

    protected RestResponse options(String url, Map<String, Object> urlVariables,
                                   @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.options(url, urlVariables, customizer)
    }

    protected RestResponse options(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.options(url, customizer)
    }

    protected RestResponse trace(String url, Map<String, Object> urlVariables,
                                 @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.trace(url, urlVariables, customizer)
    }

    protected RestResponse trace(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        client.trace(url, customizer)
    }

    static class RestClient extends RestBuilder {

        private String baseUrl

        RestClient(String baseUrl, proxy = Proxy.NO_PROXY) {
            // プロキシの設定をしない場合、環境変数のhttp.proxyHostの設定が自動的に使われる
            // 残念ながらno_proxyの設定までは見てくれないためhttp.proxyHostが設定されている環境下で
            // proxyを無効化したい場合は明示的に無効化する必要がある
            super([proxy: proxy])
            this.baseUrl = baseUrl
        }

        @Override
        protected RestResponse doRequestInternal(String url, Closure customizer, HttpMethod method, Map<String, Object> urlVariables = Collections.emptyMap()) {
            def requestCustomizer = new RequestCustomizer()

            // デフォルトJSONで固定
            requestCustomizer.accept(MediaType.APPLICATION_JSON_VALUE)
            if (method in [PUT, POST, DELETE]) {
                requestCustomizer.contentType(MediaType.APPLICATION_JSON_VALUE)
            }

            if (urlVariables)
                requestCustomizer.urlVariables.putAll(urlVariables)

            if (customizer != null) {
                customizer.delegate = requestCustomizer
                customizer.resolveStrategy = Closure.DELEGATE_FIRST
                customizer.call()
            }

            try {
                ResponseEntity responseEntity = invokeRestTemplate("${baseUrl}${url.replaceFirst("^/", "")}", method, requestCustomizer)
                handleResponse(responseEntity)
            } catch (HttpStatusCodeException e) {
                return new RestResponse(new ResponseEntity(e.getResponseBodyAsString(), e.responseHeaders, e.statusCode))
            }
        }
    }
}
