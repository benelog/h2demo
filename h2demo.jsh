//usr/bin/env jshell --add-modules jdk.incubator.httpclient --show-version "$0" "$@"; exit $?

import jdk.incubator.http.*;

HttpClient client = HttpClient.newHttpClient();
HttpRequest req = HttpRequest.newBuilder().uri(new URI("http://h2demo.net:8080")).GET().build();
HttpResponse<String> res = client.send(req, HttpResponse.BodyHandler.asString());
System.out.println(res.headers().map());
System.out.println(res.body());
/exit
