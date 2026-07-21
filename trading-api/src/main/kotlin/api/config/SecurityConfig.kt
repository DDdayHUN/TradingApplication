package api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.Customizer


@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity
    ): SecurityFilterChain {
       http
           .csrf { csrf ->
               csrf.disable()
           }
           .sessionManagement { session ->
               session.sessionCreationPolicy(
                   SessionCreationPolicy.STATELESS
               )
           }
           .authorizeHttpRequests { authorization ->
               authorization
                   .requestMatchers("/api/public/**")
                   .permitAll()
                   .anyRequest()
                   .authenticated()
           }
           .oauth2ResourceServer { resourceServer ->
               resourceServer.jwt(Customizer.withDefaults())
           }
        return http.build()
    }
}