package org.example.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

public class CombinationGenerator implements Iterator<List<Integer>> {
  private final int n;
  private final int k;
  private int[] currentCombination;
  private boolean hasNext;

  public CombinationGenerator(int n, int k) {
    this.n = n;
    this.k = k;
    if (k > n || k <= 0) {
      hasNext = false;
    } else {
      currentCombination = IntStream.range(0, k).toArray();
      hasNext = true;
    }
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public List<Integer> next() {
    if (!hasNext) {
      throw new NoSuchElementException();
    }

    List<Integer> result = new ArrayList<>();
    for (int value : currentCombination) {
      result.add(value);
    }

    hasNext = generateNextCombination();
    return result;
  }

  private boolean generateNextCombination() {
    int i = k - 1;
    while (i >= 0 && currentCombination[i] == n - k + i) {
      i--;
    }

    if (i < 0) {
      return false;
    }

    currentCombination[i]++;
    for (int j = i + 1; j < k; j++) {
      currentCombination[j] = currentCombination[j - 1] + 1;
    }

    return true;
  }
}
