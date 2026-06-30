package infrastructure.network.finnhub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import domain.stock.Quote;
import infrastructure.network.finnhub.dto.FinnhubQuoteDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

//===========================================================//
/**
 * Client class for communicating with the Finnhub API.
 * <p>
 *    This class is responsible for HTTP communication with Finnhub.
 *    It sends requests, receives JSON response, converts the response
 *    DTO into Quote domain model
 * </p>
 */
//===========================================================//

public final class FinnhubClient {
   /*===================================================*/
   /*===================================================*/
   // Private Field(s)

   private static final Gson s_GSON = new GsonBuilder()
                                        .setPrettyPrinting()
                                        .create();

   private final FinnhubConfig m_config;
   private final HttpClient m_httpClient;

   /*===================================================*/
   /*===================================================*/
   // Public Method(es)

   /**
    * Gets the latest quote for a given stock symbol.
    *
    * @param symbol stock ticker symbol, for example "NET", "AAPL", "MSFT"
    * @return latest quote as a clean domain object
    * @throws IOException if the Finnhub request fails
    * @throws InterruptedException if the HTTP request is interrupted
    */
   public Quote getQuote(final String symbol) throws IOException, InterruptedException {
      if(symbol == null || symbol.isEmpty()) {
         throw new IllegalArgumentException("Symbol is missing");
      }

      final String encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8);
      final String url = m_config.getBaseUrl() + "/quote?symbol=" + encodedSymbol;

      final HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .timeout(m_config.getTimeout())
                                    .header("X-Finnhub-Token", m_config.getApiKey())
                                    .GET()
                                    .build();

      final HttpResponse<String> response = m_httpClient.send(
        request,
        HttpResponse.BodyHandlers.ofString()
      );

      if(response.statusCode() < 200 || response.statusCode() >= 300){
         throw new IOException(
           "Finnhub request failed. Status: " + response.statusCode()
           + " Body: " + response.body()
         );
      }

      final FinnhubQuoteDto dto = s_GSON.fromJson(
        response.body(),
        FinnhubQuoteDto.class
      );

      if(dto == null){
         throw new IOException("Finnhub returned an empty quote response");
      }

      return dto.toDomain(symbol);
   }

   /*===================================================*/
   /*===================================================*/
   // Constructor(s)

   /**
    * Initializes Finnhub client with config
    *
    * @param config Finnhub configuration API KEY, base URL and timeout
    */
   public FinnhubClient(final FinnhubConfig config){
      if (config == null) {
         throw new IllegalArgumentException("Config is missing");
      }
      this.m_config = config;
      this.m_httpClient = HttpClient.newHttpClient();
   }
}
