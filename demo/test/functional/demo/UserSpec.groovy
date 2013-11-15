package demo

class UserSpec extends RestSpec {

    def "GET /users: 一覧(0件)"() {
        when:
            def response = get("users")

        then:
            response.status == 200
            !response.json
    }

    def "GET /users: 一覧(1件)"() {
        given:
            remote { new User(username: "yamkazu").save(flush: true).id }

        when:
            def response = get("users")

        then:
            response.status == 200
            response.json*.username == ["yamkazu"]
    }

    def "GET /users/{id}: 取得"() {
        given:
            def id = remote { new User(username: "yamkazu").save(flush: true).id }

        when:
            def response = get("users/${id}")

        then:
            response.status == 200
            response.json.username == "yamkazu"
    }

    def "GET /users/{id}: 存在しないリソースの取得"() {
        when:
            def response = get("users/1")

        then:
            response.status == 404
            !response.json
    }

    def "POST /users: 登録"() {
        when:
            def response = post("users") {
                json { username = "yamkazu" }
            }

        then:
            response.status == 201
            response.json.username == "yamkazu"
    }

    def "POST /users: 制約違反"() {
        when:
            def response = post("users") {
                json { username = "" }
            }

        then:
            response.status == 422
            response.text == /{"errors":[{"object":"${User.name}","field":"username","rejected-value":null,"message":"Property [username] of class [class ${User.name}] cannot be null"}]}/
    }

    def "PUT /users/{id}: 更新"() {
        given:
            def id = remote { new User(username: "yamkazu").save(flush: true).id }

        when:
            def response = put("users/${id}") {
                json { username = "nobeans" }
            }

        then:
            response.status == 200
            response.json.username == "nobeans"
    }

    def "PUT /users/{id}: 制約違反"() {
        given:
            def id = remote { new User(username: "yamkazu").save(flush: true).id }

        when:
            def response = put("users/${id}") {
                json { username = "" }
            }

        then:
            response.status == 422
            response.text == /{"errors":[{"object":"${User.name}","field":"username","rejected-value":null,"message":"Property [username] of class [class ${User.name}] cannot be null"}]}/
    }

    def "PUT /users/{id}: 存在しないリソースの更新"() {
        when:
            def response = put("users/1") {
                json { username = "ダミー" }
            }

        then:
            response.status == 404
            !response.json
    }

    def "DELETE /users/{id}: 削除"() {
        given:
            def id = remote { new User(username: "yamkazu").save(flush: true).id }

        when:
            def response = delete("users/${id}")

        then:
            response.status == 204
            !response.json
    }

    def "DELETE /users/{id}: 存在しないリソースの削除"() {
        when:
            def response = delete("users/1")

        then:
            response.status == 404
            !response.json
    }
}
