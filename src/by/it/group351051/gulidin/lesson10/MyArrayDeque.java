package by.it.group351051.gulidin.lesson10;

import java.util.*;

public class MyArrayDeque<E> implements Deque<E> {

    private Object[] elements;
    private int headIndex;
    private int tailIndex;
    private int numberOfElements;
    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    public MyArrayDeque() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
        this.headIndex = 0;
        this.tailIndex = 0;
        this.numberOfElements = 0;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Обязательные к реализации методы             ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (numberOfElements == 0) return "[]";

        StringBuilder sb = new StringBuilder("[");
        int current = headIndex;
        for (int i = 0; i < numberOfElements; i++) {
            sb.append(elements[current]);
            if (i < numberOfElements - 1) sb.append(", ");
            current = increaseIndex(current);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int size() {
        return numberOfElements;
    }

    @Override
    public boolean add(E element) {
        addLast(element);
        return true;
    }

    @Override
    public void addFirst(E element) {
        if (numberOfElements == elements.length) {
            grow();
        }
        headIndex = decreaseIndex(headIndex);
        elements[headIndex] = element;
        numberOfElements++;
    }

    @Override
    public void addLast(E element) {
        if (numberOfElements == elements.length) {
            grow();
        }
        elements[tailIndex] = element;
        tailIndex = increaseIndex(tailIndex);
        numberOfElements++;
    }

    @Override
    public E element() {
        if (numberOfElements == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        return getFirst();
    }

    @Override
    public E getFirst() {
        if (numberOfElements == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        return (E) elements[headIndex];
    }

    @Override
    public E getLast() {
        if (numberOfElements == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        return (E) elements[decreaseIndex(tailIndex)];
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E pollFirst() {
        if (numberOfElements == 0) {
            return null;
        }
        E result = (E) elements[headIndex];
        elements[headIndex] = null;
        headIndex = increaseIndex(headIndex);
        numberOfElements--;
        return result;
    }

    @Override
    public E pollLast() {
        if (numberOfElements == 0) {
            return null;
        }
        tailIndex = decreaseIndex(tailIndex);
        E result = (E) elements[tailIndex];
        elements[tailIndex] = null;
        numberOfElements--;
        return result;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Дополнительные методы интерфейса Deque       ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    public E removeFirst() {
        if (numberOfElements == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        return pollFirst();
    }

    @Override
    public E removeLast() {
        if (numberOfElements == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        return pollLast();
    }

    @Override
    public E peekFirst() {
        if (numberOfElements == 0) {
            return null;
        }
        return (E) elements[headIndex];
    }

    @Override
    public E peekLast() {
        if (numberOfElements == 0) {
            return null;
        }
        return (E) elements[decreaseIndex(tailIndex)];
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        int current = headIndex;
        for (int i = 0; i < numberOfElements; i++) {
            if (o == null ? elements[current] == null : o.equals(elements[current])) {
                removeAtIndex(current);
                return true;
            }
            current = increaseIndex(current);
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        int current = decreaseIndex(tailIndex);
        for (int i = 0; i < numberOfElements; i++) {
            if (o == null ? elements[current] == null : o.equals(elements[current])) {
                removeAtIndex(current);
                return true;
            }
            current = decreaseIndex(current);
        }
        return false;
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean contains(Object o) {
        int current = headIndex;
        for (int i = 0; i < numberOfElements; i++) {
            if (o == null ? elements[current] == null : o.equals(elements[current])) {
                return true;
            }
            current = increaseIndex(current);
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return numberOfElements == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new DequeIterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    @Override
    public void clear() {
        for (int i = 0; i < elements.length; i++) {
            elements[i] = null;
        }
        headIndex = 0;
        tailIndex = 0;
        numberOfElements = 0;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[numberOfElements];
        int current = headIndex;
        for (int i = 0; i < numberOfElements; i++) {
            result[i] = elements[current];
            current = increaseIndex(current);
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < numberOfElements) {
            T[] newArray = (T[]) new Object[numberOfElements];
            int current = headIndex;
            for (int i = 0; i < numberOfElements; i++) {
                newArray[i] = (T) elements[current];
                current = increaseIndex(current);
            }
            return newArray;
        }

        int current = headIndex;
        for (int i = 0; i < numberOfElements; i++) {
            a[i] = (T) elements[current];
            current = increaseIndex(current);
        }

        if (a.length > numberOfElements) {
            a[numberOfElements] = null;
        }

        return a;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object item : c) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E item : c) {
            add(item);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object item : c) {
            while (remove(item)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        int current = headIndex;
        for (int i = 0; i < numberOfElements; i++) {
            if (!c.contains(elements[current])) {
                removeAtIndex(current);
                modified = true;
                i--; // Корректируем индекс после удаления
            } else {
                current = increaseIndex(current);
            }
        }
        return modified;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Вспомогательные методы                       ///////
    /////////////////////////////////////////////////////////////////////////

    private int increaseIndex(int index) {
        return (index + 1) % elements.length;
    }

    private int decreaseIndex(int index) {
        return (index - 1 + elements.length) % elements.length;
    }

    private void grow() {
        int newCapacity = elements.length + (elements.length >> 1); // Увеличиваем на 50%
        Object[] newArray = new Object[newCapacity];

        // Копируем элементы в новый массив
        int current = headIndex;
        for (int i = 0; i < numberOfElements; i++) {
            newArray[i] = elements[current];
            current = increaseIndex(current);
        }

        elements = newArray;
        headIndex = 0;
        tailIndex = numberOfElements;
    }

    private void removeAtIndex(int index) {
        // Определяем, с какой стороны быстрее удалить элемент
        int distanceFromHead = (index - headIndex + elements.length) % elements.length;
        int distanceFromTail = numberOfElements - distanceFromHead - 1;

        if (distanceFromHead <= distanceFromTail) {
            // Сдвигаем элементы от головы
            while (index != headIndex) {
                int prev = decreaseIndex(index);
                elements[index] = elements[prev];
                index = prev;
            }
            elements[headIndex] = null;
            headIndex = increaseIndex(headIndex);
        } else {
            // Сдвигаем элементы от хвоста
            while (index != decreaseIndex(tailIndex)) {
                int next = increaseIndex(index);
                elements[index] = elements[next];
                index = next;
            }
            tailIndex = decreaseIndex(tailIndex);
            elements[tailIndex] = null;
        }
        numberOfElements--;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Внутренние классы итераторов                  ///////
    /////////////////////////////////////////////////////////////////////////

    private class DequeIterator implements Iterator<E> {
        private int current = headIndex;
        private int count = 0;
        private int lastReturned = -1;

        @Override
        public boolean hasNext() {
            return count < numberOfElements;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = current;
            E result = (E) elements[current];
            current = increaseIndex(current);
            count++;
            return result;
        }

        @Override
        public void remove() {
            if (lastReturned < 0) {
                throw new IllegalStateException();
            }
            removeAtIndex(lastReturned);
            if (lastReturned < current) {
                current = decreaseIndex(current);
            }
            lastReturned = -1;
            count--;
        }
    }

    private class DescendingIterator implements Iterator<E> {
        private int current = decreaseIndex(tailIndex);
        private int count = 0;
        private int lastReturned = -1;

        @Override
        public boolean hasNext() {
            return count < numberOfElements;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = current;
            E result = (E) elements[current];
            current = decreaseIndex(current);
            count++;
            return result;
        }

        @Override
        public void remove() {
            if (lastReturned < 0) {
                throw new IllegalStateException();
            }
            removeAtIndex(lastReturned);
            if (lastReturned > current) {
                current = increaseIndex(current);
            }
            lastReturned = -1;
            count--;
        }
    }
}
