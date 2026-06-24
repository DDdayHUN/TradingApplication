package domain;
// Hello Wrold :) System.out.println("Hello, Wrold!");

import domain.algorithm.Algorithm;
import domain.algorithm.AlgorithmBackTester;

public final class Main {
    static public void main(String[] args) {
        final var BT = new AlgorithmBackTester(Algorithm.Type.TACPP46, 10000d, "Cloudflare", 20, 24);
        BT.runBackTestWithDebug();
    }
}
