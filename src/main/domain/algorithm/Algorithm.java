package domain.algorithm;

import domain.stock.History;
import utils.Pair;
import domain.stock.Holding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import data.SerializationManager;

//===========================================================//
/**
 * Abstract base class for all trading algorithms.
 * <p> Defines the required interface and provides factory methods for initializing algorithms in different modes.</p>
 */
//===========================================================//

public abstract class Algorithm {
    //===========================================================//
    //===========================================================//
    // Public Interface(s)

    /**
     * Executes the algorithm based on current holdings and market conditions.
     *
     * @param holdings      - List of currently owned assets.
     * @param allocatedToke - Amount of capital allocated for trading.
     * @param currentPrice  - Current market price of the asset.
     * @return              AlgorithmOutput containing the decision/results.
     */
    public abstract AlgorithmOutput run(final List<Holding> holdings, final double allocatedToke, final double currentPrice);

    /**
     * Updates the internal state/history of the algorithm.
     *
     * @param stock - Historical stock data to be incorporated.
     */
    public abstract void updateHistory(final History stock);

    //===========================================================//
    /**
     * Initializes an algorithm instance configured for backtesting.
     *
     * @param type     - Type of algorithm to initialize.
     * @param stockNev - Stock identifier/name.
     * @param from     - Start date (inclusive).
     * @param to       - End date (inclusive).
     * @return         Pair containing the list of history that was not used up for initialisation and the algorithm instance.
     */
    static public final Pair<List<History>, Algorithm> initForBackTest(final Type type, final String stockNev, final int from, final int to) {
        return sm_initialiser(type, Init.BACKTEST, stockNev, from, to);
    }

    //===========================================================//

    /**
     * Initializes an algorithm instance configured for live trading.
     *
     * @param type     - Type of algorithm to initialize.
     * @param stockNev - Stock identifier/name.
     * @return         Initialized algorithm instance.
     */
    static public final Algorithm initForTrading(final Type type, final String stockNev) {
        return sm_initialiser(type, Init.TRADING, stockNev, Integer.MIN_VALUE, Integer.MAX_VALUE).second;
    }

    //===========================================================//
    //===========================================================//
    // Private Interface(s)

    /**
     * Core initialization method used by both trading and backtesting setups.
     *
     * @param type     - Algorithm type.
     * @param init     - Initialization mode.
     * @param stockNev - Stock identifier/name.
     * @param from     - Start date (inclusive).
     * @param to       - End date (inclusive).
     * @return         Pair of history data and initialized algorithm.
     */
    static private final Pair<List<History>, Algorithm> sm_initialiser(final Type type, final Init init, final String stockNev, final int from, final int to) {
        try {
            final var retHistory = sm_historyInitialiser(stockNev, from, to);
            final Algorithm retAlgorithm;

            switch (type) {
                case Type.TACPP46: {
                    retAlgorithm = new TACPP46(init, retHistory);
                } break;
                default: throw new IllegalArgumentException("Type");
            }

            return new Pair<>(retHistory, retAlgorithm);
        }
        catch(IOException IO_E) { throw new IllegalArgumentException(IO_E.getMessage(), IO_E.getCause()); }
    }

    //===========================================================//
    /**
     * Loads historical data from files based on the given parameters.
     *
     * @param stockNev - Stock identifier/name.
     * @param from     - Start date (inclusive).
     * @param to       - End date (inclusive).
     * @return         List of historical data entries.
     * 
     * @throws IOException If file reading fails.
     * @throws IllegalArgumentException If the expected range of files is not fully available.
     */

    static private final List<History> sm_historyInitialiser(final String stockNev, final int from, final int to) throws IOException {
        final var backtestFiles = new File("resources/backtest/us/").listFiles();
        final List<Pair<File, Integer>> proxy = new ArrayList<>();

        for(final var file : backtestFiles) {
            final String nameWithoutExtension = file.getName().replaceFirst("\\.json$", "");
            final int date = Integer.parseInt(nameWithoutExtension.substring(nameWithoutExtension.length() - 2));
            final String nameWithoutExtensionAndDate = nameWithoutExtension.substring(0, nameWithoutExtension.length() - 2);

            if(date >= from && date <= to && nameWithoutExtensionAndDate.equals(stockNev)) {
                proxy.add(new Pair<>(file, date));
            }
        }

        
        proxy.sort(Comparator.comparingInt(h -> h.second)); // Sort chronologically by the date
        // If 'from' and/or 'to' are MIN and/or MAX then we've loaded all, so we don't throw.
        if(from != Integer.MIN_VALUE && to != Integer.MAX_VALUE && proxy.size() != (to - from + 1)) throw new IllegalArgumentException("From-To");

        final List<History> ret = new ArrayList<>();
        for(final var item : proxy) {
            final var serData = SerializationManager.loadFromFile(item.first);
            for(final var item2 : serData.stockHistory.entrySet()) {
                ret.addAll(item2.getValue());
            }
        }

        return ret;
    }

    //===========================================================//
    //===========================================================//
    // Enum(s)

    /**
     * Supported algorithm types.
     */
    static public enum Type {
        TACPP46
    }

    //===========================================================//
    /**
     * Initialization modes for the algorithm.
     */
    static protected enum Init {
        BACKTEST,
        TRADING
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    /**
     * Protected constructor to enforce initialization mode handling
     * in subclasses.
     *
     * @param init - Initialization mode.
     */
    protected Algorithm(final Init init) {}
}
