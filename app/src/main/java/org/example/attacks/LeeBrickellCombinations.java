package org.example.attacks;

import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePublicKeyParameters;
import org.example.utils.CombinationGenerator;

public class LeeBrickellCombinations extends LeeBrickell {

  public LeeBrickellCombinations(McEliecePublicKeyParameters publicKey, int p) {
    super(publicKey, p);
  }

  @Override
  public String attack() {
    var generator = new CombinationGenerator(n, k);

    while (generator.hasNext()) {
      var combination = generator.next();
      var res = innerAttack(combination);
      if (!res.isBlank()) {
        return res;
      }
    }
    return null;
  }
}
