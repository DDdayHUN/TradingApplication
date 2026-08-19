package api.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import domain.adapter.AlgorithmAdapter
import domain.adapter.InstantAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GsonConfig {

    @Bean
    fun gson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(
                AlgorithmAdapter::class.java,
                AlgorithmAdapter()
            )
            .registerTypeAdapter(
                InstantAdapter::class.java,
                InstantAdapter()
            )
        .create()
    }
}