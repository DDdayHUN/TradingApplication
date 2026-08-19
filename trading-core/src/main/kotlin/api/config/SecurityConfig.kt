package api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.Customizer
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity
    ): SecurityFilterChain {
       http
           .cors(Customizer.withDefaults())
           .csrf { csrf ->
               csrf.disable()
           }
           .sessionManagement { session ->
               session.sessionCreationPolicy(
                   SessionCreationPolicy.STATELESS
               )
           }
           // AUTHORIZATION, most meg nem kell
           // .authorizeHttpRequests { authorization ->
           //    authorization
           //        .requestMatchers("/api/public/**")
           //        .permitAll()
           //        .anyRequest()
           //        .authenticated()
           //}
           .oauth2ResourceServer { resourceServer ->
               resourceServer.jwt(Customizer.withDefaults())
           }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf(
            "http://localhost:5173"
        )

        configuration.allowedMethods = listOf(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        )
        configuration.allowedHeaders = listOf(
            "Authorization",
            "Content-Type"
        )
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }
}
