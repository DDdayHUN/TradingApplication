package data.infrastructure.broker

import application.logging.logger
import java.util.*

class InteractiveBrokersSession(
    val userId: UUID,
    private val client: IbkrClient,
    private val host: String,
    private val port: Int,
    private val clientId: Int,
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val logger = logger<InteractiveBrokersSession>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

     suspend fun connect(){
        if (client.isConnected()) return

        client.connect(
            host = host,
            port = port,
            clientId = clientId
        )
    }

    fun disconnect() {
        if (!client.isConnected()) return

        logger.info("Disconnecting user={} from IBKR", userId)
        client.disconnect()
    }

    fun getClient(): IbkrClient {
        if (!client.isConnected())  {throw IllegalStateException("IBKR session is not connected") }
        return client
    }

    fun isConnected(): Boolean = client.isConnected()
}