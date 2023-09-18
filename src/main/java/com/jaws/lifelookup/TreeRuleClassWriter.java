package com.jaws.lifelookup;

import java.io.PrintStream;
import java.util.Arrays;

/**
 *
 * @author tonyj
 */
public class TreeRuleClassWriter {

    private static final String BASE_CLASS = """
                       class %s : public TreeRule {
                         public:
                           int transition(int* neighbors) {
                             %s
                           }
                         private: 
                           const short lookup[%d][%d] = %s;                                     
                       };
                       """;

    public void write(PrintStream out, String className, RuleTree tree) {
        String body = buildBody(tree);
        String lookup = buildLookup(tree);
        out.printf(BASE_CLASS, className, body, tree.getNumNodes(), tree.getNumStates(), lookup);
    }

    private String buildBody(RuleTree tree) {
        StringBuilder sb = new StringBuilder();
        sb.append("int node = ").append(tree.getNumNodes() - 1).append('\n');
        sb.append("      int* n = neighbors;\n");
        for (int i = 0; i < tree.getNumNeighbors() + 1; i++) {
            sb.append("      node = lookup[node][*(n++)]\n");
        }
        sb.append("      return node;\n");
        return sb.toString();
    }

    private String buildLookup(RuleTree tree) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (int i = 0;;) {
            sb.append("      {");
            for (int j = 0;;) {
                sb.append(tree.getNodeValue(i, j + 1));
                if (++j == tree.getNumStates()) {
                    break;
                }
                sb.append(",");
            }
            sb.append("}");
            if (++i == tree.getNumNodes()) {
                break;
            }
            sb.append(",\n");
        }
        sb.append("\n    }");
        return sb.toString();
    }

    public static void main(String[] args) {
        RuleTree.RuleTreeBuilder ruleTreeBuilder = new RuleTree.RuleTreeBuilder(9, 8, new NiemiecLifeLookup.FullTransitionFunction());
        RuleTree tree = ruleTreeBuilder.build();
        TreeRuleClassWriter writer = new TreeRuleClassWriter();
        writer.write(System.out, "NiemiecTreeRule", tree);

    }
}
