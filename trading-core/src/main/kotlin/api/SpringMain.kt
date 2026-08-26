package api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EntityScan("data.repository")
@EnableJpaRepositories("data.repository")
@SpringBootApplication(
    scanBasePackages = [
        "api",
        "application",
        "data",
        "infrastructure"
    ]
)
class SpringMain

fun main(args: Array<String>){
    runApplication<api.SpringMain>(*args)
}