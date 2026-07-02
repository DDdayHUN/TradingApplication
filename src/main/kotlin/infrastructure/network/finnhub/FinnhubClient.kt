package infrastructure.network.finnhub

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.sun.tools.javac.tree.DCTree.isBlank
import domain.assets.security.SecurityIdentifier
import infrastructure.network.finnhub.dto.FinnhubQuoteDto
import infrastructure.network.finnhub.dto.FinnhubSymbolDto
import infrastructure.network.httpGetRequestBuilder
import java.io.IOException
import java.net.URLEncoder
import java.net.http.HttpClient
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

class FinnhubClient (
    private val m_Config: FinnhubConfig,
    private val m_HttpClient: HttpClient = HttpClient.newHttpClient(),
)
{
    companion object {
        private val s_GSON: Gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        private val s_HEADER_NAME = "X-Finnhub-Token"
        private const val s_HTTP_MIN = 200
        private const val s_HTTP_MAX = 299
    }

    /*===================================================*/
    /*===================================================*/
    // Public Method(es)

    /**
     * Gets the latest quote for a given stock symbol.
     *
     * @param identifier The security identifier containing the isin number
     * @return [Result] containing finnhub quote dto.
     */

    fun getQuoteAsync(identifier: SecurityIdentifier): Result<FinnhubQuoteDto> {
        val symbol = getSymbol(identifier.isin).getOrElse {
            error -> return Result.failure(error)
        }

        val encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8)

        return getJson("/quote?symbol=$encodedSymbol")
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

        val dto = getJson<FinnhubSymbolDto>("/search/?q=$encodedIsin").getOrElse{
            error -> return Result.failure(error)
        }

        val symbol = dto.result.firstOrNull()?.symbol

        if(symbol == null || symbol.isBlank()){
            return Result.failure(
                IllegalStateException("Finnhub did not return a ticker symbol")
            )
        }

        return Result.success(symbol)
    }

    /**
     * Sends Get request to the given Finnhub endpoint and parses JSON response
     *
     * @param endpoint finnhub endpoint, including query parameter
     * @return [Result] containing the dto
     */
    private inline fun <reified T> getJson(endpoint: String): Result<T> {
        val url = m_Config.baseUrl + endpoint

        val request = httpGetRequestBuilder(url,
            s_HEADER_NAME,
            m_Config.timeout,
            m_Config.apiKey
        )

        return try {
            val response: HttpResponse<String> = m_HttpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )

            if (response.statusCode() !in s_HTTP_MIN..s_HTTP_MAX) {
                return Result.failure(
                    IOException("Finnhub request failed. Status code: ${response.statusCode()}, URL: $url")
                )
            }

            val body = response.body()

            if (body.isNullOrBlank()) {
                return Result.failure(
                    IOException("Finnhub returned an empty response. URL: $url")
                )
            }

            val dto = s_GSON.fromJson(body, T::class.java)

            if (dto == null) {
                Result.failure(IOException("Failed to parse Finnhub response. URL: $url"))
            } else {
                Result.success(dto)
            }

        } catch (exception: IOException) {
            Result.failure(exception)
        } catch (exception: JsonSyntaxException) {
            Result.failure(IOException("Finnhub returned invalid JSON. URL: $url", exception))
        } catch (exception: IllegalArgumentException) {
            Result.failure(IOException("Invalid Finnhub request URL: $url", exception))
        }
    }
}
