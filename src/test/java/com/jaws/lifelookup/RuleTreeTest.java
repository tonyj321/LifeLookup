package com.jaws.lifelookup;

import com.jaws.lifelookup.RuleTree.RuleTreeBuilder;
import com.jaws.lifelookup.RuleTree.TransitionFunction;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author tonyj
 */
public class RuleTreeTest {

    @Test 
    public void neimiecSmall() {
        TransitionFunction f = (int... input) -> NiemiecLifeLookup.compute(input); 
        generateAndTest(f, 8, 3, 1000);
    }

    @Test 
    public void neimiecLarge() {
        generateAndTest(new NiemiecLifeLookup.FullTransitionFunction(), 9, 8, 1000);
    }
    
    private void generateAndTest(TransitionFunction f, int numStates, int numNeighbors, int nTests) {
        
        RuleTreeBuilder ruleTreeBuilder = new RuleTreeBuilder(numStates, numNeighbors, f);
        RuleTree tree = ruleTreeBuilder.build();
        tree.write(System.out);

        // Non-exhaustive test
        Random random = new Random();
        for (int i=0; i<nTests; i++) {
            int[] in = new int[tree.getNumNeighbors()+1];
            for (int j=0; i<in.length; i++) {
                in[j] = random.nextInt(tree.getNumStates());
            }
            int transitionResult = tree.transition(in);
            int originalResult = f.compute(in);
            assertEquals(originalResult, transitionResult);
        }
    }

    
}
