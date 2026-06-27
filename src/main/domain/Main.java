package domain;
// Hello Wrold :) System.out.println("Hello, Wrold!");

import domain.algorithm.Algorithm;
import application.backtest.AlgorithmBackTester;
//import infrastructure.finnhub.FinnhubClient;
//import infrastructure.finnhub.FinnhubConfig;
//import infrastructure.finnhub.FinnhubTester;

import java.io.IOException;

public final class Main {
    static public void main(String[] args) throws IOException, InterruptedException {
        System.out.println("#==========================================#");
        System.out.println(System.lineSeparator() + "### Internal testing ###" + System.lineSeparator());
        final var BT = new AlgorithmBackTester(Algorithm.Type.TACPP46, 10000d, "Cloudflare", 20, 24);
        BT.runBackTest();
        System.out.println("#==========================================#");
        //System.out.println("\n### Finnhub ###\n");
        //final var FT = new FinnhubTester();
        //FT.runFinnhubTester("AAPL");
    }
}
