package by.it.group351051.gulidin.lesson11;

import java.util.*;

public class MyLinkedHashSet<E> implements Set<E> {

    private Node<E>[] buckets;
    private int setSize;
    private Node<E> header; // Заголовок двусвязного списка для порядка вставки
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;

    // Конструкторы
    public MyLinkedHashSet() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public MyLinkedHashSet(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Initial capacity must be positive");
        }
        this.buckets = new Node[initialCapacity];
        this.setSize = 0;
        this.header = new Node<>(null);
        this.header.before = this.header.after = this.header; // Циклический список
    }

    // Внутренний класс для узлов с поддержкой порядка вставки
    private static class Node<E> {
        E data;
        Node<E> next;     // Следующий в цепочке коллизий
        Node<E> before;   // Предыдущий в порядке вставки
        Node<E> after;    // Следующий в порядке вставки

        Node(E data) {
            this.data = data;
        }

        Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
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
        Node<E> current = header.after;
        boolean first = true;

        while (current != header) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(current.data);
            first = false;
            current = current.after;
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
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = null;
        }
        header.before = header.after = header;
        setSize = 0;
    }

    @Override
    public boolean isEmpty() {
        return setSize == 0;
    }

    @Override
    public boolean add(E element) {
        if (element == null) {
            throw new NullPointerException("Null elements are not allowed");
        }

        // Проверяем необходимость увеличения размера
        if (setSize >= buckets.length * DEFAULT_LOAD_FACTOR) {
            resize();
        }

        int bucketIndex = getBucketIndex(element);
        Node<E> current = buckets[bucketIndex];

        // Проверяем, есть ли уже такой элемент
        while (current != null) {
            if (element.equals(current.data)) {
                return false; // Элемент уже существует
            }
            current = current.next;
        }

        // Создаем новый узел и добавляем в bucket
        Node<E> newNode = new Node<>(element, buckets[bucketIndex]);
        buckets[bucketIndex] = newNode;

        // Добавляем в конец двусвязного списка (порядок вставки)
        linkLast(newNode);
        setSize++;
        return true;
    }

    @Override
    public boolean remove(Object element) {
        if (element == null) {
            return false;
        }

        int bucketIndex = getBucketIndex(element);
        Node<E> current = buckets[bucketIndex];
        Node<E> previous = null;

        while (current != null) {
            if (element.equals(current.data)) {
                // Удаляем из bucket
                if (previous == null) {
                    buckets[bucketIndex] = current.next;
                } else {
                    previous.next = current.next;
                }

                // Удаляем из двусвязного списка
                unlinkNode(current);
                setSize--;
                return true;
            }
            previous = current;
            current = current.next;
        }

        return false;
    }

    @Override
    public boolean contains(Object element) {
        if (element == null) {
            return false;
        }

        int bucketIndex = getBucketIndex(element);
        Node<E> current = buckets[bucketIndex];

        while (current != null) {
            if (element.equals(current.data)) {
                return true;
            }
            current = current.next;
        }

        return false;
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
        Node<E> current = header.after;

        while (current != header) {
            Node<E> next = current.after;
            if (!collection.contains(current.data)) {
                remove(current.data);
                modified = true;
            }
            current = next;
        }

        return modified;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Дополнительные методы интерфейса Set         ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public Iterator<E> iterator() {
        return new LinkedHashSetIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[setSize];
        int index = 0;
        Node<E> current = header.after;

        while (current != header) {
            result[index++] = current.data;
            current = current.after;
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        if (array.length < setSize) {
            T[] newArray = (T[]) new Object[setSize];
            int index = 0;
            Node<E> current = header.after;

            while (current != header) {
                newArray[index++] = (T) current.data;
                current = current.after;
            }
            return newArray;
        }

        int index = 0;
        Node<E> current = header.after;

        while (current != header) {
            array[index++] = (T) current.data;
            current = current.after;
        }

        if (array.length > setSize) {
            array[setSize] = null;
        }

        return array;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Вспомогательные методы                       ///////
    /////////////////////////////////////////////////////////////////////////

    private int getBucketIndex(Object element) {
        return Math.abs(element.hashCode()) % buckets.length;
    }

    // Добавляет узел в конец двусвязного списка
    private void linkLast(Node<E> node) {
        Node<E> last = header.before;
        node.before = last;
        node.after = header;
        last.after = node;
        header.before = node;
    }

    // Удаляет узел из двусвязного списка
    private void unlinkNode(Node<E> node) {
        Node<E> before = node.before;
        Node<E> after = node.after;
        before.after = after;
        after.before = before;
        node.before = node.after = null; // Очищаем ссылки
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<E>[] oldBuckets = buckets;
        buckets = new Node[oldBuckets.length * 2];

        // Перехешируем все элементы в новую таблицу
        for (int i = 0; i < oldBuckets.length; i++) {
            Node<E> current = oldBuckets[i];
            while (current != null) {
                Node<E> next = current.next;

                // Перемещаем узел в новый bucket
                int newBucketIndex = getBucketIndex(current.data);
                current.next = buckets[newBucketIndex];
                buckets[newBucketIndex] = current;

                current = next;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Внутренний класс итератора                   ///////
    /////////////////////////////////////////////////////////////////////////

    private class LinkedHashSetIterator implements Iterator<E> {
        private Node<E> currentNode = header.after;
        private Node<E> lastReturned = null;
        private int expectedSize = setSize;

        @Override
        public boolean hasNext() {
            return currentNode != header;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new RuntimeException("NoSuchElementException");
            }
            if (expectedSize != setSize) {
                throw new RuntimeException("ConcurrentModificationException");
            }

            lastReturned = currentNode;
            currentNode = currentNode.after;
            return lastReturned.data;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            if (expectedSize != setSize) {
                throw new RuntimeException("ConcurrentModificationException");
            }

            MyLinkedHashSet.this.remove(lastReturned.data);
            expectedSize--;
            lastReturned = null;
        }
    }
}
