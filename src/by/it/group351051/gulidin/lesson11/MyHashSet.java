package by.it.group351051.gulidin.lesson11;

import java.util.*;

public class MyHashSet<E> implements Set<E> {

    private Node<E>[] buckets;
    private int setSize;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;

    // Конструкторы
    public MyHashSet() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public MyHashSet(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Initial capacity must be positive");
        }
        this.buckets = new Node[initialCapacity];
        this.setSize = 0;
    }

    // Внутренний класс для узлов односвязного списка
    private static class Node<E> {
        E data;
        Node<E> next;

        Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Обязательные к реализации методы             ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public int size() {
        return setSize;
    }

    @Override
    public void clear() {
        Arrays.fill(buckets, null);
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

        // Добавляем новый элемент в начало списка
        buckets[bucketIndex] = new Node<>(element, buckets[bucketIndex]);
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
                // Удаляем узел
                if (previous == null) {
                    // Удаляем первый элемент в цепочке
                    buckets[bucketIndex] = current.next;
                } else {
                    // Удаляем элемент из середины или конца цепочки
                    previous.next = current.next;
                }
                setSize--;
                return true;
            }
            previous = current;
            current = current.next;
        }

        return false; // Элемент не найден
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
    public String toString() {
        if (setSize == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (Node<E> bucket : buckets) {
            Node<E> current = bucket;
            while (current != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(current.data);
                first = false;
                current = current.next;
            }
        }

        sb.append("]");
        return sb.toString();
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Дополнительные методы интерфейса Set         ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public Iterator<E> iterator() {
        return new HashSetIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[setSize];
        int index = 0;

        for (Node<E> bucket : buckets) {
            Node<E> current = bucket;
            while (current != null) {
                result[index++] = current.data;
                current = current.next;
            }
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        if (array.length < setSize) {
            T[] newArray = (T[]) new Object[setSize];
            int index = 0;

            for (Node<E> bucket : buckets) {
                Node<E> current = bucket;
                while (current != null) {
                    newArray[index++] = (T) current.data;
                    current = current.next;
                }
            }
            return newArray;
        }

        int index = 0;
        for (Node<E> bucket : buckets) {
            Node<E> current = bucket;
            while (current != null) {
                array[index++] = (T) current.data;
                current = current.next;
            }
        }

        if (array.length > setSize) {
            array[setSize] = null;
        }

        return array;
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
    public boolean retainAll(Collection<?> collection) {
        boolean modified = false;

        for (int i = 0; i < buckets.length; i++) {
            Node<E> current = buckets[i];
            Node<E> previous = null;

            while (current != null) {
                if (!collection.contains(current.data)) {
                    // Удаляем элемент
                    if (previous == null) {
                        buckets[i] = current.next;
                    } else {
                        previous.next = current.next;
                    }
                    setSize--;
                    modified = true;
                    current = (previous == null) ? buckets[i] : previous.next;
                } else {
                    previous = current;
                    current = current.next;
                }
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

    /////////////////////////////////////////////////////////////////////////
    //////               Вспомогательные методы                       ///////
    /////////////////////////////////////////////////////////////////////////

    private int getBucketIndex(Object element) {
        return Math.abs(element.hashCode()) % buckets.length;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<E>[] oldBuckets = buckets;
        buckets = new Node[oldBuckets.length * 2];
        int oldSize = setSize;
        setSize = 0;

        // Перехешируем все элементы
        for (Node<E> oldBucket : oldBuckets) {
            Node<E> current = oldBucket;
            while (current != null) {
                add(current.data);
                current = current.next;
            }
        }

        setSize = oldSize; // Восстанавливаем правильный размер
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Внутренний класс итератора                   ///////
    /////////////////////////////////////////////////////////////////////////

    private class HashSetIterator implements Iterator<E> {
        private int bucketIndex = 0;
        private Node<E> currentNode = null;
        private Node<E> lastReturned = null;
        private int expectedSize = setSize;

        public HashSetIterator() {
            // Находим первый непустой bucket
            while (bucketIndex < buckets.length && buckets[bucketIndex] == null) {
                bucketIndex++;
            }
            if (bucketIndex < buckets.length) {
                currentNode = buckets[bucketIndex];
            }
        }

        @Override
        public boolean hasNext() {
            return currentNode != null;
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
            E result = currentNode.data;

            // Переходим к следующему элементу
            currentNode = currentNode.next;
            if (currentNode == null) {
                // Ищем следующий непустой bucket
                bucketIndex++;
                while (bucketIndex < buckets.length && buckets[bucketIndex] == null) {
                    bucketIndex++;
                }
                if (bucketIndex < buckets.length) {
                    currentNode = buckets[bucketIndex];
                }
            }

            return result;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            if (expectedSize != setSize) {
                throw new RuntimeException("ConcurrentModificationException");
            }

            MyHashSet.this.remove(lastReturned.data);
            expectedSize--;
            lastReturned = null;
        }
    }
}
