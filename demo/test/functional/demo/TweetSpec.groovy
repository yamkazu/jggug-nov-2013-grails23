package demo

class TweetSpec extends RestSpec {

    def users

    def setup() {
        users = remote {
            def nobeans = new User(username: "nobeans").save()
            def yamkazu = new User(username: "yamkazu").save()

            new Tweet(text: "ビール", user: nobeans).save()
            new Tweet(text: "にゃんぱすー", user: yamkazu).save()
            new Tweet(text: "温泉いきたい", user: yamkazu).save()

            return [yamkazu: yamkazu.id, nobeans: nobeans.id]
        }
    }

    def "一覧を取得する"() {
        when:
            def response = client.get("statuses")

        then:
            response.status == 200
            response.json*.text == ["ビール", "にゃんぱすー", "温泉いきたい"]
    }

    def "ツイートする"() {
        given:
            def userId = users.yamkazu

        when:
            def response = client.post("statuses") {
                json {
                    text = "Hello Grails"
                    user {
                        id = userId
                    }
                }
            }

        then:
            response.status == 201
    }
}
