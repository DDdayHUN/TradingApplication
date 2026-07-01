package domain.stock

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

//===========================================================//
/**
 * Represents the latest market quote for a stock symbol.
 * 
 * 
 * Stores readable version of data after it has been received
 * by an external Provider.
 * 
 * 
 * 
 * 
 * Example:
 * Symbol = "NET" means CloudFare stock.
 * 
 * 
 * @param symbol
 * @param currentPrice
 * @param change
 * @param percentChange
 * @param highPrice
 * @param lowPrice
 * @param openPrice
 * @param prevClosePrice
 */
//===========================================================//

data class Quote(
    val symbol: String,
    val currentPrice: Double,
    val change: Double,
    val percentChange: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val openPrice: Double,
    val prevClosePrice: Double,
    val receivedAtMillis: Long
) {
    val formattedReceivedAt = s_Formatter.format(Instant.ofEpochMilli(receivedAtMillis))

    companion object {
        private val s_Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
    }

    init {
        require(!symbol.isBlank()) { "Symbol is missing" }
        require(currentPrice >= 0.0) { "Current price is invalid" }
        require(receivedAtMillis > 0L) { "Received at is invalid" }
    }
}
