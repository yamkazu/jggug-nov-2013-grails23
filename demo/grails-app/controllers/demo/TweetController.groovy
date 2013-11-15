package demo

import grails.rest.RestfulController

class TweetController extends RestfulController {
    TweetController() {
        super(Tweet)
    }
}
