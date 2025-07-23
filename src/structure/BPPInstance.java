package structure;

import grafo.optilib.structure.Instance;
import utils.Cell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BPPInstance implements Instance {
    private String name;
    private int rows;
    private int columns;
    private int[][] board;
    private int nRectangles;
    private int[][] rectangles; // 0 = id, 1 = height, 2 = width, 3 = cost
    private Map<Integer, List<Cell>> availableCellsForRectangle;
    private List<Cell> availableCells;

    public BPPInstance(String path) {
        readInstance(path);
    }

    @Override
    public void readInstance(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            this.name = path.substring(path.lastIndexOf("/") + 1);
            this.rows = Integer.parseInt(br.readLine());
            this.columns = Integer.parseInt(br.readLine());
            this.availableCells = new ArrayList<>(this.rows * this.columns);
            this.board = new int[this.rows][this.columns];
            for (int i = 0; i < this.rows; i++) {
                String[] tokens = br.readLine().strip().split(", ");
                for (int j = 0; j < this.columns; j++) {
                    this.board[i][j] = Integer.parseInt(tokens[j]);
                    this.availableCells.add(new Cell(i, j, this.board[i][j]));
                }
            }
            this.nRectangles = Integer.parseInt(br.readLine());
            this.rectangles = new int[nRectangles][4]; // 0 = id, 1 = height, 2 = width, 3 = cost
            this.availableCellsForRectangle = new HashMap<>();
            for (int i = 0; i < nRectangles; i++) {
                String[] tokens = br.readLine().strip().split(", ");
                this.rectangles[i][0] = i;
                for (int j = 0; j < 3; j++) {
                    this.rectangles[i][j + 1] = Integer.parseInt(tokens[j]);
                }

                for (int j = 0; j + this.rectangles[i][1] <= this.rows; j++) {
                    for (int k = 0; k + this.rectangles[i][2] <= this.columns; k++) {
                        int counter = 0;
                        for (int l = j; l < j + this.rectangles[i][1]; l++) {
                            for (int m = k; m < k + this.rectangles[i][2]; m++) {
                                counter += this.board[l][m];
                            }
                        }
                        this.availableCellsForRectangle.putIfAbsent(i, new ArrayList<>());
                        if (counter - this.rectangles[i][3] > 0) {
                            this.availableCellsForRectangle.get(i).add(new Cell(j, k, counter - this.rectangles[i][3]));
                        }
                    }
                }
                if (!this.availableCellsForRectangle.get(i).isEmpty()) {
                    this.availableCellsForRectangle.get(i).sort((a, b) -> Integer.compare(b.getProfit(), a.getProfit()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return this.name;
    }

    public List<Cell> getAvailableCellsForRectangle(int rectangle) {
        return this.availableCellsForRectangle.get(rectangle);
    }

    public Map<Integer, List<Cell>> getMapOfAvailableCellsForRectangle() {
        Map<Integer, List<Cell>> copy = new HashMap<>();
        for (int key : this.availableCellsForRectangle.keySet()) {
            copy.put(key, new ArrayList<>(this.availableCellsForRectangle.get(key)));
        }
        return copy;
    }

    public int[] getRectangle(int id) {
        return this.rectangles[id];
    }

    public int getRectangleWidth(int id) {
        return this.rectangles[id][2];
    }

    public int getRectangleHeight(int id) {
        return this.rectangles[id][1];
    }

    public int getRectangleCost(int id) {
        return this.rectangles[id][3];
    }

    public int getNRectangles() {
        return this.nRectangles;
    }

    public Cell getCell(int i, int j) {
        return this.availableCells.get(i * this.columns + j);
    }

    public int getBoardValue(int i, int j) {
        return this.board[i][j];
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    @Override
    public String toString() {
        return this.name;
    }


    public int[][] getRectangles() {
        return this.rectangles;
    }

    public boolean isUsable(int i, int j) {
        return i * this.columns + j < this.availableCells.size();
    }

}