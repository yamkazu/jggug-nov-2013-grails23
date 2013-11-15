class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.${format})?"{
            constraints {
                // apply constraints here
            }
        }

        "/statuses"(resources: "tweet", excludes: ["create", "edit", "update"])
        "/users"(resources: "user") {
            "/statuses"(resources: "tweet", includes: ["index"])
        }

        "/"(view:"/index")
        "500"(view:'/error')
	}
}
