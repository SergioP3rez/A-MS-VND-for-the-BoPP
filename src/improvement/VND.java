package improvement;

import grafo.optilib.metaheuristics.Improvement;
import structure.BPPSolution;

import java.util.Arrays;

public class VND implements Improvement<BPPSolution> {
    private final Improvement<BPPSolution>[] ls;
    private final int kMax;

    public VND(Improvement<BPPSolution>[] ls) {
        this.ls = ls;
        this.kMax = ls.length;
    }

    @Override
    public void improve(BPPSolution sol) {
        int k = 0;
        BPPSolution best = new BPPSolution(sol);
        while (k < kMax) {
            ls[k].improve(sol);
            if (sol.getOFValue() > best.getOFValue()) {
                best = new BPPSolution(sol);
                k = (k != 0) ? 0 : k + 1;
            } else {
                k++;
            }
        }
        sol.copy(best);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + Arrays.toString(ls) + ", " + kMax + ")";
    }
}
