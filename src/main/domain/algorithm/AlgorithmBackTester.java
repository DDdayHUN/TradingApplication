package domain.algorithm;

import java.util.ArrayList;
import java.util.List;

import domain.stock.Bought;
import domain.stock.History;

/*===========================================================*/
/*===========================================================*/

public final class AlgorithmBackTester {
    /*===========================================================*/
    /*===========================================================*/
    // Private Field(s)

    private final String    m_StockNev;
    private final int       m_From;
    private final int       m_To;

    private final double    m_KezdetiToke;

    private Algorithm               m_Algorithm;
    private final Algorithm.Type    m_Type;

    private final List<History> m_HistoryWeRunAgainst;
    private final List<Bought>  m_Holdings;
    private final List<Double>  m_CapitalHistory;

    private double m_Toke;

    private int m_TotalTrades   = 0;
    private int m_WinningTrades = 0;

    /*===========================================================*/
    /*===========================================================*/
    // Public Interface(s)

    public void runBackTestWithDebug() {
        internal_runBackTest();
        Display(true);
    }

    /*===========================================================*/

    public void runBackTest() {
        internal_runBackTest();
        Display(false);
    }

    /*===========================================================*/
    /*===========================================================*/
    // Private Interface(s)

    private void internal_runBackTest() {
        final var pair = Algorithm.InitForBackTest(m_Type, m_StockNev, m_From, m_To);

        // Cleanup and Init
        {
            m_Holdings.clear();
            m_CapitalHistory.clear();

            m_Toke = m_KezdetiToke;

            m_Algorithm = pair.second;

            m_TotalTrades   = 0;
            m_WinningTrades = 0;

            m_CapitalHistory.add(m_KezdetiToke);
        }

        for(final var history : m_HistoryWeRunAgainst) {
            final var currentPrice = history.closingPrice;
            this.runOneIteration(currentPrice);
            m_Algorithm.UpdateHistory(history);
        }
    }

    /*===========================================================*/

    private void runOneIteration(final double currentPrice) {
        final var ret = m_Algorithm.Run(m_Holdings, m_Toke, currentPrice);

        if(ret.buy != null) {
            m_Toke -= ret.buy.amount * currentPrice;
            m_Holdings.add(new Bought(currentPrice, ret.buy.amount));
        }

        if(ret.sell != null) {
            for(final var item : ret.sell.batches) {
                final var bought = item.first;
                final var amount = item.second;
                
                if (amount > bought.amount) throw new IllegalStateException("Sell Amount");

                if(amount == bought.amount) {
                    m_Toke += amount * currentPrice;
                    m_Holdings.remove(bought);
                }
                else {
                    m_Toke += amount * currentPrice;
                    bought.amount -= amount;
                }

                m_TotalTrades++;
                if(currentPrice > bought.price) m_WinningTrades++;
            }
        }

        double sum = 0d;
        for(var item : m_Holdings) sum += (currentPrice * item.amount);
        m_CapitalHistory.add(m_Toke + sum);
    }

    /*===========================================================*/

    private void Display(final boolean debug) {
        if (m_CapitalHistory.isEmpty()) throw new IllegalArgumentException("m_CapitalHistory is empty");

        final double last = m_CapitalHistory.get(m_CapitalHistory.size() - 1);
        final double profit = last - m_KezdetiToke;
        final double szazalek = (profit / m_KezdetiToke) * 100.0d;

        final double winrate;
        if(m_TotalTrades <= 0) winrate = Double.NaN;
        else winrate = m_WinningTrades * 100.0d / m_TotalTrades;

        System.out.println("===============================================================");
        System.out.println("Stock: " + m_StockNev + " " + "[" + Integer.toString(m_From) + "-" + Integer.toString(m_To) + "]");
        System.out.println();
        System.out.println("Total Trades Made: " + m_TotalTrades);
        System.out.println("Winrate: " + String.format("%.2f", winrate) + "%");
        System.out.println();
        System.out.println("Kezdeti Toke: " + String.format("%.2f", m_KezdetiToke));
        System.out.println("Profit: " + String.format("%.2f", profit));
        System.out.println("Return: " + String.format("%.2f", szazalek) + "%");
        System.out.println();
        System.out.println("Sharpe Ratio: " + String.format("%.2f", utils.Math.sharpeRatio(m_CapitalHistory, 0.03d)));

        if(!debug) { System.out.println(); return;}

        System.out.println(System.lineSeparator() + "DEBUG:");
        System.out.print("Holding: ");
        if (m_Holdings.isEmpty()) System.out.println("None");
        else {
            System.out.println();
            for (Bought item : m_Holdings) System.out.println("  Price: " + String.format("%.2f", item.price) + " db: " + item.amount);
        }
    }

    /*===========================================================*/
    /*===========================================================*/
    // Constructor(s)

    public AlgorithmBackTester(final Algorithm.Type type, final double toke, final String stockNev, final int from, final int to) {
        m_StockNev = stockNev;
        m_From = from;
        m_To = to;

        m_KezdetiToke = toke;
        m_Toke = toke;

        final var pair = Algorithm.InitForBackTest(type, m_StockNev, m_From, m_To);

        m_Algorithm = pair.second;
        m_Type = type;

        m_HistoryWeRunAgainst = pair.first;
        m_Holdings = new ArrayList<>();
        m_CapitalHistory = new ArrayList<>();
    }
}
