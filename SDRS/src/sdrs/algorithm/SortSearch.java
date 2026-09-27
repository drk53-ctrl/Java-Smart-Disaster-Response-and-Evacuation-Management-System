package sdrs.algorithm;

import java.util.ArrayList;
import java.util.List;

public final class SortSearch {

    private SortSearch() {
    }

    public interface Key {

        double keyOf(Object item);
    }

    public static List<Object> mergeSort(List<Object> items, Key key, boolean descending) {
        if (items.size() <= 1) {
            return new ArrayList<Object>(items);
        }
        int mid = items.size() / 2;
        List<Object> left = mergeSort(items.subList(0, mid), key, descending);
        List<Object> right = mergeSort(items.subList(mid, items.size()), key, descending);
        return merge(left, right, key, descending);
    }

    private static List<Object> merge(List<Object> left, List<Object> right, Key key, boolean descending) {
        List<Object> result = new ArrayList<Object>();
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            double a = key.keyOf(left.get(i));
            double b = key.keyOf(right.get(j));
            boolean takeLeft = descending ? (a >= b) : (a <= b);
            if (takeLeft) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }
        while (i < left.size()) {
            result.add(left.get(i));
            i++;
        }
        while (j < right.size()) {
            result.add(right.get(j));
            j++;
        }
        return result;
    }

    public static int binarySearch(List<String> sortedIds, String targetId) {
        int low = 0;
        int high = sortedIds.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int compare = sortedIds.get(mid).compareTo(targetId);
            if (compare == 0) {
                return mid;
            }
            if (compare < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }
}
