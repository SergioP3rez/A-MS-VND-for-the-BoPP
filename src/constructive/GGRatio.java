package constructive;

import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.Timer;
import structure.BPPInstance;
import structure.BPPSolution;
import utils.Cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GGRatio implements Constructive<BPPInstance, BPPSolution> {
    @Override
    public BPPSolution constructSolution(BPPInstance instance) {
        BPPSolution sol = new BPPSolution(instance);
        List<int[]> rectangles = new ArrayList<>(instance.getNRectangles());
        for (int[] rectangle : instance.getRectangles()) {
            if (!instance.getAvailableCellsForRectangle(rectangle[0]).isEmpty()) {
                rectangles.add(Arrays.copyOf(rectangle, rectangle.length));
            }
        }
        rectangles.sort(Comparator.comparingDouble(a -> 1. * a[3] / sol.getAvailableCellsForRectangle(a[0]).getFirst().getProfit()));
        int rect = 0;
        while (!sol.isFeasible() && rect < rectangles.size() && !Timer.timeReached()) {
            List<Cell> cellsForRectangle = sol.getAvailableCellsForRectangle(rectangles.get(rect)[0]);
            boolean added = false;
            while (!cellsForRectangle.isEmpty() && !added && !Timer.timeReached()) {
                Cell cell = cellsForRectangle.removeFirst();
                added = sol.addRectangle(rectangles.get(rect)[0], cell);
            }
            rect++;
        }
        return sol;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
