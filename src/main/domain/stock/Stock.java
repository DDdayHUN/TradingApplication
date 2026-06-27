package domain.stock;

public record Stock(
    String uuid, 
    String isin, 
    String symbol
) {}
