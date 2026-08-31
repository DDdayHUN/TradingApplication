package data.network.finnhub

import java.io.FileInputStream
import java.io.IOException
import java.time.Duration
import java.util.Properties

//===========================================================//
/**
 * Configuration class for Finnhub API access.
 * This Config loads API key from local .env file.
 */
//===========================================================//

internal class FinnhubConfig {
    /*===================================================*/
    /*===================================================*/
    // Public Field(s)

    val apiKey: String
    val baseUrl: String
    val timeout: Duration

    companion object {
        /*===================================================*/
        /*===================================================*/
        // Private Field(s)

        private const val s_ENV_FILE = "../.env"
        private const val s_FINNHUB_API_KEY_NAME = "FINNHUB_API_KEY"
        private const val s_DEFAULT_BASE_URL = "https://finnhub.io/api/v1"
        private val s_DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(10)

        /*===================================================*/
        /*===================================================*/
        // Private Method(es)

        /**
         * Loads API key from .env file.
         * @return the Finnhub API key
         */
        @Throws(IllegalStateException::class)
        private fun loadApiKeyFromEnv(): String {
            val properties = Properties()

            try {
                FileInputStream(s_ENV_FILE).use { input ->
                    properties.load(input)
                }
            } catch (ex: IOException) {
                throw IllegalStateException(
                    "Could not load local .env file. Make sure .env file exists in the project root.",
                    ex
                )
            }

            val apiKey: String = properties.getProperty(s_FINNHUB_API_KEY_NAME)

            check(!apiKey.isBlank()) { "Finnhub API Key is missing" }

            return apiKey
        }
    }

    /*===================================================*/
    /*===================================================*/
    // Constructor(s)

    /**
     * Initializes Finnhub configuration with custom values.
     * Api key is still loaded from local .env file.
     *
     * @param baseUrl base URL of the Finnhub API
     * @param timeout maximum time allowed for Finnhub API requests
     */
    constructor(baseUrl: String = s_DEFAULT_BASE_URL, timeout: Duration = s_DEFAULT_TIMEOUT) {
        require(!baseUrl.isBlank()) { "Base URL is missing" }
        require(!timeout.isNegative && !timeout.isZero) { "Timeout is invalid" }

        this.apiKey = loadApiKeyFromEnv()
        this.baseUrl = baseUrl
        this.timeout = timeout
    }
}