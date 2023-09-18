package com.jaws.lifelookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author tonyj
 */
public class RuleTree {

    private final int numStates;
    private final int numNeighbors;
    private final int numNodes;
    private final int[] nodes;
    private int nextNode;

    private RuleTree(int numStates, int numNeighbors, int numNodes) {
        this.numStates = numStates;
        this.numNeighbors = numNeighbors;
        this.numNodes = numNodes;
        nodes = new int[numNodes * (numStates + 1)];
        nextNode = 0;
    }

    public int getNumStates() {
        return numStates;
    }

    public int getNumNeighbors() {
        return numNeighbors;
    }

    public int getNumNodes() {
        return numNodes;
    }
    
    public int getNodeValue(int node, int index) {
        return nodes[node*(numStates + 1) + index];
    }

    public int transition(int... states) {
        int rootNode = nodes.length - (numStates + 1);
        int node = rootNode;
        for (int state : states) {
            int depth = nodes[node];
            int next = nodes[node + state + 1];
            if (depth == 1) {
                return next;
            } else {
                node = next * (numStates + 1);
            }
        }
        throw new IllegalStateException();
    }

    private boolean addNode(int[] node) {
        System.arraycopy(node, 0, nodes, nextNode, numStates + 1);
        nextNode += numStates + 1;
        return nextNode == nodes.length;
    }

    public void write(PrintStream out) {
        out.printf("num_states=%d\nnum_neighbors=%d\nnum_nodes=%d\n", numStates, numNeighbors, numNodes);
        for (int n = 0; n < numNodes; n++) {
            for (int i = 0; i < numStates + 1; i++) {
                int index = n * (numStates + 1) + i;
                System.out.printf("%d ", nodes[index]);
            }
            System.out.println();
        }
    }

    public interface TransitionFunction {

        int compute(int... input);
    }

    public static class RuleTreeBuilder {

        private final int numStates;
        private final int numNeighbors;
        private final TransitionFunction f;
        private final Map<List<Integer>, Integer> nodes = new LinkedHashMap<>();

        public RuleTreeBuilder(int numStates, int numNeighbors, TransitionFunction f) {
            this.numStates = numStates;
            this.numNeighbors = numNeighbors;
            this.f = f;
        }

        public RuleTree build() {
            // Loop over all combinations of input states;
            int[] params = new int[numNeighbors + 1];
            recurse(numNeighbors + 1, params);
            RuleTree result = new RuleTree(numStates, numNeighbors, nodes.size());
            for (List<Integer> node : nodes.keySet()) {
                result.addNode(node.stream().mapToInt(i->i).toArray());
            }
            return result;
        }

        private int getNode(List<Integer> node) {
            return nodes.computeIfAbsent(node, n -> nodes.size());
        }
        
        private int recurse(int depth, int[] params) {
            if (depth == 0) {
                return f.compute(params);
            }
            List<Integer> node = new ArrayList<>(numStates + 1);
            node.add(depth);
            for (int i = 0; i < numStates; i++) {
                params[params.length - depth] = i;
                int result = recurse(depth - 1, params);
                node.add(result);
            }
            return getNode(node);
        }
    }

    static RuleTree read(BufferedReader reader) throws IOException {
        int numStates = 0;
        int numNeighbours = 0;
        int numNodes = 0;
        Pattern assignment = Pattern.compile("(.*)=(\\d+)");
        for (;;) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Premature end of file");
            }
            if (line.startsWith("#") || line.isBlank()) {
                continue;
            }
            Matcher matcher = assignment.matcher(line.replaceAll("\\s", ""));
            if (matcher.matches()) {
                String token = matcher.group(1);
                int value = Integer.parseInt(matcher.group(2));
                switch (token) {
                    case "num_states" ->
                        numStates = value;
                    case "num_neighbors" ->
                        numNeighbours = value;
                    case "num_nodes" ->
                        numNodes = value;
                    default ->
                        throw new IOException("Illegal line: " + line);
                }
            } else {
                throw new IOException("Illegal line" + line);
            }
            if (numStates != 0 && numNeighbours != 0 && numNodes != 0) {
                break;
            }
        }
        RuleTree ruleTree = new RuleTree(numStates, numNeighbours, numNodes);
        for (;;) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Premature end of file");
            }
            if (line.startsWith("#") || line.isBlank()) {
                continue;
            }
            String[] tokens = line.split("\\s+");
            if (tokens.length != numStates + 1) {
                throw new IOException("Illegal line with " + tokens.length + " tokens: " + line);
            }
            int[] node = Arrays.stream(tokens).mapToInt(Integer::parseInt).toArray();
            boolean done = ruleTree.addNode(node);
            if (done) {
                break;
            }
        }
        return ruleTree;
    }

//    public static void main(String[] args) throws IOException {
//        try (InputStream in = RuleTree.class.getResourceAsStream("/NiemiecLife.tree"); Reader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
//            RuleTree ruleTree = RuleTree.read(br);
//            ruleTree.write(System.out);
//
//            int result = ruleTree.transition(0, 1, 0, 2, 3, 0, 0, 0, 0);
//            System.out.println(result);
//        }
//    }
//    
//    public static void main(String[] args) {
//        
//        TransitionFunction f = (int... input) -> NiemiecLifeLookup.compute(input); 
//        RuleTreeBuilder ruleTreeBuilder = new RuleTreeBuilder(8, 3, f);
//        RuleTree tree = ruleTreeBuilder.build();
//        tree.write(System.out);
//    }

    public static void main(String[] args) {
        
        //TransitionFunction f = (int... input) -> NiemiecLifeLookup.compute(input); 
        TransitionFunction f = (int... a) -> {
            //long n = Arrays.stream(a).limit(8).filter(i -> i != 0).count();
            int n = 0;
            for (int i=0; i<8;i++) {
                if (a[i] != 0) n++;
            }
            if ((n == 2 || n == 3) && a[8] != 0) {
                return a[8];
            }
            if (n == 3) {
                int[] liveNeighbors = Arrays.stream(a).limit(8).filter(i -> i != 0).map(i -> i - 1).toArray();
                return NiemiecLifeLookup.compute(liveNeighbors);
            }
            return 0;
        };
        
        RuleTreeBuilder ruleTreeBuilder = new RuleTreeBuilder(9, 8, f);
        RuleTree tree = ruleTreeBuilder.build();
        tree.write(System.out);
    }
}
