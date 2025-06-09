package by.it.group351051.gulidin.lesson10;

import java.util.*;

public class MyPriorityQueue<E> implements Queue<E> {

    private Object[] heap;
    private int heapSize;
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    private Comparator<? super E> comparator;

    public MyPriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    public MyPriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    public MyPriorityQueue(Comparator<? super E> comparator) {
        this(DEFAULT_INITIAL_CAPACITY, comparator);
    }

    public MyPriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException();
        }
        this.heap = new Object[initialCapacity];
        this.heapSize = 0;
        this.comparator = comparator;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Обязательные к реализации методы             ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (heapSize == 0) return "[]";

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < heapSize; i++) {
            sb.append(heap[i]);
            if (i < heapSize - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int size() {
        return heapSize;
    }

    @Override
    public void clear() {
        for (int i = 0; i < heapSize; i++) {
            heap[i] = null;
        }
        heapSize = 0;
    }

    @Override
    public boolean add(E element) {
        return offer(element);
    }

    @Override
    public E remove() {
        E result = poll();
        if (result == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return result;
    }

    @Override
    public boolean contains(Object element) {
        return indexOf(element) != -1;
    }

    @Override
    public boolean offer(E element) {
        if (element == null) {
            throw new NullPointerException();
        }

        int i = heapSize;
        if (i >= heap.length) {
            grow();
        }

        heapSize = i + 1;
        if (i == 0) {
            heap[0] = element;
        } else {
            siftUp(i, element);
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E poll() {
        if (heapSize == 0) {
            return null;
        }

        int lastIndex = --heapSize;
        E result = (E) heap[0];
        E lastElement = (E) heap[lastIndex];
        heap[lastIndex] = null;

        if (lastIndex != 0) {
            siftDown(0, lastElement);
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E peek() {
        return (heapSize == 0) ? null : (E) heap[0];
    }

    @Override
    public E element() {
        E result = peek();
        if (result == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return heapSize == 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        boolean modified = false;
        for (E element : c) {
            if (add(element)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        // ИСПРАВЛЕНО: собираем элементы для сохранения
        Object[] temp = new Object[heapSize];
        int tempSize = 0;

        for (int i = 0; i < heapSize; i++) {
            if (!c.contains(heap[i])) {
                temp[tempSize++] = heap[i];
            } else {
                modified = true;
            }
        }

        if (modified) {
            rebuildHeap(temp, tempSize);
        }

        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        // ИСПРАВЛЕНО: собираем элементы для сохранения
        Object[] temp = new Object[heapSize];
        int tempSize = 0;

        for (int i = 0; i < heapSize; i++) {
            if (c.contains(heap[i])) {
                temp[tempSize++] = heap[i];
            } else {
                modified = true;
            }
        }

        if (modified) {
            rebuildHeap(temp, tempSize);
        }

        return modified;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index == -1) {
            return false;
        }
        removeAt(index);
        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return new PriorityQueueIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[heapSize];
        System.arraycopy(heap, 0, result, 0, heapSize);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < heapSize) {
            return (T[]) Arrays.copyOf(heap, heapSize, a.getClass());
        }
        System.arraycopy(heap, 0, a, 0, heapSize);
        if (a.length > heapSize) {
            a[heapSize] = null;
        }
        return a;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Вспомогательные методы кучи                  ///////
    /////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private void rebuildHeap(Object[] elements, int size) {
        // Очищаем текущую кучу
        clear();

        // Копируем элементы в кучу
        for (int i = 0; i < size; i++) {
            heap[i] = elements[i];
        }
        heapSize = size;

        // Применяем heapify снизу вверх
        for (int i = (heapSize >>> 1) - 1; i >= 0; i--) {
            siftDown(i, (E) heap[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int k, E x) {
        if (comparator != null) {
            siftUpUsingComparator(k, x);
        } else {
            siftUpComparable(k, x);
        }
    }

    @SuppressWarnings("unchecked")
    private void siftUpComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = heap[parent];
            if (key.compareTo((E) e) >= 0) {
                break;
            }
            heap[k] = e;
            k = parent;
        }
        heap[k] = key;
    }

    @SuppressWarnings("unchecked")
    private void siftUpUsingComparator(int k, E x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = heap[parent];
            if (comparator.compare(x, (E) e) >= 0) {
                break;
            }
            heap[k] = e;
            k = parent;
        }
        heap[k] = x;
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int k, E x) {
        if (comparator != null) {
            siftDownUsingComparator(k, x);
        } else {
            siftDownComparable(k, x);
        }
    }

    @SuppressWarnings("unchecked")
    private void siftDownComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        int half = heapSize >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = heap[child];
            int right = child + 1;
            if (right < heapSize && ((Comparable<? super E>) c).compareTo((E) heap[right]) > 0) {
                c = heap[child = right];
            }
            if (key.compareTo((E) c) <= 0) {
                break;
            }
            heap[k] = c;
            k = child;
        }
        heap[k] = key;
    }

    @SuppressWarnings("unchecked")
    private void siftDownUsingComparator(int k, E x) {
        int half = heapSize >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = heap[child];
            int right = child + 1;
            if (right < heapSize && comparator.compare((E) c, (E) heap[right]) > 0) {
                c = heap[child = right];
            }
            if (comparator.compare(x, (E) c) <= 0) {
                break;
            }
            heap[k] = c;
            k = child;
        }
        heap[k] = x;
    }

    private void grow() {
        int oldCapacity = heap.length;
        int newCapacity = oldCapacity + ((oldCapacity < 64) ? (oldCapacity + 2) : (oldCapacity >> 1));
        heap = Arrays.copyOf(heap, newCapacity);
    }

    private int indexOf(Object o) {
        if (o != null) {
            for (int i = 0; i < heapSize; i++) {
                if (o.equals(heap[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private E removeAt(int i) {
        int lastIndex = --heapSize;
        E result = (E) heap[i];
        if (lastIndex == i) {
            heap[i] = null;
        } else {
            E moved = (E) heap[lastIndex];
            heap[lastIndex] = null;
            siftDown(i, moved);
            if (heap[i] == moved) {
                siftUp(i, moved);
            }
        }
        return result;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Внутренний класс итератора                   ///////
    /////////////////////////////////////////////////////////////////////////

    private class PriorityQueueIterator implements Iterator<E> {
        private int cursor = 0;
        private int lastRet = -1;

        @Override
        public boolean hasNext() {
            return cursor < heapSize;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (cursor >= heapSize) {
                throw new NoSuchElementException();
            }
            lastRet = cursor;
            return (E) heap[cursor++];
        }

        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            removeAt(lastRet);
            cursor = lastRet;
            lastRet = -1;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Внутренний класс Arrays для копирования      ///////
    /////////////////////////////////////////////////////////////////////////

    private static class Arrays {
        public static Object[] copyOf(Object[] original, int newLength) {
            Object[] copy = new Object[newLength];
            System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
            return copy;
        }

        @SuppressWarnings("unchecked")
        public static <T> T[] copyOf(Object[] original, int newLength, Class<? extends T[]> newType) {
            T[] copy = (T[]) new Object[newLength];
            System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
            return copy;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Внутренний интерфейс Comparator              ///////
    /////////////////////////////////////////////////////////////////////////

    public interface Comparator<T> {
        int compare(T o1, T o2);
    }
}
