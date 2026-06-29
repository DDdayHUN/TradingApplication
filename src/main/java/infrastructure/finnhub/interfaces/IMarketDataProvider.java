package infrastructure.finnhub.interfaces;

import domain.stock.Quote;

import java.io.IOException;

//===========================================================//
/**
 * Provides market data to the application
 * <p>
 *    Implementations can load data from Finnhub
 * </p>
 */
//===========================================================//

public interface IMarketDataProvider {
   Quote getQuote(final String symbol) throws IOException, InterruptedException;
}
