package domain.algorithm;

import java.util.ArrayList;
import java.util.List;

import domain.stock.Holding;
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

    private final double m_StartingCapital;

    private Algorithm               m_Algorithm;
    private final Algorithm.Type    m_Type;

    private final List<History> m_HistoryWeRunAgainst;
    private final List<Holding>  m_Holdings;
    private final List<Double>  m_CapitalHistory;

    private double m_Capital;

    private int m_TotalTrades   = 0;
    private int m_WinningTrades = 0;

    /*===========================================================*/
    /*===========================================================*/
    // Public Interface(s)

    public void runBackTestWithDebug() {
        runInternalBackTest();
        Display(true);
    }

    /*===========================================================*/

    public void runBackTest() {
        runInternalBackTest();
        Display(false);
    }

    /*===========================================================*/
    /*===========================================================*/
    // Private Interface(s)

    private void runInternalBackTest() {
        final var pair = Algorithm.InitForBackTest(m_Type, m_StockNev, m_From, m_To);

        // Cleanup and Init
        {
            m_Holdings.clear();
            m_CapitalHistory.clear();

            m_Capital = m_StartingCapital;

            m_Algorithm = pair.second;

            m_TotalTrades   = 0;
            m_WinningTrades = 0;

            m_CapitalHistory.add(m_StartingCapital);
        }

        for(final var history : m_HistoryWeRunAgainst) {
            final var currentPrice = history.closingPrice();
            this.runOneIteration(currentPrice);
            m_Algorithm.UpdateHistory(history);
        }
    }

    /*===========================================================*/

    private void runOneIteration(final double currentPrice) {
        final var ret = m_Algorithm.Run(m_Holdings, m_Capital, currentPrice);

        if(ret.buy() != null) {
            m_Capital -= ret.buy().amount() * currentPrice;
            m_Holdings.add(new Holding(currentPrice, ret.buy().amount()));
        }

        if(ret.sell() != null) {
            for(final var item : ret.sell().batches()) {
                final var bought = item.first;
                final var amount = item.second;
                
                if (amount > bought.amount()) throw new IllegalStateException("Sell Amount");

                m_Holdings.remove(bought);

                if(amount == bought.amount()) m_Capital += amount * currentPrice;
                else {
                    m_Capital += amount * currentPrice;
                    m_Holdings.add(new Holding(bought.entryPrice(), bought.amount() - amount));
                }

                m_TotalTrades++;
                if(currentPrice > bought.entryPrice()) m_WinningTrades++;
            }
        }

        double sum = 0d;
        for(var item : m_Holdings) sum += (currentPrice * item.amount());
        m_CapitalHistory.add(m_Capital + sum);
    }

    /*===========================================================*/

    private void Display(final boolean debug) {
        if (m_CapitalHistory.isEmpty()) throw new IllegalArgumentException("m_CapitalHistory is empty");

        final double last = m_CapitalHistory.get(m_CapitalHistory.size() - 1);
        final double profit = last - m_StartingCapital;
        final double szazalek = (profit / m_StartingCapital) * 100.0d;

        final double winrate;
        if(m_TotalTrades <= 0) winrate = Double.NaN;
        else winrate = m_WinningTrades * 100.0d / m_TotalTrades;

        System.out.println("===============================================================");
        System.out.println("Stock: " + m_StockNev + " " + "[" + Integer.toString(m_From) + "-" + Integer.toString(m_To) + "]");
        System.out.println();
        System.out.println("Total Trades Made: " + m_TotalTrades);
        System.out.println("Winrate: " + String.format("%.2f", winrate) + "%");
        System.out.println();
        System.out.println("Kezdeti Toke: " + String.format("%.2f", m_StartingCapital));
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
            for (Holding item : m_Holdings) System.out.println("  Entry Price: " + String.format("%.2f", item.entryPrice()) + " db: " + item.amount());
        }
    }

    /*===========================================================*/
    /*===========================================================*/
    // Constructor(s)

    public AlgorithmBackTester(final Algorithm.Type type, final double capital, final String stockNev, final int from, final int to) {
        m_StockNev = stockNev;
        m_From = from;
        m_To = to;

        m_StartingCapital = capital;
        m_Capital = capital;

        final var pair = Algorithm.InitForBackTest(type, m_StockNev, m_From, m_To);

        m_Algorithm = pair.second;
        m_Type = type;

        m_HistoryWeRunAgainst = pair.first;
        m_Holdings = new ArrayList<>();
        m_CapitalHistory = new ArrayList<>();
    }
}
