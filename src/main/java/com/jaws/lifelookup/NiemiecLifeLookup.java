package com.jaws.lifelookup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tonyj
 */
public class NiemiecLifeLookup {

    public static void main(String[] args) throws IOException {
        List<Integer> results = new ArrayList();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    results.add(compute(i, j, k));
                }
            }
        }
        System.out.println(results.size());
        System.out.println(results);
        int[] counts = new int[8];
        for (int r : results) {
            counts[r]++;
        }
        System.out.println(Arrays.toString(counts));
        List<Integer> packed = new ArrayList();
        // Pack the results into ints -- 9*3 per int
        int l = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int pack = 0;
                for (int k = 0; k < 8; k++) {
                    pack = (pack << 3) + results.get(l+(7-k));
                }
                packed.add(pack);
                l += 8;
            }
        }
        System.out.println(packed.size() * 4);
        System.out.println(packed);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    final int cc = compute(i, j, k);
                    final int ll = lookup(packed, i, j, k);
                    if (cc != ll) {
                        System.out.printf("Error %d %d %d %d %d\n", i, j, k, cc, ll);
                    }
                }
            }
        }
    }

    static int lookup(List<Integer> packed, int... neighborIndexes) {
        int index = (neighborIndexes[0] << 3) + neighborIndexes[1];
        int pack = packed.get(index);
        return (pack >> (3 * neighborIndexes[2])) & 7;
    }

    static int compute(int... neighborIndexes) {
        int[] count = new int[8];
        int[] set = new int[2];
        //Three cells of the same colour produce a child of the same colour (x+x+x→x)
        //Two cells of any one colour plus one of any colour favour the dominant colour (x+x+y→x)
        for (int i = 0; i < 3; i++) {
            int index = neighborIndexes[i];
            if (++count[index] > 1) {
                return index;
            }
            set[index / 4]++;
        }
        //Three cells of different colours in the same set produce the complement of the fourth colour (x+y+z→w');
        for (int s = 0; s < 2; s++) {
            if (set[s] == 3) {
                for (int i = 0; i < 4; i++) {
                    if (count[i + s * 4] == 0) {
                        return i + (1 - s) * 4;
                    }
                }
            }
        }
        //Two complementary cells cancel out (x+x'+y→y)
        for (int i = 0; i < 4; i++) {
            if (count[i] == 1 && count[i + 4] == 1) {
                for (int j = 0; j < 8; j++) {
                    if (j % 4 != i && count[j] == 1) {
                        return j;
                    }
                }
            }
        }
        //Two cells of different colours from one set plus one from the other set favour the solitary one (x+y+z'→z').
        for (int s = 0; s < 2; s++) {
            if (set[s] == 2) {
                for (int i = 0; i < 4; i++) {
                    if (count[i + (1 - s) * 4] == 1) {
                        return i + (1 - s) * 4;
                    }
                }
            }
        }
        // Logically, we can't get here, but the compiler does not know that
        return -1;
    }
    
    public static class FullTransitionFunction implements RuleTree.TransitionFunction {

        @Override
        public int compute(int... a) {
            int n = 0;
            for (int i=0; i<8;i++) {
                if (a[i] != 0) n++;
            }
            if ((n == 2 || n == 3) && a[8] != 0) {
                return a[8];
            }
            if (n == 3) {
                int[] liveNeighbors = Arrays.stream(a).limit(8).filter(i -> i != 0).map(i -> i - 1).toArray();
                return NiemiecLifeLookup.compute(liveNeighbors) + 1;
            }
            return 0;
        }
        
    }
}
