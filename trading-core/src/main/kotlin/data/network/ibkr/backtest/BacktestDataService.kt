package data.network.ibkr.backtest

import application.service.broker.InteractiveBrokersService
import domain.market.security.SecurityIdentifier
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.time.Instant

@Service
class BacktestDataService(
    private val brokerService: InteractiveBrokersService,
    private val fileWriter: BacktestFileWriter,
) {

    suspend fun download(userId: UUID, securityIdentifier: SecurityIdentifier, from: Instant, to: Instant){
        val bars = brokerService.getHistoricalData(
            userId = userId,
            securityIdentifier = securityIdentifier,
            from = from,
            to = to
        )

        val cleanedBars = bars
            .distinctBy { bar -> bar.timestamp }
            .sortedBy { bar -> bar.timestamp}

        fileWriter.save(securityIdentifier.tickerSymbol, cleanedBars)
    }
}