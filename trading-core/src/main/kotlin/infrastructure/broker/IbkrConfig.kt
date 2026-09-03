package infrastructure.broker

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "ibkr")
data class IbkrConfig(
    val host: String,
    val port: Int,
    val clientId: Int
)

@Configuration
class IbkrConfiguration