package infrastructure.broker

import application.logging.logger
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

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun connect(){
        if (client.isConnected()) return

        logger.info(
            "Connecting to IBKR host={} port={} clientId={}",
            config.host,
            config.port,
            config.clientId
        )

        client.connect(
            config.host,
            config.port,
            config.clientId
        )
    }

    suspend fun getClient(): IbkrClient {
        connect()
        return client
    }

     fun disconnect(){
        if(!client.isConnected()) return
        client.disconnect()
    }
}