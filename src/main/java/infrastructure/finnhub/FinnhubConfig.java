package infrastructure.finnhub;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

//===========================================================//
/**
 * Configuration class for Finnhub API access.
 * <p>
 *    This Config loads API key from local .env file.
 * </p>
 */
//===========================================================//

public final class FinnhubConfig {
   /*===================================================*/
   /*===================================================*/
   // Private Field(s)

   private static final String s_ENV_FILE = ".env";
   private static final String s_FINNHUB_API_KEY_NAME = "FINNHUB_API_KEY";
   private static final String s_DEFAULT_BASE_URL = "https://finnhub.io/api/v1";
   private static final Duration s_DEFAULT_TIMEOUT = Duration.ofSeconds(10);

private final String m_ApiKey;
private final String m_BaseUrl;
private final Duration m_Timeout;

   /*===================================================*/
   /*===================================================*/
   // Public Method(es)

   public String getApiKey(){ return m_ApiKey; }
   public String getBaseUrl(){ return m_BaseUrl; }
   public Duration getTimeout(){ return m_Timeout; }

   /*===================================================*/
   /*===================================================*/
   // Private Method(es)

   /**
    *  Loads API key from .env file.
    * @return the Finnhub API key
    */
   private static String loadApiKeyFromEnv() {
      final Properties properties = new Properties();

      try(FileInputStream input = new FileInputStream(s_ENV_FILE)){
         properties.load(input);
      } catch(IOException ex){
         throw new IllegalStateException(
           "Could not load local .env file. Make sure .env file exists in the project root.",
           ex
         );
      }

      final String apiKey = properties.getProperty(s_FINNHUB_API_KEY_NAME);

      if (apiKey == null || apiKey.isBlank()){
         throw new IllegalStateException(
           "Finnhub API Key is missing"
         );
      }

      return apiKey;
   }

   /**
    * Validates Finnhub requests timeout.
    * @param timeout maximum time allowed for Finnhub API requests
    */
   private static void validateTimeout(final Duration timeout){
      if(timeout == null || timeout.isNegative() || timeout.isZero()){
         throw new IllegalArgumentException("Timeout is invalid");
      }
   }

   /**
    * Validates the Finnhub API base URL.
    * @param baseUrl base URL of the Finnhub API
    */
   private static void validateBaseUrl(final String baseUrl){
      if(baseUrl == null || baseUrl.isBlank()){
         throw new IllegalArgumentException("Base URL is missing");
      }
   }

   /*===================================================*/
   /*===================================================*/
   // Constructor(s)

   /**
    * Initializes Finnhub configuration with default values.
    */
   public FinnhubConfig(){
      this(s_DEFAULT_BASE_URL, s_DEFAULT_TIMEOUT);
   }

   /**
    * Initializes Finnhub configuration with custom timeout value.
    *
    * @param timeout maximum time allowed for Finnhub API requests.
    */
   public FinnhubConfig(final Duration timeout){
      this(s_DEFAULT_BASE_URL, timeout);
   }

   /**
    * Initializes Finnhub configuration with custom values.
    * <p>
    *    Api key is still loaded from local .env file.
    * </p>
    * @param baseUrl base URL of the Finnhub API
    * @param timeout maximum time allowed for Finnhub API requests
    */
   public FinnhubConfig(String baseUrl, Duration timeout){
      validateBaseUrl(baseUrl);
      validateTimeout(timeout);
      this.m_apiKey = loadApiKeyFromEnv();
      this.m_baseUrl = baseUrl;
this.m_ApiKey = loadApiKeyFromEnv();
this.m_BaseUrl = baseUrl;
this.m_Timeout = timeout;
   }

}


