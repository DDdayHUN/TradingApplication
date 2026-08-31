package application.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean

@Bean
inline fun <reified T> logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}