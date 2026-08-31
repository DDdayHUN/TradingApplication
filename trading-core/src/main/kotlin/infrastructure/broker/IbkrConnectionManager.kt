package infrastructure.broker

import org.springframework.stereotype.Component
import application.logging.logger
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

@Component
class IbkrConnectionManager(
    private val session: IbkrSession
) {
    private val logger = logger<IbkrConnectionManager>()
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        scope.launch {
            try{
                logger.info("Spring started, connecting to IB Gateway")

                session.connect()
            }catch(e: Exception){
                logger.error("Could not connect to IB Gateway")
                throw e
            }
        }
    }

    @PreDestroy
     fun shutdown() {
        session.disconnect()
    }
}