package domain.stock;

public record History(
    double closingPrice
) {
    public History {
        if(closingPrice <= 0d) throw new IllegalArgumentException("ClosingPrice");
    }
}
