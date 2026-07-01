package infrastructure.network.finnhub

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import domain.assets.security.SecurityIdentifier
import infrastructure.network.finnhub.dto.FinnhubQuoteDto
import infrastructure.network.finnhub.dto.FinnhubSymbolDto
import infrastructure.network.httpRequestBuilder
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

        private val s_HEADER_NAME = "X-Finnhub-Token"
    }

    /*===================================================*/
    /*===================================================*/
    // Public Method(es)

    /**
     * Gets the latest quote for a given stock symbol.
     * 
     * @param symbol stock ticker symbol, for example "NET", "AAPL", "MSFT"
     * @return latest quote as a clean domain object
     */

    fun getQuote(identifier: SecurityIdentifier): Result<FinnhubQuoteDto> {
        val encodedSymbol = getSymbol(identifier.isin).getOrNull()
        val url = m_Config.baseUrl + "/quote?symbol=" + encodedSymbol

        val request = httpRequestBuilder(url, s_HEADER_NAME, m_Config.timeout, m_Config.apiKey)

        val response: HttpResponse<String?> = m_HttpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() !in 200..<300) {
            return Result.failure(IOException("Unexpected error"))
        }

        val dto: FinnhubQuoteDto? = s_GSON.fromJson(
            response.body(),
            FinnhubQuoteDto::class.java
        )

        if (dto == null) {
            return Result.failure(IOException("Finnhub returned an empty quote response"))
        }

        return Result.success(dto)
    }

    /*===================================================*/
    /*===================================================*/
    // Private Method(es)

    /**
     * Gets Ticker Symbol for a given isin number.
     *
     * @param isin International Securities Identification Number
     * @returns result ticker symbol
     */
    private fun getSymbol(isin: String): Result<String> {
        val encodedIsin = URLEncoder.encode(isin, StandardCharsets.UTF_8)
        val url = m_Config.baseUrl + "/search?q=" + encodedIsin

        val request = httpRequestBuilder(url, s_HEADER_NAME, m_Config.timeout, m_Config.apiKey)

        val response : HttpResponse<String?> = m_HttpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() !in 200..<300) {
            return Result.failure(IOException("Unexpected error"))
        }

        val dto: FinnhubSymbolDto? = s_GSON.fromJson(
            response.body(),
            FinnhubSymbolDto::class.java
        )

        if (dto == null) return Result.failure(IOException("Finnhub returned empty response from server"))
        if (dto.count == 0) return Result.failure(IllegalStateException("Finnhub returned empty response from server"))


        return Result.success(dto.result[0].symbol)
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
