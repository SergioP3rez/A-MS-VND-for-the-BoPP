package algorithm;

import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.results.Result;
import grafo.optilib.tools.Timer;
import structure.BPPInstance;
import structure.BPPSolution;

import java.util.Objects;

public class AlgConstructive implements Algorithm<BPPInstance, BPPSolution> {

    private BPPSolution best;
    private final Constructive<BPPInstance, BPPSolution> c;
    private final int iters;
    private final int globalIters;

    public AlgConstructive(Constructive<BPPInstance, BPPSolution> c, int iters, int globalIters) {
        this.c = c;
        this.iters = iters;
        this.globalIters = globalIters;
    }

    @Override
    public Result execute(BPPInstance instance) {
        Result r = new Result(instance.getName());
        System.out.println(instance.getName());
        this.best = null;
        for (int k = 0; k < globalIters; k++) {

            double secsToBest = 0;
            BPPSolution bestItSol = null;

            Timer.initTimer(60 * 1000);
            for (int i = 0; i < iters; i++) {
                BPPSolution sol = c.constructSolution(instance);
                if (bestItSol == null || sol.getOFValue() > bestItSol.getOFValue()) {
                    secsToBest = Timer.getTime() / 1000.0;
                    bestItSol = new BPPSolution(sol);
                }

                if (Timer.timeReached()) break;
            }

            double secs = Timer.getTime() / 1000.0;
            r.add("O.F Value it " + k, Objects.requireNonNull(bestItSol).getOFValue());
            r.add("Time (s) it " + k, secs);
            r.add("Time to best (s) it " + k, secsToBest);
            if (this.best == null || bestItSol.getOFValue() > this.best.getOFValue()) {
                this.best = new BPPSolution(bestItSol);
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
        return "AlgConstructive(" + c + ", " + iters + ')';
    }
}
