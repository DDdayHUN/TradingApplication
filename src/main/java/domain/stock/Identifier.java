package domain.stock;

//===========================================================//
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
