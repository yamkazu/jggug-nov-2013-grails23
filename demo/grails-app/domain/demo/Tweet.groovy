package demo

import grails.rest.Resource

@Resource(formats = ["json", "xml"])
class Tweet {
    String text
}
