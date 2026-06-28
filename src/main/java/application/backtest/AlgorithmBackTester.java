package application.backtest;

import java.util.ArrayList;
import java.util.List;

import domain.algorithm.Algorithm;
import domain.stock.Holding;
import domain.tax.Taxation;
import utils.Pair;
import domain.stock.History;

//===========================================================//
/**
 * AlgorithmBackTester is responsible for simulating and evaluating a trading algorithm
 * over historical market data.
 *
 * <p>It runs a specified {@link Algorithm} over a defined time range for a given stock,
 * tracks virtual holdings, capital changes, and performance metrics such as total trades and win rate.</p>
 *
 * <p>The backtester supports both normal execution and debug execution, where additional
 * internal state (such as current holdings) is printed for inspection.</p>
 *
 * <p>This class is immutable in configuration (stock, range, initial capital, algorithm type),
 * but maintains mutable state during backtesting execution.</p>
 */
//===========================================================//

public final class AlgorithmBackTester {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private final String m_StockNev;
    private final int m_From;
    private final int m_To;

    private final double m_StartingCapital;
    private final Algorithm.Type m_Type;

    private final BackTesterWithTaxationContext m_WithoutTax;
    private final BackTesterWithTaxationContext m_WithTax;

    //===========================================================//
    //===========================================================//
    // Public Interface(s)

    public void runBackTestWithDebug() {
        m_WithoutTax.reset(Algorithm.initForBackTest(m_Type, m_StockNev, m_From, m_To));
        m_WithTax.reset(Algorithm.initForBackTest(m_Type, m_StockNev, m_From, m_To));
        m_WithoutTax.runBackTest();
        m_WithTax.runBackTest();
        display(true);
    }

    //===========================================================//

    public void runBackTest() {
        m_WithoutTax.reset(Algorithm.initForBackTest(m_Type, m_StockNev, m_From, m_To));
        m_WithTax.reset(Algorithm.initForBackTest(m_Type, m_StockNev, m_From, m_To));
        m_WithoutTax.runBackTest();
        m_WithTax.runBackTest();
        display(false);
    }

    //===========================================================//
    //===========================================================//
    // Private Interface(s)

    private void display(final boolean debug) {
        System.out.println("===============================================================");
        System.out.println("Stock: " + m_StockNev + " " + "[" + Integer.toString(m_From) + "-" + Integer.toString(m_To) + "]");
        System.out.println("Kezdeti Toke: " + String.format("%.2f", m_StartingCapital) + System.lineSeparator());
        m_WithoutTax.display();
        if(debug) m_WithoutTax.displayDebugInfo();
        m_WithTax.display();
        if(debug) m_WithTax.displayDebugInfo();
        System.out.println("===============================================================");
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    public AlgorithmBackTester(final Taxation taxation, final Algorithm.Type type, final double capital, final String stockNev, final int from, final int to) {
        m_StockNev = stockNev;
        m_From = from;
        m_To = to;
        
        m_StartingCapital = capital;
        m_Type = type;

        m_WithoutTax = new BackTesterWithTaxationContext(null, Algorithm.initForBackTest(type, stockNev, from, to), capital);
        m_WithTax = new BackTesterWithTaxationContext(taxation, Algorithm.initForBackTest(type, stockNev, from, to), capital);
    }

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    static private final class BackTesterWithTaxationContext {
        //===========================================================//
        //===========================================================//
        // Private Field(s)

        private final Taxation m_Taxation;

        private Algorithm m_Algorithm;
        private List<History> m_HistoryWeRunAgainst;

        private final List<Holding> m_Holdings;
        private final List<Double> m_CapitalHistory;

        private final double m_StartingCapital;
        private double m_CurrentCapital;

        private int m_TotalTrades = 0;
        private int m_WinningTrades = 0;

        //===========================================================//
        //===========================================================//
        // Public Interface(s)

        public void runBackTest() {
            for(final var history : m_HistoryWeRunAgainst) {
                final var currentPrice = history.closingPrice();
                runOneIteration(currentPrice);
                m_Algorithm.updateHistory(history);
            }
        }

        //===========================================================//

        public void reset(final Pair<List<History>, Algorithm> pair) {
            m_Algorithm = pair.second;
            m_HistoryWeRunAgainst = pair.first;

            m_Holdings.clear();
            m_CapitalHistory.clear();

            m_CurrentCapital = m_StartingCapital;
            m_TotalTrades = 0;
            m_WinningTrades = 0;
        }

        //===========================================================//

        public void display() {
            if (m_CapitalHistory.isEmpty()) throw new IllegalArgumentException("m_CapitalHistory is empty");

            final double last = m_CapitalHistory.get(m_CapitalHistory.size() - 1);
            final double profit = last - m_StartingCapital;
            final double szazalek = (profit / m_StartingCapital) * 100.0d;

            final double winrate;
            if(m_TotalTrades <= 0) winrate = Double.NaN;
            else winrate = m_WinningTrades * 100.0d / m_TotalTrades;
            
            if(m_Taxation == null) System.out.println("With Taxes:");
            else System.out.println("Without Taxes:");

            System.out.println("    Profit: " + String.format("%.2f", profit));
            System.out.println("    Return: " + String.format("%.2f", szazalek) + "%");
            System.out.println();
            
            System.out.println("    Total Trades Made: " + m_TotalTrades);
            System.out.println("    Winrate: " + String.format("%.2f", winrate) + "%");
            System.out.println("    Sharpe Ratio: " + String.format("%.2f", utils.Math.sharpeRatio(m_CapitalHistory, 0.03d)));
            System.out.println();
        }

        //===========================================================//

        public void displayDebugInfo() {
            System.out.println("    DEBUG:");
            System.out.print("  Holding: ");
            if (m_Holdings.isEmpty()) System.out.println("None");
            else {
                System.out.println();
                for (Holding item : m_Holdings) System.out.println("        Entry Price: " + String.format("%.2f", item.entryPrice()) + " db: " + item.amount());
            }
        }

        //===========================================================//
        //===========================================================//
        // Private Interface(s)

        private void runOneIteration(final double currentPrice) {
            final var ret = m_Algorithm.run(m_Holdings, m_CurrentCapital, currentPrice);

            if(ret.buy() != null) {
                m_CurrentCapital -= ret.buy().amount() * currentPrice;
                m_Holdings.add(new Holding(currentPrice, ret.buy().amount()));
            }

            if(ret.sell() != null) {
                for(final var item : ret.sell().batches()) {
                    final var bought = item.first;
                    final var amount = item.second;
                    
                    if (amount > bought.amount()) throw new IllegalStateException("Sell Amount");

                    m_Holdings.remove(bought);

                    if(amount == bought.amount()) m_CurrentCapital += amount * currentPrice;
                    else {
                        m_CurrentCapital += amount * currentPrice;
                        m_Holdings.add(new Holding(bought.entryPrice(), bought.amount() - amount));
                    }

                    m_TotalTrades++;
                    if(currentPrice > bought.entryPrice()) m_WinningTrades++;
                }
            }

            double sum = 0d;
            for(var item : m_Holdings) sum += (currentPrice * item.amount());
            m_CapitalHistory.add(m_CurrentCapital + sum);
        }

        //===========================================================//
        //===========================================================//
        // Constructor(s)

        public BackTesterWithTaxationContext(final Taxation taxation, final Pair<List<History>, Algorithm> pair, final double capital) {
            if(pair == null) throw new IllegalArgumentException("Pair");
            if(capital <= 0d) throw new IllegalArgumentException("Capital");

            m_Taxation = taxation;
            m_StartingCapital = capital;
            m_CurrentCapital = capital;

            m_Algorithm = pair.second;

            m_HistoryWeRunAgainst = pair.first;
            m_Holdings = new ArrayList<>();
            m_CapitalHistory = new ArrayList<>();
        }
    }
}
