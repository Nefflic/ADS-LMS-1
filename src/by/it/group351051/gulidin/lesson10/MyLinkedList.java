package by.it.group351051.gulidin.lesson10;

import java.util.*;

public class MyLinkedList<E> implements Deque<E> {

    private Node<E> firstNode;
    private Node<E> lastNode;
    private int listSize;

    public MyLinkedList() {
        this.firstNode = null;
        this.lastNode = null;
        this.listSize = 0;
    }

    // Внутренний класс для узла двунаправленного списка
    private static class Node<E> {
        E data;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E data, Node<E> next) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Обязательные к реализации методы             ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (listSize == 0) return "[]";

        StringBuilder sb = new StringBuilder("[");
        Node<E> current = firstNode;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) sb.append(", ");
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean add(E element) {
        addLast(element);
        return true;
    }

    // Метод remove(int index) - дополнительный метод List
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(getNode(index));
    }

    // ВАЖНО: этот метод remove() требуется интерфейсом Deque
    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object element) {
        Node<E> current = firstNode;
        while (current != null) {
            if (Objects.equals(element, current.data)) {
                unlink(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public int size() {
        return listSize;
    }

    @Override
    public void addFirst(E element) {
        Node<E> oldFirst = firstNode;
        Node<E> newNode = new Node<>(null, element, oldFirst);
        firstNode = newNode;

        if (oldFirst == null) {
            lastNode = newNode;
        } else {
            oldFirst.prev = newNode;
        }
        listSize++;
    }

    @Override
    public void addLast(E element) {
        Node<E> oldLast = lastNode;
        Node<E> newNode = new Node<>(oldLast, element, null);
        lastNode = newNode;

        if (oldLast == null) {
            firstNode = newNode;
        } else {
            oldLast.next = newNode;
        }
        listSize++;
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E getFirst() {
        Node<E> first = firstNode;
        if (first == null) {
            throw new NoSuchElementException("List is empty");
        }
        return first.data;
    }

    @Override
    public E getLast() {
        Node<E> last = lastNode;
        if (last == null) {
            throw new NoSuchElementException("List is empty");
        }
        return last.data;
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E pollFirst() {
        Node<E> first = firstNode;
        return (first == null) ? null : unlink(first);
    }

    @Override
    public E pollLast() {
        Node<E> last = lastNode;
        return (last == null) ? null : unlink(last);
    }

    // Остальные методы интерфейса Deque
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
        E element = getFirst();
        unlink(firstNode);
        return element;
    }

    @Override
    public E removeLast() {
        E element = getLast();
        unlink(lastNode);
        return element;
    }

    @Override
    public E peekFirst() {
        Node<E> first = firstNode;
        return (first == null) ? null : first.data;
    }

    @Override
    public E peekLast() {
        Node<E> last = lastNode;
        return (last == null) ? null : last.data;
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        Node<E> current = firstNode;
        while (current != null) {
            if (Objects.equals(o, current.data)) {
                unlink(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        Node<E> current = lastNode;
        while (current != null) {
            if (Objects.equals(o, current.data)) {
                unlink(current);
                return true;
            }
            current = current.prev;
        }
        return false;
    }

    @Override
    public boolean offer(E e) {
        return add(e);
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
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    public int indexOf(Object o) {
        int index = 0;
        Node<E> current = firstNode;
        while (current != null) {
            if (Objects.equals(o, current.data)) {
                return index;
            }
            index++;
            current = current.next;
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return listSize == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedListIterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    @Override
    public void clear() {
        Node<E> current = firstNode;
        while (current != null) {
            Node<E> next = current.next;
            current.data = null;
            current.next = null;
            current.prev = null;
            current = next;
        }
        firstNode = lastNode = null;
        listSize = 0;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[listSize];
        int i = 0;
        Node<E> current = firstNode;
        while (current != null) {
            result[i++] = current.data;
            current = current.next;
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < listSize) {
            T[] newArray = (T[]) new Object[listSize];
            int i = 0;
            Node<E> current = firstNode;
            while (current != null) {
                newArray[i++] = (T) current.data;
                current = current.next;
            }
            return newArray;
        }

        int i = 0;
        Node<E> current = firstNode;
        while (current != null) {
            a[i++] = (T) current.data;
            current = current.next;
        }

        if (a.length > listSize) {
            a[listSize] = null;
        }

        return a;
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
        boolean modified = false;
        for (E element : c) {
            add(element);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        Node<E> current = firstNode;
        while (current != null) {
            Node<E> next = current.next;
            if (c.contains(current.data)) {
                unlink(current);
                modified = true;
            }
            current = next;
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Node<E> current = firstNode;
        while (current != null) {
            Node<E> next = current.next;
            if (!c.contains(current.data)) {
                unlink(current);
                modified = true;
            }
            current = next;
        }
        return modified;
    }

    // Вспомогательные методы
    private Node<E> getNode(int index) {
        Node<E> current;
        if (index < (listSize >> 1)) {
            current = firstNode;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            current = lastNode;
            for (int i = listSize - 1; i > index; i--) {
                current = current.prev;
            }
        }
        return current;
    }

    private E unlink(Node<E> node) {
        E element = node.data;
        Node<E> next = node.next;
        Node<E> prev = node.prev;

        if (prev == null) {
            firstNode = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            lastNode = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.data = null;
        listSize--;
        return element;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= listSize) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + listSize);
        }
    }

    // Внутренние классы итераторов
    private class LinkedListIterator implements Iterator<E> {
        private Node<E> current = firstNode;
        private Node<E> lastReturned = null;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = current;
            current = current.next;
            return lastReturned.data;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            if (current == lastReturned) {
                current = lastNext;
            }
            lastReturned = null;
        }
    }

    private class DescendingIterator implements Iterator<E> {
        private Node<E> current = lastNode;
        private Node<E> lastReturned = null;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = current;
            current = current.prev;
            return lastReturned.data;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            Node<E> lastPrev = lastReturned.prev;
            unlink(lastReturned);
            if (current == lastReturned) {
                current = lastPrev;
            }
            lastReturned = null;
        }
    }

    // Вспомогательный класс для безопасного сравнения
    private static class Objects {
        public static boolean equals(Object a, Object b) {
            return java.util.Objects.equals(a, b);
        }
    }
}
