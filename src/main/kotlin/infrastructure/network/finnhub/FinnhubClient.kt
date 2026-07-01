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
 * This class is responsible for HTTP communication with Finnhub.
 * It sends requests, receives JSON response, converts the response
 * DTO into Quote domain model
 */
//===========================================================//

class FinnhubClient {
    /*===================================================*/
    /*===================================================*/
    // Private Field(s)

    private val m_Config: FinnhubConfig
    private val m_HttpClient: HttpClient

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
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Throws(IOException::class, InterruptedException::class)
    fun getQuote(symbol: String): Quote {
        require(!symbol.isEmpty()) { "Empty" }

        val encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8)
        val url = m_Config.baseUrl + "/quote?symbol=" + encodedSymbol

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(m_Config.timeout)
            .header("X-Finnhub-Token", m_Config.apiKey)
            .GET()
            .build()

        val response: HttpResponse<String?> = m_HttpClient.send(
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

    /**
     * Initializes Finnhub client with config
     * @param config Finnhub configuration API KEY, base URL and timeout
     */
    constructor(config: FinnhubConfig) {
        this.m_Config = config
        this.m_HttpClient = HttpClient.newHttpClient()
    }
}
