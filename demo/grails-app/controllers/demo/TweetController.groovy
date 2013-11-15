package demo

import grails.rest.RestfulController

class TweetController extends RestfulController {
    TweetController() {
        super(Tweet)
    }

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
}
