package api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "ibkr")
data class IbkrConfig(
    val host: String
)

@Configuration
class IbkrConfiguration