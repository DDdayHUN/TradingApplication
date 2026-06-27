package domain.algorithm;

import java.util.List;

import domain.stock.Holding;
import utils.Pair;

public record AlgorithmOutput(
    Buy buy,
    Sell sell
) {
    static public record Buy(
        long amount
    ) {}

    static public record Sell(
        List<Pair<Holding, Long>> batches
    ) {}
}
