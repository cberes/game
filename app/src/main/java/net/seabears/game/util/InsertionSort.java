package net.seabears.game.util;

import java.util.Comparator;
import java.util.List;

/**
 * A simple implementation of an insertion sort. I implemented this very quickly the other day so it
 * may not be perfect or the most efficient! Feel free to implement your own sorter instead.
 *
 * @author Karl
 *
 */
public class InsertionSort {
    /**
     * Sorts a list of particles so that the particles with the highest distance from the camera are
     * first, and the particles with the shortest distance are last.
     *
     * @param list the list of particles to sort
     */
    public static <T> void sortDescending(List<T> list, Comparator<T> cmp) {
        for (int i = 1; i < list.size(); i++) {
            T item = list.get(i);
            if (cmp.compare(item, list.get(i - 1)) > 0) {
                sortDescending(list, cmp, i);
            }
        }
    }
 
    private static <T> void sortDescending(List<T> list, Comparator<T> cmp, int i) {
        T item = list.get(i);
        int attemptPos = i - 1;
        while (attemptPos != 0 && cmp.compare(item, list.get(attemptPos - 1)) >= 0) {
            attemptPos--;
        }
        list.remove(i);
        list.add(attemptPos, item);
    }
}
