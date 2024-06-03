package org.example.attacks;

import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Vector;
import org.example.McEliece;

import java.security.SecureRandom;

public interface IAttack {
  void prepare(McEliece mcEliece, SecureRandom secureRandom, GF2Vector message);
  String attack();
}
