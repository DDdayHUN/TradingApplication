package data.network.finnhub

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

//===========================================================//
/**
 * Configuration class for Finnhub API access.
 * This Config loads API key from local .env file.
 */
//===========================================================//
class FinnhubConfig(
    val apiKey: String,
    val baseUrl: String,
    val timeout: Duration = s_DEFAULT_TIMEOUT
){

    init {
        require(apiKey.isNotBlank()) { "Finnhub API key is missing" }
        require(baseUrl.isNotBlank()) { "Base URL is missing" }
        require(!timeout.isNegative && !timeout.isZero) { "Timeout is invalid" }
    }

    companion object {
        val s_DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(10)
    }
}

@ConfigurationProperties(prefix = "finnhub")
data class FinnhubProperties(
    var apiKey: String,
    val baseUrl: String,
    val timeout: Duration = Duration.ofSeconds(10)
)

@Configuration
class FinnhubConfiguration{
    @Bean
    fun finnhubConfig(properties: FinnhubProperties): FinnhubConfig {
        return FinnhubConfig(
            apiKey = properties.apiKey,
            baseUrl = properties.baseUrl,
        )
    }
}

