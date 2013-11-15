class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.${format})?"{
            constraints {
                // apply constraints here
            }
        }

        "/statuses"(resources: "tweet", excludes: ["create", "edit", "update"])

        "/"(view:"/index")
        "500"(view:'/error')
	}
}
