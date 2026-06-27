package domain.stock;

public record Holding(
    double entryPrice,
    long amount
) {
    public Holding {
        if(entryPrice <= 0d) throw new IllegalArgumentException("Price");
        if(amount == 0L) throw new IllegalArgumentException("Amount");
    }
}
