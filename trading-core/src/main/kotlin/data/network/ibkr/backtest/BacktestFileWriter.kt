package data.network.ibkr.backtest

import application.logging.logger
import com.google.gson.GsonBuilder
import infrastructure.broker.IbkrHistoricalBar
import org.springframework.stereotype.Component
import java.nio.file.*

@Component
class BacktestFileWriter{

    private val logger = logger<BacktestFileWriter>()
    private val gson = GsonBuilder().setPrettyPrinting().create()
    fun save (ticker: String, bars: List<IbkrHistoricalBar>){
            val path = Path.of("trading-core/src/main/resources/backtest/ibkr/us",
                "${ticker.lowercase()}.json")

        logger.info("Writing backtest file to: {}", path.toAbsolutePath())

        Files.createDirectories(path.parent)
        Files.writeString(path, gson.toJson(bars))

        logger.info(
            "Backtest file saved: {} bars={}",
            path.toAbsolutePath(),
            bars.size
        )
    }
}