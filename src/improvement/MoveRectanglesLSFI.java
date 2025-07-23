package improvement;

import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.Timer;
import structure.BPPSolution;
import utils.Cell;

import java.util.HashSet;
import java.util.Set;

public class MoveRectanglesLSFI implements Improvement<BPPSolution> {
    private final int direction;

    public MoveRectanglesLSFI(int direction) {
        this.direction = direction; // 0 = up, 1 = left, 2 = down, 3 = right
    }

    @Override
    public void improve(BPPSolution sol) {

        boolean improved = true;
        while (improved && !Timer.timeReached()) {
            improved = false;
            Set<Integer> usedRectangles = new HashSet<>(sol.getUsedRectangles());
            Cell[] originalPositions = new Cell[sol.getInstance().getNRectangles()];
            for (int i = 0; i < sol.getInstance().getNRectangles(); i++) {
                if (usedRectangles.contains(i)) {
                    originalPositions[i] = sol.getCellPositionOfRectangle(i);
                }
            }
            int actualOFValue = sol.getOFValue();
            for (int usedRectangle : usedRectangles) {
                boolean moved = sol.moveRectangle(usedRectangle, direction);
                if (sol.getOFValue() > actualOFValue) {
                    improved = true;
                    break;
                } else if (moved) {
                    Cell cell = originalPositions[usedRectangle];
                    sol.restoreRectangleToItsOriginalPosition(usedRectangle, (direction + 2) % 4, cell);
                }
            }
        }
    }

    @Override
    public String toString() {
        String direction = switch (this.direction) {
            case 0 -> "up";
            case 1 -> "left";
            case 2 -> "down";
            case 3 -> "right";
            default -> "";
        };
        return this.getClass().getSimpleName() + "(" + direction + ")";
    }
}

