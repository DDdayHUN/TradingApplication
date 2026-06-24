package domain.stock;

public final class History {
    public final double closingPrice;

    public History(final double closingPrice) {
        if(closingPrice <= 0d) throw new IllegalArgumentException("ClosingPrice");

        this.closingPrice = closingPrice;
    }
}
