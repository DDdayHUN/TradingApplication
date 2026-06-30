package domain;
// Hello Wrold :) System.out.println("Hello, Wrold!");

import domain.algorithm.Algorithm;
import domain.tax.Taxation;
import application.backtest.AlgorithmBackTester;
//import infrastructure.finnhub.FinnhubClient;
//import infrastructure.finnhub.FinnhubConfig;
//import infrastructure.finnhub.FinnhubTester;

import java.io.IOException;

public final class Main {
    static public void main(String[] args) throws IOException, InterruptedException {
        final var BT = new AlgorithmBackTester(Taxation.HUNGARY, Algorithm.Type.TACPP46, 10000d, "Cloudflare", 20, 24);
        BT.runBackTest();
        System.out.println("#==========================================#");
        //System.out.println("\n### Finnhub ###\n");
        //final var FT = new FinnhubTester();
        //FT.runFinnhubTester("AAPL");
    }
}
