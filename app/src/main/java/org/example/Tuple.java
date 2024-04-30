package org.example;

public class Tuple<N, K> {
  public final N first;
  public final K second;

  public Tuple(N a, K b) {
    this.first = a;
    this.second = b;
  }
}
