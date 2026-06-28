package infrastructure.finnhub;

import domain.stock.Quote;
import infrastructure.finnhub.interfaces.IMarketDataProvider;

import java.io.IOException;

/**
 * Provider implementation that gets quote data from Finnhub.
 */
public final class FinnhubMarketDataProvider implements IMarketDataProvider {
   private final FinnhubClient m_client;

   public FinnhubMarketDataProvider(final FinnhubClient client) {
      if(client == null){
         throw new IllegalArgumentException("Finnhub client is missing");
      }
      this.m_client = client;
   }
   @Override
   public Quote getQuote(String symbol) throws IOException, InterruptedException {
      return m_client.getQuote(symbol);
   }
}
