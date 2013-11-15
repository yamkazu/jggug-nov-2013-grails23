package demo

import grails.rest.Resource

@Resource(uri = "/statuses", formats = ["json", "xml"])
class Tweet {
    String text
}
