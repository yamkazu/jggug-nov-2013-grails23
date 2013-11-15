package demo

import grails.converters.JSON

class Tweet {
    String text
    User user

    def toJSON(JSON converter) {
        converter.build {
            id id
            text text
            username user.username
        }

        // MapでもOK
        // [id: id, text: text, username: user.username]
    }
}
