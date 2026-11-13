package api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EntityScan("data")
@EnableJpaRepositories("data")
@SpringBootApplication(
    scanBasePackages = [
        "api",
        "application",
        "data"
    ]
)
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan(
    basePackages = [
        "api",
        "application",
        "data"
    ]
)
class SpringMain

fun main(args: Array<String>){
    runApplication<SpringMain>(*args)
}