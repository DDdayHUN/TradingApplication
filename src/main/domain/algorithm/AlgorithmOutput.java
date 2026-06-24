package domain.algorithm;

import java.util.List;

import domain.stock.Bought;
import utils.Pair;

public final class AlgorithmOutput {
    public final Buy buy;
    public final Sell sell;

    static public final class Buy {
        public final long amount;

        public Buy(final long amount) {
            this.amount = amount;
        }
    }

    static public final class Sell {
        public final List<Pair<Bought, Long>> batches;

        public Sell(final List<Pair<Bought, Long>> batches) {
            this.batches = batches;
        }
    }

    public AlgorithmOutput(final Buy buy, final Sell sell) {
        this.buy = buy;
        this.sell = sell;
    }
}
