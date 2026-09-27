package sdrs.algorithm;

import java.util.ArrayList;
import java.util.List;

import sdrs.model.Disaster;

public class DisasterPriorityQueue {

    private final List<Disaster> heap = new ArrayList<Disaster>();

    public DisasterPriorityQueue() {
    }

    public DisasterPriorityQueue(List<Disaster> disasters) {
        for (int i = 0; i < disasters.size(); i++) {
            insert(disasters.get(i));
        }
	}

    public void insert(Disaster disaster) {
        heap.add(disaster);
        siftUp(heap.size() - 1);
    }

    public Disaster peek() {
        if (heap.isEmpty()) {
            return null;
        }
        return heap.get(0);
    }

    public Disaster extractMax() {
        if (heap.isEmpty()) {
            return null;
        }
        Disaster top = heap.get(0);
        Disaster last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return top;
    }

    public List<Disaster> toSortedList() {
        List<Disaster> copy = new ArrayList<Disaster>(heap);
        List<Disaster> sorted = new ArrayList<Disaster>();
        while (!copy.isEmpty()) {
            Disaster top = copy.get(0);
            Disaster last = copy.remove(copy.size() - 1);
            if (!copy.isEmpty()) {
                copy.set(0, last);
                siftDownOn(copy, 0);
            }
            sorted.add(top);
        }
        return sorted;
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap.get(parent).getPriorityScore() >= heap.get(index).getPriorityScore()) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        siftDownOn(heap, index);
    }

    private void siftDownOn(List<Disaster> list, int index) {
        int size = list.size();
        while (true) {
            int left = index * 2 + 1;
            int right = index * 2 + 2;
            int largest = index;
            if (left < size && list.get(left).getPriorityScore() > list.get(largest).getPriorityScore()) {
                largest = left;
            }
            if (right < size && list.get(right).getPriorityScore() > list.get(largest).getPriorityScore()) {
                largest = right;
            }
            if (largest == index) {
                break;
            }
            Disaster tmp = list.get(index);
            list.set(index, list.get(largest));
            list.set(largest, tmp);
            index = largest;
        }
    }

    private void swap(int a, int b) {
        Disaster tmp = heap.get(a);
        heap.set(a, heap.get(b));
        heap.set(b, tmp);
    }
}
