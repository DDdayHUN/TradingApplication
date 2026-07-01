package domain.stock;

//===========================================================//
/**
 * Represents a financial instrument identifier consisting of an ISIN,
 * an exchange code, and a ticker symbol.
 *
 * The ISIN provides a globally unique identifier for the security,
 * while the exchange and symbol define its specific listing context.
 *
 * @param isin - the 12-character International Securities Identification Number (ISIN).
 * @param mic- the market identifier code where the instrument is traded.
 * @param tickerSymbol - the ticker symbol of the instrument on the given exchange.
 */
//===========================================================//

public record Identifier(
    String isin,
    String mic,
    String tickerSymbol,
    String currency
) {
    public Identifier {
        if(isin == null || !isin.matches("[A-Za-z]{2}[A-Za-z0-9]{9}[0-9]")) throw new IllegalArgumentException("ISIN");
        if(mic == null || !mic.matches("[A-Za-z0-9]{4}")) throw new IllegalArgumentException("MIC");
        if(tickerSymbol == null || tickerSymbol.isBlank()) throw new IllegalArgumentException("Ticker Symbol");
        if(currency == null || !currency.matches("[A-Za-z]{3}")) throw new IllegalArgumentException("Currency");
    }
}
