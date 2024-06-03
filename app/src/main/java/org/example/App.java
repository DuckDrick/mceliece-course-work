package org.example;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.beust.jcommander.JCommander;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Vector;
import org.example.attacks.*;
import org.example.utils.Args;
import org.example.utils.Utils;

public class App {
  private static int m = 8;
  private static int t = 20;

  private static int n = (int) Math.pow(2, m);
  private static int k = n - m * t;

  private static void recalcParameters() {
    App.m = args.m;
    App.t = args.t;
    App.n = (int) Math.pow(2, args.m);
    App.k = App.n - App.m * App.t;
  }

  public static Args args = new Args();

  public static void main(String[] argv) throws NoSuchAlgorithmException {
    JCommander.newBuilder()
            .addObject(args)
            .build()
            .parse(argv);

    int count = args.tries;

    recalcParameters();

    var times = new ArrayList<Double>();

    SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
    rnd.setSeed(args.seed.getBytes(StandardCharsets.UTF_8));


    for (int i = 0; i < count; i++) {
      var rez = new App().execute(rnd, args.attackName);
      times.add(rez / 1000000.0);
    }
    times.sort(Double::compare);

    System.out.printf("T: %d; N: %d; K: %d;\n", t, n ,k);
    System.out.println("----------TOTAL----------");
    Utils.printResults(times);

    System.out.println("----------PER-BIT----------");
    var timesPerBit = times.stream().map(a -> a / k).collect(Collectors.toList());
    Utils.printResults(timesPerBit);
  }

  public Double execute(SecureRandom random, String attackName)
          throws NoSuchAlgorithmException {

    SecureRandom rnd;
    if (random == null) {
      rnd = SecureRandom.getInstance("SHA1PRNG");
    } else {
      rnd = random;
    }

    var mcEliece = McEliece.getInstance(m, t, rnd);

    var message = new GF2Vector(mcEliece.publicKey.getK(), rnd);

    IAttack attack = switch (attackName.toLowerCase()) {
      case "mreg" -> new MessageResendErrorGeneration(mcEliece.publicKey);
      case "mri" -> new MessageResendIndependant(mcEliece.publicKey);
      case "lbc" -> new LeeBrickellCombinations(mcEliece.publicKey, args.p);
      case "lbs" -> new LeeBrickellShuffle(mcEliece.publicKey, args.p);
      default -> throw new IllegalArgumentException("No such attack type has been implemented");
    };

    attack.prepare(mcEliece, random, message);

    long start = System.nanoTime();
    String rez;

    if (args.shouldRetry) {
      do {
        rez = attack.attack();
      } while (!rez.equals(message.toString().replace(" ", "")));
    } else {
      rez = attack.attack();

      if (rez == null) {
        System.out.println("Failed LPS attack on");
        System.out.println(message);
        System.out.println(m);
        System.out.println(t);
      }
    }

    long end = System.nanoTime();

    return (double) end - (double) start;
  }
}
