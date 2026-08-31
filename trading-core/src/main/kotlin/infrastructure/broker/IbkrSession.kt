package infrastructure.broker

import application.logging.logger
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class IbkrSession(
    private val client: IbkrClient,
    private val config: IbkrConfig
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<IbkrSession>()
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    //===========================================================//

    suspend fun getClient(): IbkrClient {
        connect()
        return client
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    @EventListener(ApplicationReadyEvent::class)
    private fun onApplicationReady(){
        scope.launch {
            try{
                connect()
            }catch(e:Exception){
                logger.error("Connecting to IB Gateway failed", e)
            }
        }
    }

    //===========================================================//

    @PreDestroy
    private fun shutdown(){
        if(!client.isConnected()) return
        client.disconnect()
    }

    //===========================================================//

    private suspend fun connect(){
        if (client.isConnected()) return

        logger.info(
            "Connecting to IBKR host={} port={} clientId={}",
            config.host,
            config.port,
            config.clientId
        )

        client.connect(
            host = config.host,
            port = config.port,
            clientId = config.clientId
        )
    }
}