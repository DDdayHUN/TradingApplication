package infrastructure.network.finnhub;

import java.io.IOException;

public final class FinnhubTester {
   private final FinnhubConfig m_config;
   private final FinnhubClient m_client;
   private final FinnhubMarketDataProvider m_provider;

   public void runFinnhubTester(String symbol) throws IOException, InterruptedException {
      try{
         var quote = m_provider.getQuote(symbol);

         System.out.println("Symbol: " + quote.symbol());
         System.out.println("Current Price: " + quote.currentPrice());
         System.out.println("Change: " + quote.change());
         System.out.println("Percent Change: " + quote.percentChange() + "%");
         System.out.println("High: " + quote.highPrice());
         System.out.println("Low: " + quote.lowPrice());
         System.out.println("Open: " + quote.openPrice());
         System.out.println("Previous Close: " + quote.prevClosePrice());
         System.out.println("Received at: " + quote.getFormattedReceivedAt());
      } catch (Exception ex){
         ex.printStackTrace();
      }
   }

   public FinnhubTester() {
      this.m_config = new FinnhubConfig();
      this.m_client = new FinnhubClient(m_config);
      this.m_provider = new FinnhubMarketDataProvider(this.m_client);
   }

}
