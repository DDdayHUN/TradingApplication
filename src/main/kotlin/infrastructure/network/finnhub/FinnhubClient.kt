package infrastructure.network.finnhub

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import domain.stock.Quote
import infrastructure.network.finnhub.dto.FinnhubQuoteDto
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

//===========================================================//
/**
 * Client class for communicating with the Finnhub API.
 * 
 * 
 * This class is responsible for HTTP communication with Finnhub.
 * It sends requests, receives JSON response, converts the response
 * DTO into Quote domain model
 *
 * Initializes Finnhub client with config
 * @param config Finnhub configuration API KEY, base URL and timeout
 */
//===========================================================//

class FinnhubClient(
    config: FinnhubConfig
) {
    /*===================================================*/
    /*===================================================*/
    // Private Field(s)

    private val m_config: FinnhubConfig
    private val m_httpClient: HttpClient

    companion object {
        private val s_GSON: Gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
    }

    /*===================================================*/
    /*===================================================*/
    // Public Method(es)

    /**
     * Gets the latest quote for a given stock symbol.
     * 
     * @param symbol stock ticker symbol, for example "NET", "AAPL", "MSFT"
     * @return latest quote as a clean domain object
     * @throws IOException if the Finnhub request fails
     * @throws InterruptedException if the HTTP request is interrupted
     */
    @Throws(IOException::class, InterruptedException::class)
    fun getQuote(symbol: String): Quote {
        require(!(symbol == null || symbol.isEmpty())) { "Symbol is missing" }

        val encodedSymbol: String? = URLEncoder.encode(symbol, StandardCharsets.UTF_8)
        val url = m_config.baseUrl + "/quote?symbol=" + encodedSymbol

        val request: HttpRequest? = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(m_config.timeout)
            .header("X-Finnhub-Token", m_config.apiKey)
            .GET()
            .build()

        val response: HttpResponse<String?> = m_httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw IOException(
                ("Finnhub request failed. Status: " + response.statusCode()
                        + " Body: " + response.body())
            )
        }

        val dto = s_GSON.fromJson(
            response.body(),
            FinnhubQuoteDto::class.java
        )

        if (dto == null) {
            throw IOException("Finnhub returned an empty quote response")
        }

        return dto.toDomain(symbol)
    }

    /*===================================================*/
    /*===================================================*/
    // Constructor(s)

    init {
        this.m_config = config
        this.m_httpClient = HttpClient.newHttpClient()
    }
}
