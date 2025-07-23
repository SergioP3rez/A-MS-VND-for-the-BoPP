package structure;

import grafo.optilib.structure.Solution;
import utils.Cell;
import utils.Colors;
import utils.OverlappedCellsStorage;

import java.util.*;
import java.util.List;

public class BPPSolution implements Solution {
    private BPPInstance instance;
    private int ofValue;

    private Set<Integer> usedRectangles;
    private Set<Integer> notUsedRectangles;
    private Map<Integer, List<Cell>> availableCellsForRectangle;
    private Map<Integer, List<Cell>> cellsOccupiedByRectangle;
    private Map<Integer, Cell> rectanglePlacedInCell;
    private Map<Cell, Set<Integer>> rectanglesContainedByCell;
    private int[][] occupiedBoard;
    private OverlappedCellsStorage overlappedCellsByRectangle;

    public BPPSolution(BPPInstance instance) {
        this.instance = instance;
        this.occupiedBoard = new int[instance.getRows()][instance.getColumns()];
        this.ofValue = 0;
        this.usedRectangles = new HashSet<>();
        this.notUsedRectangles = new HashSet<>();
        this.rectanglePlacedInCell = new HashMap<>();
        for (int[] rectangle : this.instance.getRectangles()) {
            this.notUsedRectangles.add(rectangle[0]);
        }
        this.availableCellsForRectangle = new HashMap<>(instance.getMapOfAvailableCellsForRectangle());
        this.cellsOccupiedByRectangle = new HashMap<>();
        this.overlappedCellsByRectangle = new OverlappedCellsStorage();
        this.rectanglesContainedByCell = new HashMap<>();
    }

    public BPPSolution(BPPSolution sol) {
        this.copy(sol);
    }

    public void copy(BPPSolution sol) {
        this.instance = sol.instance;
        this.ofValue = sol.ofValue;
        this.usedRectangles = new HashSet<>(sol.usedRectangles);
        this.notUsedRectangles = new HashSet<>(sol.notUsedRectangles);
        this.availableCellsForRectangle = new HashMap<>(sol.availableCellsForRectangle);
        this.cellsOccupiedByRectangle = new HashMap<>(sol.cellsOccupiedByRectangle);
        this.rectanglePlacedInCell = new HashMap<>();
        this.rectanglePlacedInCell.putAll(sol.rectanglePlacedInCell);
        this.occupiedBoard = new int[this.instance.getRows()][this.instance.getColumns()];
        for (int i = 0; i < this.instance.getRows(); i++) {
            this.occupiedBoard[i] = Arrays.copyOf(sol.occupiedBoard[i], this.instance.getColumns());
        }
        this.overlappedCellsByRectangle = new OverlappedCellsStorage(sol.overlappedCellsByRectangle);
        this.rectanglesContainedByCell = new HashMap<>(sol.rectanglesContainedByCell);
    }

    public boolean addRectangle(int id, Cell cell) {

        int row = cell.getRow();
        int column = cell.getColumn();
        int height = this.instance.getRectangleHeight(id);
        int width = this.instance.getRectangleWidth(id);
        int value = 0;
        boolean added = false;
        Set<Cell> potentialCells = new HashSet<>();
        int overlappedCells = 0;
        for (int i = row; i < row + height; i++) {
            for (int j = column; j < column + width; j++) {
                if (this.instance.isUsable(i, j)) {
                    if (!this.isUsedCell(this.instance.getCell(i, j))) {
                        value += this.instance.getBoardValue(i, j);
                    }
                    potentialCells.add(this.instance.getCell(i, j));
                    this.occupiedBoard[i][j]++;
                    if (this.occupiedBoard[i][j] > 1) {
                        overlappedCells++;
                    }
                }
            }
        }

        int cost = this.instance.getRectangleCost(id);
        if (value - cost > 0) {
            this.usedRectangles.add(id);
            this.rectanglePlacedInCell.put(id, this.instance.getCell(cell.getRow(), cell.getColumn()));
            this.notUsedRectangles.remove(id);
            this.availableCellsForRectangle.get(id).remove(cell);
            if (this.availableCellsForRectangle.get(id).isEmpty()) {
                this.availableCellsForRectangle.remove(id);
            }
            this.ofValue += (value - cost);
            this.cellsOccupiedByRectangle.putIfAbsent(id, new ArrayList<>());
            this.cellsOccupiedByRectangle.get(id).addAll(potentialCells);
            for (Cell potentialCell : potentialCells) {
                this.rectanglesContainedByCell.putIfAbsent(potentialCell, new HashSet<>());
                this.rectanglesContainedByCell.get(potentialCell).add(id);
            }
            if (overlappedCells > 0) {
                this.overlappedCellsByRectangle.put(id, (this.instance.getRectangleHeight(id) * this.instance.getRectangleWidth(id)) * 1. / overlappedCells);
            }
            added = true;
        } else {
            for (int i = row; i < row + height; i++) {
                for (int j = column; j < column + width; j++) {
                    if (this.instance.isUsable(i, j)) {
                        this.occupiedBoard[i][j]--;
                    }
                }
            }
        }
        return added;
    }

    public boolean isFeasible() {
        return this.availableCellsForRectangle.isEmpty() || this.usedRectangles.size() == this.instance.getNRectangles();
    }

    public List<Cell> getAvailableCellsForRectangle(int rect) {
        return this.availableCellsForRectangle.get(rect);
    }

    public int getOFValue() {
        return this.ofValue;
    }

    public boolean isUsedCell(Cell cell) {
        return this.occupiedBoard[cell.getRow()][cell.getColumn()] > 0;
    }

    private boolean isOverlappedCell(Cell cell) {
        return this.occupiedBoard[cell.getRow()][cell.getColumn()] > 1;
    }

    public BPPInstance getInstance() {
        return this.instance;
    }

    public Set<Integer> getUsedRectangles() {
        return this.usedRectangles;
    }

    public boolean moveRectangle(int usedRectangle, int direction) {

        return switch (direction) {
            case 0 -> moveRectangleUp(usedRectangle);
            case 1 -> moveRectangleLeft(usedRectangle);
            case 2 -> moveRectangleDown(usedRectangle);
            case 3 -> moveRectangleRight(usedRectangle);
            default -> false;
        };
    }

    private boolean moveRectangleRight(int usedRectangle) {
        boolean moved = false;
        Cell cell = this.rectanglePlacedInCell.get(usedRectangle);
        int row = cell.getRow();
        int column = cell.getColumn();
        int height = this.instance.getRectangleHeight(usedRectangle);
        int width = this.instance.getRectangleWidth(usedRectangle);
        int value = this.ofValue;
        int initialOFValue = value;
        int actualHighestColumn = column;
        for (int i = column + width; i < instance.getColumns(); i++) {
            moved = true;
            for (int j = row; j < row + height; j++) {
                if (!this.isOverlappedCell(this.instance.getCell(j, actualHighestColumn))) {
                    value -= this.instance.getBoardValue(j, actualHighestColumn);
                }
                if (this.rectanglesContainedByCell.containsKey(this.instance.getCell(j, actualHighestColumn))) {
                    this.rectanglesContainedByCell.get(this.instance.getCell(j, actualHighestColumn)).remove(usedRectangle);
                }
                this.cellsOccupiedByRectangle.get(usedRectangle).remove(this.instance.getCell(j, actualHighestColumn));
                this.occupiedBoard[j][actualHighestColumn]--;
                if (this.instance.isUsable(j, i)) {
                    if (!this.isUsedCell(this.instance.getCell(j, i))) {
                        value += this.instance.getBoardValue(j, i);
                    }
                    this.cellsOccupiedByRectangle.get(usedRectangle).add(this.instance.getCell(j, i));
                    this.occupiedBoard[j][i]++;

                }
            }
            this.rectanglePlacedInCell.put(usedRectangle, this.instance.getCell(row, i - width + 1));
            this.ofValue = value;
            if (this.ofValue > initialOFValue) {
                break;
            }
            actualHighestColumn++;

        }
        return moved;
    }

    private boolean moveRectangleLeft(int usedRectangle) {
        boolean moved = false;
        Cell cell = this.rectanglePlacedInCell.get(usedRectangle);
        int row = cell.getRow();
        int column = cell.getColumn();
        int height = this.instance.getRectangleHeight(usedRectangle);
        int width = this.instance.getRectangleWidth(usedRectangle);
        int value = this.ofValue;
        int initialOFValue = value;
        int actualLowestColumn = column + width - 1;
        for (int i = column - 1; i >= 0; i--) {
            moved = true;
            for (int j = row; j < row + height; j++) {
                if (!this.isOverlappedCell(this.getInstance().getCell(j, actualLowestColumn))) {
                    value -= this.instance.getBoardValue(j, actualLowestColumn);
                }
                if (this.rectanglesContainedByCell.containsKey(this.instance.getCell(j, actualLowestColumn))) {
                    this.rectanglesContainedByCell.get(this.instance.getCell(j, actualLowestColumn)).remove(usedRectangle);
                }
                this.cellsOccupiedByRectangle.get(usedRectangle).remove(this.instance.getCell(j, actualLowestColumn));
                this.occupiedBoard[j][actualLowestColumn]--;
                if (this.instance.isUsable(j, i)) {
                    if (!this.isUsedCell(this.instance.getCell(j, i))) {
                        value += this.instance.getBoardValue(j, i);
                    }
                    this.cellsOccupiedByRectangle.get(usedRectangle).add(this.instance.getCell(j, i));
                    this.occupiedBoard[j][i]++;
                }
            }
            this.rectanglePlacedInCell.put(usedRectangle, this.instance.getCell(row, i));
            this.ofValue = value;
            if (this.ofValue > initialOFValue) {
                break;
            }
            actualLowestColumn--;
        }
        return moved;
    }

    private boolean moveRectangleDown(int usedRectangle) {
        boolean moved = false;
        Cell cell = this.rectanglePlacedInCell.get(usedRectangle);
        int row = cell.getRow();
        int column = cell.getColumn();
        int width = this.instance.getRectangleWidth(usedRectangle);
        int height = this.instance.getRectangleHeight(usedRectangle);
        int value = this.ofValue;
        int initialOFValue = value;
        int actualHighestRow = row;
        for (int i = row + height; i < instance.getRows(); i++) {
            moved = true;
            for (int j = column; j < column + width; j++) {
                if (!this.isOverlappedCell(this.instance.getCell(actualHighestRow, j))) {
                    value -= this.instance.getBoardValue(actualHighestRow, j);
                }
                if (this.rectanglesContainedByCell.containsKey(this.instance.getCell(actualHighestRow, j))) {
                    this.rectanglesContainedByCell.get(this.instance.getCell(actualHighestRow, j)).remove(usedRectangle);
                }
                this.cellsOccupiedByRectangle.get(usedRectangle).remove(this.instance.getCell(actualHighestRow, j));
                this.occupiedBoard[actualHighestRow][j]--;
                if (this.instance.isUsable(i, j)) {
                    if (!this.isUsedCell(this.instance.getCell(i, j))) {
                        value += this.instance.getBoardValue(i, j);
                    }
                    this.cellsOccupiedByRectangle.get(usedRectangle).add(this.instance.getCell(i, j));
                    this.occupiedBoard[i][j]++;
                }
            }

            this.rectanglePlacedInCell.put(usedRectangle, this.instance.getCell(i - height + 1, column));
            this.ofValue = value;
            if (this.ofValue > initialOFValue) {
                break;
            }
            actualHighestRow++;
        }
        return moved;
    }

    private boolean moveRectangleUp(int usedRectangle) {
        boolean moved = false;
        Cell cell = this.rectanglePlacedInCell.get(usedRectangle);
        int row = cell.getRow();
        int column = cell.getColumn();
        int height = this.instance.getRectangleHeight(usedRectangle);
        int width = this.instance.getRectangleWidth(usedRectangle);
        int value = this.ofValue;
        int initialOFValue = value;
        int actualLowestRow = row + height - 1;
        for (int i = row - 1; i >= 0; i--) {
            moved = true;
            for (int j = column; j < column + width; j++) {
                if (!this.isOverlappedCell(this.getInstance().getCell(actualLowestRow, j))) {
                    value -= this.instance.getBoardValue(actualLowestRow, j);
                }
                if (this.rectanglesContainedByCell.containsKey(this.instance.getCell(actualLowestRow, j))) {
                    this.rectanglesContainedByCell.get(this.instance.getCell(actualLowestRow, j)).remove(usedRectangle);
                }
                this.cellsOccupiedByRectangle.get(usedRectangle).remove(this.instance.getCell(actualLowestRow, j));
                this.occupiedBoard[actualLowestRow][j]--;
                if (this.instance.isUsable(i, j)) {
                    if (!this.isUsedCell(this.instance.getCell(i, j))) {
                        value += this.instance.getBoardValue(i, j);
                    }
                    this.cellsOccupiedByRectangle.get(usedRectangle).add(this.instance.getCell(i, j));
                    this.occupiedBoard[i][j]++;
                }
            }

            this.rectanglePlacedInCell.put(usedRectangle, this.instance.getCell(i, column));
            this.ofValue = value;
            if (this.ofValue > initialOFValue) {
                break;
            }
            actualLowestRow--;
        }
        return moved;
    }

    public Cell getCellPositionOfRectangle(int usedRectangle) {
        return this.rectanglePlacedInCell.get(usedRectangle);
    }

    public void restoreRectangleToItsOriginalPosition(int usedRectangle, int direction, Cell originalCell) {
        switch (direction) {
            case 0 -> restoreRectangleUp(usedRectangle, originalCell);
            case 1 -> restoreRectangleLeft(usedRectangle, originalCell);
            case 2 -> restoreRectangleDown(usedRectangle, originalCell);
            case 3 -> restoreRectangleRight(usedRectangle, originalCell);
        }
    }

    private void restoreRectangleRight(int usedRectangle, Cell originalCell) {
        Cell cell = this.rectanglePlacedInCell.get(usedRectangle);
        int row = cell.getRow();
        int column = cell.getColumn();
        int height = this.instance.getRectangleHeight(usedRectangle);
        int width = this.instance.getRectangleWidth(usedRectangle);
        int value = this.ofValue;
        int actualHighestColumn = column;
        boolean restored;
        for (int i = column + width; i < instance.getColumns(); i++) {
            for (int j = row; j < row + height; j++) {
                if (!this.isOverlappedCell(this.instance.getCell(j, actualHighestColumn))) {
                    value -= this.instance.getBoardValue(j, actualHighestColumn);
                }
                if (this.rectanglesContainedByCell.containsKey(this.instance.getCell(j, actualHighestColumn))) {
                    this.rectanglesContainedByCell.get(this.instance.getCell(j, actualHighestColumn)).remove(usedRectangle);
                }
                this.cellsOccupiedByRectangle.get(usedRectangle).remove(this.instance.getCell(j, actualHighestColumn));
                this.occupiedBoard[j][actualHighestColumn]--;
                if (this.instance.isUsable(j, i)) {
                    if (!this.isUsedCell(this.instance.getCell(j, i))) {
                        value += this.instance.getBoardValue(j, i);
                    }
                    this.cellsOccupiedByRectangle.get(usedRectangle).add(this.instance.getCell(j, i));
                    this.occupiedBoard[j][i]++;
                }
            }

            this.rectanglePlacedInCell.put(usedRectangle, this.instance.getCell(row, i - width + 1));
            this.ofValue = value;
            restored = (i - width + 1 == originalCell.getColumn() && row == originalCell.getRow());
            if (restored) {
                break;
            }
            actualHighestColumn++;
        }
    }

    private void restoreRectangleLeft(int usedRectangle, Cell originalCell) {
        Cell cell = this.rectanglePlacedInCell.get(usedRectangle);
        int row = cell.getRow();
        int column = cell.getColumn();
        int height = this.instance.getRectangleHeight(usedRectangle);
        int width = this.instance.getRectangleWidth(usedRectangle);
        int value = this.ofValue;
        int actualLowestColumn = column + width - 1;
        boolean restored;
        for (int i = column - 1; i >= 0; i--) {
            for (int j = row; j < row + height; j++) {
                if (!this.isOverlappedCell(this.getInstance().getCell(j, actualLowestColumn))) {
                    value -= this.instance.getBoardValue(j, actualLowestColumn);
                }
                if (this.rectanglesContainedByCell.containsKey(this.instance.getCell(j, actualLowestColumn))) {
                    this.rectanglesContainedByCell.get(this.instance.getCell(j, actualLowestColumn)).remove(usedRectangle);
                }
                this.cellsOccupiedByRectangle.get(usedRectangle).remove(this.instance.getCell(j, actualLowestColumn));
                this.occupiedBoard[j][actualLowestColumn]--;
                if (this.instance.isUsable(j, i)) {
                    if (!this.isUsedCell(this.instance.getCell(j, i))) {
                        value += this.instance.getBoardValue(j, i);
                    }
                    this.cellsOccupiedByRectangle.get(usedRectangle).add(this.instance.getCell(j, i));
                    this.occupiedBoard[j][i]++;
                }
            }
            this.rectanglePlacedInCell.put(usedRectangle, this.instance.getCell(row, i));
            this.ofValue = value;
            restored = (i == originalCell.getColumn() && row == originalCell.getRow());
            if (restored) {
                break;
            }
            actualLowestColumn--;
        }
    }

    private void restoreRectangleDown(int usedRectangle, Cell originalCell) {
        Cell cell = this.rectanglePlacedInCell.get(usedRectangle);
        int row = cell.getRow();
        int column = cell.getColumn();
        int width = this.instance.getRectangleWidth(usedRectangle);
        int height = this.instance.getRectangleHeight(usedRectangle);
        int value = this.ofValue;
        int actualHighestRow = row;
        boolean restored;
        for (int i = row + height; i < instance.getRows(); i++) {
            for (int j = column; j < column + width; j++) {
                if (!this.isOverlappedCell(this.instance.getCell(actualHighestRow, j))) {
                    value -= this.instance.getBoardValue(actualHighestRow, j);
                }
                if (this.rectanglesContainedByCell.containsKey(this.instance.getCell(actualHighestRow, j))) {
                    this.rectanglesContainedByCell.get(this.instance.getCell(actualHighestRow, j)).remove(usedRectangle);
                }
                this.cellsOccupiedByRectangle.get(usedRectangle).remove(this.instance.getCell(actualHighestRow, j));
                this.occupiedBoard[actualHighestRow][j]--;
                if (this.instance.isUsable(i, j)) {
                    if (!this.isUsedCell(this.instance.getCell(i, j))) {
                        value += this.instance.getBoardValue(i, j);
                    }
                    this.cellsOccupiedByRectangle.get(usedRectangle).add(this.instance.getCell(i, j));
                    this.occupiedBoard[i][j]++;
                }
            }

            this.rectanglePlacedInCell.put(usedRectangle, this.instance.getCell(i - height + 1, column));
            this.ofValue = value;
            restored = (i - height + 1 == originalCell.getRow() && column == originalCell.getColumn());
            if (restored) {
                break;
            }
            actualHighestRow++;
        }
    }

    private void restoreRectangleUp(int usedRectangle, Cell originalCell) {
        Cell cell = this.rectanglePlacedInCell.get(usedRectangle);
        int row = cell.getRow();
        int column = cell.getColumn();
        int height = this.instance.getRectangleHeight(usedRectangle);
        int width = this.instance.getRectangleWidth(usedRectangle);
        int value = this.ofValue;
        int actualLowestRow = row + height - 1;
        boolean restored;
        for (int i = row - 1; i >= 0; i--) {
            for (int j = column; j < column + width; j++) {
                if (!this.isOverlappedCell(this.getInstance().getCell(actualLowestRow, j))) {
                    value -= this.instance.getBoardValue(actualLowestRow, j);
                }
                if (this.rectanglesContainedByCell.containsKey(this.instance.getCell(actualLowestRow, j))) {
                    this.rectanglesContainedByCell.get(this.instance.getCell(actualLowestRow, j)).remove(usedRectangle);
                }
                this.cellsOccupiedByRectangle.get(usedRectangle).remove(this.instance.getCell(actualLowestRow, j));
                this.occupiedBoard[actualLowestRow][j]--;
                if (this.instance.isUsable(i, j)) {
                    if (!this.isUsedCell(this.instance.getCell(i, j))) {
                        value += this.instance.getBoardValue(i, j);
                    }
                    this.cellsOccupiedByRectangle.get(usedRectangle).add(this.instance.getCell(i, j));
                    this.occupiedBoard[i][j]++;
                }
            }

            this.rectanglePlacedInCell.put(usedRectangle, this.instance.getCell(i, column));
            this.ofValue = value;
            restored = (i == originalCell.getRow() && column == originalCell.getColumn());
            if (restored) {
                break;
            }
            actualLowestRow--;
        }
    }

    @Override
    public String toString() {
        String[] indexColors = new String[]{
                "#FFFF00", "#1CE6FF", "#FF34FF", "#FF4A46", "#008941", "#006FA6", "#A30059",
                "#FFDBE5", "#7A4900", "#0000A6", "#63FFAC", "#B79762", "#004D43", "#8FB0FF", "#997D87",
                "#5A0007", "#809693", "#FEFFE6", "#1B4400", "#4FC601", "#3B5DFF", "#4A3B53", "#FF2F80",
                "#61615A", "#BA0900", "#6B7900", "#00C2A0", "#FFAA92", "#FF90C9", "#B903AA", "#D16100",
                "#DDEFFF", "#000035", "#7B4F4B", "#A1C299", "#300018", "#0AA6D8", "#013349", "#00846F",
                "#372101", "#FFB500", "#C2FFED", "#A079BF", "#CC0744", "#C0B9B2", "#C2FF99", "#001E09",
                "#00489C", "#6F0062", "#0CBD66", "#EEC3FF", "#456D75", "#B77B68", "#7A87A1", "#788D66",
                "#885578", "#FAD09F", "#FF8A9A", "#D157A0", "#BEC459", "#456648", "#0086ED", "#886F4C",
                "#34362D", "#B4A8BD", "#00A6AA", "#452C2C", "#636375", "#A3C8C9", "#FF913F", "#938A81",
                "#575329", "#00FECF", "#B05B6F", "#8CD0FF", "#3B9700", "#04F757", "#C8A1A1", "#1E6E00",
                "#7900D7", "#A77500", "#6367A9", "#A05837", "#6B002C", "#772600", "#D790FF", "#9B9700",
                "#549E79", "#FFF69F", "#201625", "#72418F", "#BC23FF", "#99ADC0", "#3A2465", "#922329",
                "#5B4534", "#FDE8DC", "#404E55", "#0089A3", "#CB7E98", "#A4E804", "#324E72", "#6A3A4C",
                "#83AB58", "#001C1E", "#D1F7CE", "#004B28", "#C8D0F6", "#A3A489", "#806C66", "#222800",
                "#BF5650", "#E83000", "#66796D", "#DA007C", "#FF1A59", "#8ADBB4", "#1E0200", "#5B4E51",
                "#C895C5", "#320033", "#FF6832", "#66E1D3", "#CFCDAC", "#D0AC94", "#7ED379", "#012C58",
                // Nuevos colores añadidos
                "#FF5733", "#33FF57", "#3357FF", "#FF33A1", "#A133FF", "#33FFA1", "#FF5733", "#33FF57",
                "#3357FF", "#FF33A1", "#A133FF", "#33FFA1", "#FF5733", "#33FF57", "#3357FF", "#FF33A1",
                "#A133FF", "#33FFA1", "#FF5733", "#33FF57", "#3357FF", "#FF33A1", "#A133FF", "#33FFA1",
                "#FF5733", "#33FF57", "#3357FF", "#FF33A1", "#A133FF", "#33FFA1", "#FF5733", "#33FF57",
                "#3357FF", "#FF33A1", "#A133FF", "#33FFA1", "#FF5733", "#33FF57", "#3357FF", "#FF33A1",
                "#A133FF", "#33FFA1", "#FF5733", "#33FF57", "#3357FF", "#FF33A1", "#A133FF", "#33FFA1",
                "#FF5733", "#33FF57", "#3357FF", "#FF33A1", "#A133FF", "#33FFA1", "#FF5733", "#33FF57",
                "#3357FF", "#FF33A1", "#A133FF", "#33FFA1", "#FF5733", "#33FF57", "#3357FF", "#FF33A1"
        };

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.instance.getRows(); i++) {
            for (int j = 0; j < this.instance.getColumns(); j++) {
                if (this.isUsedCell(this.instance.getCell(i, j)) && !isOverlappedCell(this.instance.getCell(i, j))) {
                    int rectangle = this.getRectangleIdInCell(i, j);
                    sb.append(Colors.bg(indexColors[rectangle % indexColors.length])).append(this.instance.getBoardValue(i, j)).append(Colors.reset()).append(" ");
                } else if (isOverlappedCell(this.instance.getCell(i, j))) {
                    sb.append(Colors.bg("#fc0011")).append(this.instance.getBoardValue(i, j)).append(Colors.reset()).append(" ");
                } else {
                    sb.append(this.instance.getBoardValue(i, j)).append(" ");
                }
            }
            sb.append("\n");
        }
        for (int usedRectangle : this.usedRectangles) {
            Cell actCell = this.getCellPositionOfRectangle(usedRectangle);
            sb.append("Rectangle ").append(usedRectangle).append(" in cell: ").append(actCell).append("\n");
        }
        sb.append("OF: ").append(this.ofValue).append("\n");
        return sb.toString();
    }

    private int getRectangleIdInCell(int i, int j) {
        for (int rectangle : this.usedRectangles) {
            Cell cell = this.rectanglePlacedInCell.get(rectangle);
            if (cell.getRow() <= i && i < cell.getRow() + this.instance.getRectangleHeight(rectangle) &&
                    cell.getColumn() <= j && j < cell.getColumn() + this.instance.getRectangleWidth(rectangle)) {
                return rectangle;
            }
        }
        return -1;
    }

}