# adoptium.net documentation services

## Introduction

The service is in development. It should provide the documentation that is created
at [this repository](https://github.com/AdoptOpenJDK/website-adoptium-documentation) on
the [adoptium.net website](https://adoptium.net). The service is based
on [Eclipse MicroProfile](https://microprofile.io).

## Building and launching this application

The service is currently configured to run in [OpenLiberty](https://openliberty.io/). By calling the Maven
command `mvn liberty:dev` a running instance can be started at port `9080`. The SwaggerUI frontend of the service can be
reached at `http://localhost:9080/openapi/ui/`. The root folder of the repo contains a `simple-demonstration.html` file
that can be opened in a browser to see the usage of the service. In this sample a documentation
from [https://github.com/adoptium/documentation](https://github.com/adoptium/documentation) will be loaded and added to
the body of the page.

By default the service will use the GitHub REST API without any authentification. By doing so only 60 requests can be
done each hour. This is very limited and you can only display some documents by doing so. If the environment
variable `GITHUB_ACCESS_TOKEN` exists the value will be used as a personal GitHub access token. By doing so 1.000
requests can be done each hour.



