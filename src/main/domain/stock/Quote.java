package domain.stock;
/* ====================================================== */
/**
 * Represents the latest market quote for a stock symbol.
 * <p>
 *    Stores readable version of data after it has been received
 *    by an external Provider.
 * </p>
 *
 * <p>
 *    Example:
 *       Symbol = "NET" means CloudFare stock.
 * </p>
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
/* ====================================================== */

public record Quote(
  String symbol,
  double currentPrice,
  double change,
  double percentChange,
  double highPrice,
  double lowPrice,
  double openPrice,
  double prevClosePrice
) {
   public Quote{
      if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol is missing");
      if (currentPrice <= 0d) throw new IllegalArgumentException("Current price is invalid");
   }
}
