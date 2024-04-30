package org.example;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Vector;

public class App {
  private static int m = 6;
  private static int t = 9;

  private static int n = (int) Math.pow(2, m);
  private static int k = n - m * t;

  private static String attack = "one";

  private static void recalcParameters(int m, int t) {
    App.m = m;
    App.t = t;
    App.n = (int) Math.pow(2, m);
    App.k = n - m * t;
  }
  public static void main(String[] args) throws NoSuchAlgorithmException {

    int count = args.length > 1 ? Integer.parseInt(args[2]) : 100;

    attack = args.length > 2 ? args[3] : attack;

    if (args.length > 0) {
      recalcParameters(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
    var times = new ArrayList<Double>();


    SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
    rnd.setSeed("SomeRandom".getBytes(StandardCharsets.UTF_8));

    for (int i = 0; i < count; i++) {
      var rez = new App().execute(rnd);
      times.add(rez.first / 1000000.0);
    }

    times.sort(Double::compare);

    System.out.printf("Tries: %d\n", count);
    System.out.printf("T: %d; N: %d; K: %d;\n", t, n ,k);
    System.out.println("----------TOTAL----------");
    printResults(times);

    System.out.println("----------PER-BIT----------");
    var timesPerBit = times.stream().map(a -> a / k).collect(Collectors.toList());
    printResults(timesPerBit);
  }

  private static void printResults(List<Double> timesPerBit) {
    System.out.printf("Average (ms): %f\n", timesPerBit.stream().mapToDouble(a -> a).average().orElse(0));

    System.out.printf("Percentiles - 25: %f; 50: %f; 75: %f; 90: %f; 95: %f; 99: %f;\n",
            Utils.percentile(timesPerBit, 25),
            Utils.percentile(timesPerBit, 50),
            Utils.percentile(timesPerBit, 75),
            Utils.percentile(timesPerBit, 90),
            Utils.percentile(timesPerBit, 95),
            Utils.percentile(timesPerBit, 99)
    );
  }

  public Tuple<Double, long[]> execute(SecureRandom random)
          throws NoSuchAlgorithmException {

    SecureRandom rnd;
    if (random == null) {
      rnd = SecureRandom.getInstance("SHA1PRNG");
    } else {
      rnd = random;
    }

    var mcEliece = McEliece.getInstance(m, t, rnd);

    var e1 = new GF2Vector(mcEliece.publicKey.getN(), mcEliece.publicKey.getT(), rnd);
    var e2 = new GF2Vector(mcEliece.publicKey.getN(), mcEliece.publicKey.getT(), rnd);
    var message = new GF2Vector(mcEliece.publicKey.getK(), rnd);

    var c1 = (GF2Vector) mcEliece.encrypt(message, e1);
    var c2 = (GF2Vector) mcEliece.encrypt(message, e2);

    long start = System.nanoTime();

    var msgResend = new MessageResend(mcEliece.publicKey, c1, c2);
    Tuple<String, Integer> rez;
    if (attack.equalsIgnoreCase("two")) {
      do {
        rez = msgResend.attack2();

      } while (!rez.first.equals(message.toString().replace(" ", "")));
    } else {
      rez = msgResend.attack();
    }
    long end = System.nanoTime();

    if (!rez.first.equals(message.toString().replace(" ", ""))) {
      System.out.println(rez.first);
      System.out.println(message.toString().replace(" ", ""));
      System.out.println("Failed to find solutions");
      return new Tuple<>(0d, new long[]{1,0});
    }

    return new Tuple<>((double) (end - start), new long[]{rez.second, (long) 0});
  }
}
