package utils;

import java.util.*;

public class OverlappedCellsStorage {
    private Map<Integer, Double> data;

    public OverlappedCellsStorage() {
        data = new HashMap<>();
    }

    public OverlappedCellsStorage(OverlappedCellsStorage overlappedCellsByRectangle) {
        this.data = new HashMap<>(overlappedCellsByRectangle.data);
    }

    public void put(int id, double number) {
        data.put(id, number);
    }

    public Double get(int id) {
        return data.get(id);
    }

    public void remove(int id) {
        data.remove(id);
    }

    public boolean containsKey(int id) {
        return data.containsKey(id);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public List<Map.Entry<Integer, Double>> getSortedEntries() {
        List<Map.Entry<Integer, Double>> entries = new ArrayList<>(data.entrySet());

        entries.sort((e1, e2) -> {
            int cmp = Double.compare(e2.getValue(), e1.getValue());
            if (cmp == 0) {
                cmp = Double.compare(e1.getKey(), e2.getKey());
            }
            return cmp;
        });

        return entries;
    }

    public Map.Entry<Integer, Double> getMaxEntry() {
        PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>(
                (e1, e2) -> {
                    int cmp = Double.compare(e2.getValue(), e1.getValue());
                    if (cmp == 0) {
                        cmp = Integer.compare(e1.getKey(), e2.getKey());
                    }
                    return cmp;
                }
        );
        pq.addAll(data.entrySet());
        data.remove(pq.peek().getKey());
        return pq.remove();
    }
}
