package api.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import domain.algorithm.ITradingAlgorithm
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GsonConfig {

    @Bean
    fun gson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(
                ITradingAlgorithm::class.java,
                ITradingAlgorithm.Adapter()
            )
        .create()
    }
}