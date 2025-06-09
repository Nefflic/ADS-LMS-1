package by.it.group351051.gulidin.lesson11;

import java.util.*;

public class MyTreeSet<E> implements Set<E> {

    private Object[] elements;
    private int setSize;
    private Comparator<? super E> comparator;
    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    // Конструкторы
    public MyTreeSet() {
        this((Comparator<? super E>) null);
    }

    public MyTreeSet(Comparator<? super E> comparator) {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
        this.setSize = 0;
        this.comparator = comparator;
    }

    public MyTreeSet(Collection<? extends E> collection) {
        this();
        addAll(collection);
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Обязательные к реализации методы             ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (setSize == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < setSize; i++) {
            sb.append(elements[i]);
            if (i < setSize - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int size() {
        return setSize;
    }

    @Override
    public void clear() {
        for (int i = 0; i < setSize; i++) {
            elements[i] = null;
        }
        setSize = 0;
    }

    @Override
    public boolean isEmpty() {
        return setSize == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean add(E element) {
        if (element == null) {
            throw new NullPointerException("Null elements are not allowed");
        }

        // Находим позицию для вставки
        int insertIndex = findInsertionPoint(element);

        // Проверяем, есть ли уже такой элемент
        if (insertIndex < setSize && compare((E) elements[insertIndex], element) == 0) {
            return false; // Элемент уже существует
        }

        // Расширяем массив при необходимости
        if (setSize >= elements.length) {
            grow();
        }

        // Сдвигаем элементы вправо
        for (int i = setSize; i > insertIndex; i--) {
            elements[i] = elements[i - 1];
        }

        // Вставляем новый элемент
        elements[insertIndex] = element;
        setSize++;
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object element) {
        if (element == null) {
            return false;
        }

        int index = binarySearch((E) element);
        if (index < 0) {
            return false; // Элемент не найден
        }

        // Сдвигаем элементы влево
        for (int i = index; i < setSize - 1; i++) {
            elements[i] = elements[i + 1];
        }

        elements[--setSize] = null;
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object element) {
        if (element == null) {
            return false;
        }

        return binarySearch((E) element) >= 0;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object element : collection) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean modified = false;
        for (E element : collection) {
            if (add(element)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean modified = false;
        for (Object element : collection) {
            if (remove(element)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        boolean modified = false;
        int writeIndex = 0;

        for (int readIndex = 0; readIndex < setSize; readIndex++) {
            if (collection.contains(elements[readIndex])) {
                elements[writeIndex++] = elements[readIndex];
            } else {
                modified = true;
            }
        }

        // Очищаем оставшиеся элементы
        for (int i = writeIndex; i < setSize; i++) {
            elements[i] = null;
        }

        setSize = writeIndex;
        return modified;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Дополнительные методы интерфейса Set         ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public Iterator<E> iterator() {
        return new TreeSetIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[setSize];
        for (int i = 0; i < setSize; i++) {
            result[i] = elements[i];
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        if (array.length < setSize) {
            T[] newArray = (T[]) new Object[setSize];
            for (int i = 0; i < setSize; i++) {
                newArray[i] = (T) elements[i];
            }
            return newArray;
        }

        for (int i = 0; i < setSize; i++) {
            array[i] = (T) elements[i];
        }

        if (array.length > setSize) {
            array[setSize] = null;
        }

        return array;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Дополнительные методы TreeSet                ///////
    /////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public E first() {
        if (setSize == 0) {
            throw new RuntimeException("NoSuchElementException");
        }
        return (E) elements[0];
    }

    @SuppressWarnings("unchecked")
    public E last() {
        if (setSize == 0) {
            throw new RuntimeException("NoSuchElementException");
        }
        return (E) elements[setSize - 1];
    }

    @SuppressWarnings("unchecked")
    public E pollFirst() {
        if (setSize == 0) {
            return null;
        }
        E result = (E) elements[0];
        remove(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public E pollLast() {
        if (setSize == 0) {
            return null;
        }
        E result = (E) elements[setSize - 1];
        remove(result);
        return result;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Вспомогательные методы                       ///////
    /////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private int compare(E e1, E e2) {
        if (comparator != null) {
            return comparator.compare(e1, e2);
        } else {
            return ((Comparable<? super E>) e1).compareTo(e2);
        }
    }

    // Бинарный поиск элемента
    @SuppressWarnings("unchecked")
    private int binarySearch(E element) {
        int left = 0;
        int right = setSize - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = compare((E) elements[mid], element);

            if (cmp == 0) {
                return mid; // Элемент найден
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return -1; // Элемент не найден
    }

    // Найти позицию для вставки элемента
    @SuppressWarnings("unchecked")
    private int findInsertionPoint(E element) {
        int left = 0;
        int right = setSize - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = compare((E) elements[mid], element);

            if (cmp == 0) {
                return mid; // Элемент уже существует
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return left; // Позиция для вставки
    }

    // Увеличиваем размер массива
    private void grow() {
        int newCapacity = elements.length + (elements.length >> 1); // Увеличиваем на 50%
        Object[] newElements = new Object[newCapacity];
        for (int i = 0; i < setSize; i++) {
            newElements[i] = elements[i];
        }
        elements = newElements;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Внутренний класс итератора                   ///////
    /////////////////////////////////////////////////////////////////////////

    private class TreeSetIterator implements Iterator<E> {
        private int currentIndex = 0;
        private int lastReturnedIndex = -1;
        private int expectedSize = setSize;

        @Override
        public boolean hasNext() {
            return currentIndex < setSize;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) {
                throw new RuntimeException("NoSuchElementException");
            }
            if (expectedSize != setSize) {
                throw new RuntimeException("ConcurrentModificationException");
            }

            lastReturnedIndex = currentIndex;
            return (E) elements[currentIndex++];
        }

        @Override
        public void remove() {
            if (lastReturnedIndex < 0) {
                throw new IllegalStateException();
            }
            if (expectedSize != setSize) {
                throw new RuntimeException("ConcurrentModificationException");
            }

            MyTreeSet.this.remove(elements[lastReturnedIndex]);
            currentIndex = lastReturnedIndex;
            lastReturnedIndex = -1;
            expectedSize--;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Внутренний интерфейс Comparator              ///////
    /////////////////////////////////////////////////////////////////////////

    public interface Comparator<T> {
        int compare(T o1, T o2);
    }
}
