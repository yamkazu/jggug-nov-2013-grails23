import demo.Tweet
import demo.User

class BootStrap {

    def init = { servletContext ->
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
    def destroy = {
    }
}
