package infrastructure.broker

import application.logging.logger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val connectionMutex = Mutex()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun getClient(): IbkrClient {
        if(client.isConnected()) return client
        connectionMutex.withLock {
            if(client.isConnected()) return client

            logger.info("Creating IBKR session host={} port={} clientId={}",
                config.host, config.port, config.clientId)

            client.connect(
                host = config.host,
                port = config.port,
                clientId = config.clientId
            )
        }
        return client
    }

    fun disconnect(){
        client.disconnect()
    }
}