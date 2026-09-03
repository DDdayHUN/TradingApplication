package data.network.ibkr.backtest

import application.service.broker.IBrokerService
import domain.market.security.SecurityIdentifier
import org.springframework.stereotype.Service
import kotlin.time.Instant

@Service
class BacktestDataService(
    private val brokerService: IBrokerService,
    private val fileWriter: BacktestFileWriter,
) {

    suspend fun download(securityIdentifier: SecurityIdentifier, from: Instant, to: Instant){
        val bars = brokerService.getHistoricalData(
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