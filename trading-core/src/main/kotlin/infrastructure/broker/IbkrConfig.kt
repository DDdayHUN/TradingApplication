package infrastructure.broker

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

data class IbkrConfig(
    val host: String,
    val port: Int,
    val clientId: Int
) {
    companion object {

        fun fromEnv(): IbkrConfig {
            return IbkrConfig(
                host = System.getenv("IBKR_HOST")
                    ?: error("IBKR_HOST environment variable is missing"),

                port = System.getenv("IBKR_PORT")
                    ?.toInt()
                    ?: error("IBKR_PORT environment variable is missing"),

                clientId = System.getenv("IBKR_CLIENT_ID")
                    ?.toInt()
                    ?: error("IBKR_CLIENT_ID environment variable is missing")
            )
        }
    }
}

@Configuration
class IbkrConfiguration {
    @Bean
    fun ibkrConfig(): IbkrConfig {
        return IbkrConfig.fromEnv()
    }
}