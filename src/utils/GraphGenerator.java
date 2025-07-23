package utils;

import constructive.GS;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;
import improvement.MoveRectanglesLSFI;
import improvement.VND;
import structure.BPPInstance;
import structure.BPPSolution;


public class GraphGenerator {
    public static void main(String[] args) {
        RandomManager.setSeed(1234);
        BPPInstance instance = new BPPInstance("/instances/whole_set/31.txt");
        representGraphicsNewBest(instance);
        representConstruction(instance);
    }

    private static void representConstruction(BPPInstance instance) {
        Timer.initTimer(1000);
        while (!Timer.timeReached()) {
            Constructive<BPPInstance, BPPSolution> c = new GS(-1.0, 15);
            BPPSolution sol = c.constructSolution(instance);
            System.out.println(sol.getOFValue() + " " + Timer.getTime() / 1000.);
        }
    }

    private static void representGraphicsNewBest(BPPInstance instance) {
        Timer.initTimer(1000);
        BPPSolution best = null;
        while (!Timer.timeReached()) {
            Constructive<BPPInstance, BPPSolution> c = new GS(-1.0, 15);
            BPPSolution sol = c.constructSolution(instance);
            if (best == null || sol.getOFValue() > best.getOFValue()) {
                best = sol;
                System.out.println("New best: " + best.getOFValue() + " " + Timer.getTime() / 1000.);
            }
            Improvement<BPPSolution> ls = new VND(new Improvement[]{
                    new MoveRectanglesLSFI(0),
                    new MoveRectanglesLSFI(1),
                    new MoveRectanglesLSFI(2),
                    new MoveRectanglesLSFI(3)
            });
            ls.improve(sol);
            if (sol.getOFValue() > best.getOFValue()) {
                best = sol;
                System.out.println("New best: " + best.getOFValue() + " " + Timer.getTime() / 1000.);
            }
        }
    }
}
