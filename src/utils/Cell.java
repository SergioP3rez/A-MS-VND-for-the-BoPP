package utils;

import java.util.Objects;

public class Cell {
    private final int row;
    private final int column;
    private final int value;

    public Cell(int row, int column, int value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    public int getProfit() {
        return this.value;
    }

    public int getRow() {
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return row == cell.row && column == cell.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row +
                ", " + column +
                ") value=" + value;
    }
}
