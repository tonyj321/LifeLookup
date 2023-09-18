package com.jaws.lifelookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tonyj
 */
public class QuadLifeLookup {

    public static void main(String[] args) {
        List<Integer> results = new ArrayList();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    results.add(compute(i, j, k));
                }
            }
        }
        System.out.println(results.size());
        System.out.println(results);
        int[] counts = new int[4];
        for (int r : results) {
            counts[r]++;
        }
        System.out.println(Arrays.toString(counts));
        List<Short> packed = new ArrayList();
        // Pack the results into ints -- 9*3 per int
        int l = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int pack = 0;
                for (int k = 0; k < 4; k++) {
                    pack = (pack << 2) + results.get(l + (3 - k));
                }
                packed.add((short) pack);
                l += 4;
            }
        }
        System.out.println(packed.size());
        System.out.println(packed);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    final int cc = compute(i, j, k);
                    final int ll = lookup(packed, i, j, k);
                    if (cc != ll) {
                        System.out.printf("Error %d %d %d %d %d\n", i, j, k, cc, ll);
                    }
                }
            }
        }
    }

    private static int lookup(List<Short> packed, int... neighborIndexes) {
        int index = (neighborIndexes[0] << 2) + neighborIndexes[1];
        int pack = packed.get(index);
        return (pack >> (2 * neighborIndexes[2])) & 3;
    }

    private static int compute(int... neighborIndexes) {
        int[] count = new int[4];

        for (int i = 0; i < 3; i++) {
            int index = neighborIndexes[i];
            if (++count[index] > 1) {
                return index;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (count[i] == 0) {
                return i;
            }
        }
        // Logically, we can't get here, but the compiler does not know that
        return -1;
    }
}
