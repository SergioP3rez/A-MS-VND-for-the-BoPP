package algorithm;

import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.tools.Timer;
import structure.BPPInstance;
import structure.BPPSolution;

import java.util.Objects;

public class AlgConstructiveLS implements Algorithm<BPPInstance, BPPSolution> {

    private BPPSolution best;
    private final Constructive<BPPInstance, BPPSolution> c;
    private final Improvement<BPPSolution> ls;
    private final int iters;
    private final int globalIters;

    public AlgConstructiveLS(Constructive<BPPInstance, BPPSolution> c, Improvement<BPPSolution> ls, int iters, int globalIters) {
        this.c = c;
        this.ls = ls;
        this.iters = iters;
        this.globalIters = globalIters;
    }

    @Override
    public Result execute(BPPInstance instance) {
        System.out.print(instance.getName() + "\t");
        Result r = new Result(instance.getName());
        this.best = null;
        for (int k = 0; k < globalIters; k++) {
            BPPSolution bestSolInIter = null;
            double secsToBest = 0;
            Timer.initTimer(60 * 1000);
            for (int i = 0; i < iters; i++) {
                BPPSolution sol = c.constructSolution(instance);
                ls.improve(sol);
                if (bestSolInIter == null || sol.getOFValue() > bestSolInIter.getOFValue()) {
                    secsToBest = Timer.getTime() / 1000.0;
                    bestSolInIter = new BPPSolution(sol);
                }

                if (Timer.timeReached()) break;
            }
            double secs = Timer.getTime() / 1000.0;

            r.add("O.F Value it " + k, Objects.requireNonNull(bestSolInIter).getOFValue());
            r.add("Time (s) it " + k, secs);
            r.add("Time to best (s) it " + k, secsToBest);
            if (this.best == null || bestSolInIter.getOFValue() > this.best.getOFValue()) {
                this.best = new BPPSolution(bestSolInIter);
            }
        }
        System.out.println("O.F Value: " + Objects.requireNonNull(this.best).getOFValue());
        System.out.println(this.best);
        return r;
    }

    @Override
    public BPPSolution getBestSolution() {
        return this.best;
    }

    @Override
    public String toString() {
        return "AlgConstructiveLS(" + c + ", " + ls + ", " + iters + ')';
    }
}
