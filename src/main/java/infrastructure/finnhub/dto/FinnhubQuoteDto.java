package infrastructure.finnhub.dto;

import domain.stock.Quote;

//===========================================================//
/**
 *  Data Transfer Object for Finnhub quote response.
 *  <p>
 *     This record represents the raw response format returned by Finnhub.
 *  </p>
 * @param c current price
 * @param d change
 * @param dp percent change
 * @param h high price of the day
 * @param l low price of the day
 * @param o open price of the day
 * @param pc previous close price
 */
//===========================================================//

public record FinnhubQuoteDto(
  double c,
  double d,
  double dp,
  double h,
  double l,
  double o,
  double pc
){
   public Quote toDomain(final String symbol){
      return new Quote(
        symbol,
        c,
        d,
        dp,
        h,
        l,
        o,
        pc,
        System.currentTimeMillis()
      );
   }
}