package constructive;

import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;
import structure.BPPInstance;
import structure.BPPSolution;
import utils.Cell;

import java.util.*;

public class SS implements Constructive<BPPInstance, BPPSolution> {

    private final double alpha;
    private final int itersWithoutAdding;

    public SS(double alpha, int itersWithoutAdding) {
        this.alpha = alpha;
        this.itersWithoutAdding = itersWithoutAdding;
    }

    @Override
    public BPPSolution constructSolution(BPPInstance instance) {
        BPPSolution sol = new BPPSolution(instance);
        List<int[]> rectangles = new ArrayList<>(instance.getNRectangles());
        for (int[] rectangle : instance.getRectangles()) {
            if (!instance.getAvailableCellsForRectangle(rectangle[0]).isEmpty()) {
                rectangles.add(Arrays.copyOf(rectangle, rectangle.length));
            }
        }
        rectangles.sort(Comparator.comparingDouble(a -> (1.0 * a[3] / sol.getAvailableCellsForRectangle(a[0]).getFirst().getProfit())));
        Random rnd = RandomManager.getRandom();
        double realAlpha = (alpha >= 0) ? alpha : rnd.nextDouble();

        while (!sol.isFeasible() && !rectangles.isEmpty() && !Timer.timeReached()) {
            double gMin = 1.0 * rectangles.getFirst()[3] / sol.getAvailableCellsForRectangle(rectangles.getFirst()[0]).getFirst().getProfit();
            double gMax = 1.0 * rectangles.getLast()[3] / sol.getAvailableCellsForRectangle(rectangles.getLast()[0]).getFirst().getProfit();
            double th = gMin + realAlpha * (gMax - gMin);
            int limit = 0;
            while (limit < rectangles.size() && Double.compare(1.0 * rectangles.get(limit)[3] / sol.getAvailableCellsForRectangle(rectangles.get(limit)[0]).getFirst().getProfit(), th) <= 0) {
                limit++;
            }
            int rectIndex = rnd.nextInt(limit);
            int[] rect = rectangles.remove(rectIndex);

            List<Cell> cellsForRectangle = sol.getAvailableCellsForRectangle(rect[0]);

            boolean added = false;
            int iters = 0;
            while (!cellsForRectangle.isEmpty() && !added && !Timer.timeReached() && iters < itersWithoutAdding) {
                double gMinCells = cellsForRectangle.getLast().getProfit();
                double gMaxCells = cellsForRectangle.getFirst().getProfit();
                double thCells = gMaxCells - realAlpha * (gMaxCells - gMinCells);
                int limitCells = 0;
                while (limitCells < cellsForRectangle.size() && Double.compare(cellsForRectangle.get(limitCells).getProfit(), thCells) >= 0) {
                    limitCells++;
                }
                int cellIndex = rnd.nextInt(limitCells);
                Cell cell = cellsForRectangle.remove(cellIndex);
                added = sol.addRectangle(rect[0], cell);
                if (!added) {
                    iters++;
                } else {
                    iters = 0;
                    updateCandidates(sol, rect, cell, rectangles);
                }

            }
        }
        return sol;
    }

    private void updateCandidates(BPPSolution solution, int[] rect, Cell cell, List<int[]> rectangles) {
        Set<Cell> cellsToRemove = new HashSet<>();
        for (int i = cell.getRow(); i < cell.getRow() + rect[1]; i++) {
            for (int j = cell.getColumn(); j < cell.getColumn() + rect[2]; j++) {
                if (i < solution.getInstance().getRows() && j < solution.getInstance().getColumns()) {
                    cellsToRemove.add(solution.getInstance().getCell(i, j));
                }
            }
        }
        Set<int[]> rectanglesToRemove = new HashSet<>();
        for (int[] rectangle : rectangles) {
            solution.getAvailableCellsForRectangle(rectangle[0]).removeAll(cellsToRemove);
            if (solution.getAvailableCellsForRectangle(rectangle[0]).isEmpty()) {
                rectanglesToRemove.add(rectangle);
            }
        }
        rectangles.removeAll(rectanglesToRemove);
        rectangles.sort(Comparator.comparingDouble(a -> (1.0 * a[3] / solution.getAvailableCellsForRectangle(a[0]).getFirst().getProfit())));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + alpha + ", " + itersWithoutAdding + ")";
    }
}
