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
 * @param exchange - the exchange code where the instrument is traded.
 * @param symbol - the ticker symbol of the instrument on the given exchange.
 */
//===========================================================//

public record Identifier(
    String isin,
    String exchange,
    String symbol
) {
    public Identifier {
        if(isin.length() != 12) throw new IllegalArgumentException("ISIN");
        if(exchange.isBlank()) throw new IllegalArgumentException("Exchange");
        if(symbol.isBlank()) throw new IllegalArgumentException("Symbol");
    }
}
