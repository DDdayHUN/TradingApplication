package infrastructure.network

import java.net.URI
import java.net.http.HttpRequest
import java.time.Duration


fun httpRequestBuilder(url: String, headerName: String, timeout: Duration, apiKey: String): HttpRequest {
    return HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(timeout)
        .header(headerName, apiKey)
        .GET()
        .build()
}