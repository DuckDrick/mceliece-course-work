package org.example.attacks;

import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePublicKeyParameters;
import org.example.App;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class LeeBrickellShuffle extends LeeBrickell {

  public LeeBrickellShuffle(McEliecePublicKeyParameters publicKey, int p) {
    super(publicKey, p);
  }

  public String attack() {

    var tries = App.args.lbsTries;
    while (tries --> 0) {
      List<Integer> range = new java.util.ArrayList<>(IntStream.range(0, n).boxed().toList());
      Collections.shuffle(range);
      var combination = range.subList(0, k);

      var res = innerAttack(combination);
      if (!res.isBlank()) {
        return res;
      }
    }
    return null;
  }
}
