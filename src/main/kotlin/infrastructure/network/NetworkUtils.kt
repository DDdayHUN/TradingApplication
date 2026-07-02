package infrastructure.network

import java.net.URI
import java.net.http.HttpRequest
import java.time.Duration

//===========================================================//
/**
 * Builds HTTP GET request for a given URL and adds API KEY.
 *
 * @param url target url of the request
 * @param headerName the name of the header that will contain api key
 * @param timeout maximum amount of time allowed for the request
 * @param apiKey API key value of the header
 *
 * @returns http request that is ready to be sent.
 */
//===========================================================//

fun httpGetRequestBuilder(url: String, headerName: String, timeout: Duration, apiKey: String): HttpRequest {
    return HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(timeout)
        .header(headerName, apiKey)
        .GET()
        .build()
}