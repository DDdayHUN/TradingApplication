package data.infrastructure.broker.ibkr

import application.logging.logger
import java.util.*

class InteractiveBrokersSession(
    val userId: UUID,
    val clientId: Int,
    private val client: IbkrClient,
    private val host: String,
    private val port: Int,
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Logger = logger<InteractiveBrokersSession>()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

     suspend fun connect() {
        if (client.isConnected()) return

        client.connect(
            host = host,
            port = port,
            clientId = clientId
        )
    }

    //===========================================================//

    fun disconnect() {
        if (!client.isConnected()) return

        m_Logger.info("Disconnecting user from IBKR with ID:{${userId}} ")

        client.disconnect()
    }

    //===========================================================//

    fun getClient(): IbkrClient {
        if (!client.isConnected())  { throw IllegalStateException("IBKR session is not connected") }
        return client
    }

    //===========================================================//

    fun isConnected(): Boolean = client.isConnected()
}