package application.runner

import application.tester.TradingAlgorithmBackTester
import domain.interfaces.ILogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Deprecated("This class will be separated later for individual runners.")
class TaskRunner {

    private val m_Logger: ILogger

    suspend fun runBacktestOnOneSecurity(config: RunConfig){
        withContext(Dispatchers.Default){
            val output = TradingAlgorithmBackTester(
                config.algorithm,
                config.identifier,
                config.startCapital,
                config.taxation,
                config.startDate,
                config.endDate
            ).runBackTest()

            m_Logger.log(output.displayToLogger())
        }
    }






    constructor(logger: ILogger) {
        m_Logger = logger
    }

}