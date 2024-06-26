package org.example.utils;

import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Vector;

import java.util.ArrayList;
import java.util.List;

public class Utils {


  public static int[] solve(int[][] matrix, int k) {
    var n = matrix.length;
    for (int i = 0; i < n; i++) {
      int q = i >> 5;
      int bitMask = 1 << (i & 0x1f);

      if (matrix[i].length > q && (matrix[i][q] & bitMask) == 0) {
        for (int j = i + 1; j < n; j++) {
          if ((matrix[j][q] & bitMask) != 0) {
            swapRows(matrix, i, j);
            j = n;
          }
        }
      }

      for (int j = n - 1; j >= 0; j--) {
        if (matrix[j].length > q && (j != i) && ((matrix[j][q] & bitMask) != 0)) {
          addToRow(matrix[i], matrix[j], q);
        }
      }
    }


    return ((GF2Matrix) new GF2Matrix(k + 1, matrix).computeTranspose()).getRow(k);
  }

  private static void swapRows(int[][] matrix, int first, int second) {
    int[] tmp = matrix[first];
    matrix[first] = matrix[second];
    matrix[second] = tmp;
  }

  private static void addToRow(int[] fromRow, int[] toRow, int startIndex) {
    for (int i = toRow.length - 1; i >= startIndex; i--) {
      toRow[i] = fromRow[i] ^ toRow[i];
    }
  }

  public static double percentile(List<Double> arr, double percentile) {
    int index = (int) Math.ceil(percentile / 100.0 * arr.size());
    return arr.get(index-1);
  }

  public static List<List<Integer>> generateCombinations(java.util.Vector<Integer> arr, int n) {
    List<List<Integer>> result = new ArrayList<>();
    combine(arr, n, 0, new ArrayList<>(), result);
    return result;
  }

  private static void combine(java.util.Vector<Integer> arr, int length, int startPosition, List<Integer> current, List<List<Integer>> result) {
    if (current.size() == length) {
      result.add(new ArrayList<>(current));
      return;
    }

    for (int i = startPosition; i <= arr.size() - length + current.size(); i++) {
      current.add(arr.get(i));
      combine(arr, length, i + 1, current, result);
      current.removeLast();
    }
  }

  public static GF2Matrix createGF2MatrixFromColumns(GF2Matrix in, List<Integer> columns) {
    var newMatrix = new int[columns.size()][];

    var inTrans = (GF2Matrix) in.computeTranspose();

    for (int i = 0; i < columns.size(); i++) {
      newMatrix[i] = inTrans.getRow(columns.get(i));
    }

    var resultTransposed = new GF2Matrix(columns.size(), newMatrix);
    return (GF2Matrix) resultTransposed.computeTranspose();
  }

  public static GF2Vector createGF2VectorFromColumns(GF2Vector in, List<Integer> columns) {
    var out = new int[(columns.size() - 1) / 32 + 1];
    var vector = in.getVecArray();

    for (int i = 0; i < columns.size(); i++) {
      setColumn(out, getColumn(vector, columns.get(i)), i);
    }
    return new GF2Vector(columns.size(), out);
  }

  private static int getColumn(int[] in, int pos) {
    var elem = pos % 32;
    var length = pos / 32;
    return (in[length] >>> elem) & 1;
  }

  private static void setColumn(int [] in, int value, int pos) {
    var elem = pos % 32;
    var length = pos / 32;
    var a = in[length];
    var el = (a >>> elem) & 1;
    if (el != value) {
      in[length] = a ^ (1 << elem);
    }
  }


  public static void printResults(List<Double> timesPerBit) {
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
}
