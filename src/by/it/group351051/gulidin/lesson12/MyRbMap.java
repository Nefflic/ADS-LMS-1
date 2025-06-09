package by.it.group351051.gulidin.lesson12;

import java.util.*;

public class MyRbMap implements SortedMap<Integer, String> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private Node root;
    private int mapSize;

    public MyRbMap() {
        this.root = null;
        this.mapSize = 0;
    }

    // Внутренний класс для узла красно-черного дерева
    private static class Node {
        Integer key;
        String value;
        Node left;
        Node right;
        boolean color;

        Node(Integer key, String value, boolean color) {
            this.key = key;
            this.value = value;
            this.color = color;
            this.left = null;
            this.right = null;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Обязательные к реализации методы             ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (mapSize == 0) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{");
        inOrderToString(root, sb);
        // Убираем последнюю запятую и пробел
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String put(Integer key, String value) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }

        String oldValue = get(key);
        root = insertNode(root, key, value);
        root.color = BLACK; // Корень всегда черный

        if (oldValue == null) {
            mapSize++;
        }
        return oldValue;
    }

    @Override
    public String remove(Object key) {
        if (!(key instanceof Integer)) {
            return null;
        }

        Integer intKey = (Integer) key;
        String oldValue = get(intKey);
        if (oldValue != null) {
            root = deleteNode(root, intKey);
            if (root != null) {
                root.color = BLACK;
            }
            mapSize--;
        }
        return oldValue;
    }

    @Override
    public String get(Object key) {
        if (!(key instanceof Integer)) {
            return null;
        }

        Integer intKey = (Integer) key;
        Node node = findNode(root, intKey);
        return node != null ? node.value : null;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return containsValueHelper(root, value);
    }

    @Override
    public int size() {
        return mapSize;
    }

    @Override
    public void clear() {
        root = null;
        mapSize = 0;
    }

    @Override
    public boolean isEmpty() {
        return mapSize == 0;
    }

    @Override
    public SortedMap<Integer, String> headMap(Integer toKey) {
        MyRbMap result = new MyRbMap();
        collectHeadMap(root, toKey, result);
        return result;
    }

    @Override
    public SortedMap<Integer, String> tailMap(Integer fromKey) {
        MyRbMap result = new MyRbMap();
        collectTailMap(root, fromKey, result);
        return result;
    }

    @Override
    public Integer firstKey() {
        if (root == null) {
            throw new RuntimeException("NoSuchElementException");
        }
        return findMin(root).key;
    }

    @Override
    public Integer lastKey() {
        if (root == null) {
            throw new RuntimeException("NoSuchElementException");
        }
        return findMax(root).key;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Методы красно-черного дерева                 ///////
    /////////////////////////////////////////////////////////////////////////

    // Левый поворот
    private Node rotateLeft(Node node) {
        Node newRoot = node.right;
        node.right = newRoot.left;
        newRoot.left = node;
        newRoot.color = node.color;
        node.color = RED;
        return newRoot;
    }

    // Правый поворот
    private Node rotateRight(Node node) {
        Node newRoot = node.left;
        node.left = newRoot.right;
        newRoot.right = node;
        newRoot.color = node.color;
        node.color = RED;
        return newRoot;
    }

    // Вставка узла (алгоритм Седжвика)
    private Node insertNode(Node node, Integer key, String value) {
        if (node == null) {
            return new Node(key, value, RED);
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = insertNode(node.left, key, value);
        } else if (cmp > 0) {
            node.right = insertNode(node.right, key, value);
        } else {
            // Ключ уже существует, обновляем значение
            node.value = value;
            return node;
        }

        return balance(node);
    }

    // Балансировка узла
    private Node balance(Node node) {
        // Правило 1: красная связь справа -> поворот влево
        if (isRed(node.right) && !isRed(node.left)) {
            node = rotateLeft(node);
        }

        // Правило 2: две красные связи подряд слева -> поворот вправо
        if (isRed(node.left) && node.left != null && isRed(node.left.left)) {
            node = rotateRight(node);
        }

        // Правило 3: обе связи красные -> смена цветов
        if (isRed(node.left) && isRed(node.right)) {
            flipColors(node);
        }

        return node;
    }

    // Удаление узла
    private Node deleteNode(Node node, Integer key) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            if (!isRed(node.left) && node.left != null && !isRed(node.left.left)) {
                node = moveRedLeft(node);
            }
            node.left = deleteNode(node.left, key);
        } else {
            if (isRed(node.left)) {
                node = rotateRight(node);
                cmp = key.compareTo(node.key); // Пересчитываем после поворота
            }
            if (cmp == 0 && node.right == null) {
                return null;
            }
            if (!isRed(node.right) && node.right != null && !isRed(node.right.left)) {
                node = moveRedRight(node);
                cmp = key.compareTo(node.key); // Пересчитываем после операции
            }
            if (cmp == 0) {
                Node min = findMin(node.right);
                node.key = min.key;
                node.value = min.value;
                node.right = deleteMin(node.right);
            } else if (cmp > 0) {
                node.right = deleteNode(node.right, key);
            }
        }

        return balance(node);
    }

    // Удаление минимального узла
    private Node deleteMin(Node node) {
        if (node.left == null) {
            return null;
        }
        if (!isRed(node.left) && !isRed(node.left.left)) {
            node = moveRedLeft(node);
        }
        node.left = deleteMin(node.left);
        return balance(node);
    }

    // Перемещение красного узла влево
    private Node moveRedLeft(Node node) {
        flipColors(node);
        if (node.right != null && isRed(node.right.left)) {
            node.right = rotateRight(node.right);
            node = rotateLeft(node);
            flipColors(node);
        }
        return node;
    }

    // Перемещение красного узла вправо
    private Node moveRedRight(Node node) {
        flipColors(node);
        if (node.left != null && isRed(node.left.left)) {
            node = rotateRight(node);
            flipColors(node);
        }
        return node;
    }

    // Проверка цвета узла
    private boolean isRed(Node node) {
        return node != null && node.color == RED;
    }

    // Перекраска узла и его детей
    private void flipColors(Node node) {
        node.color = !node.color;
        if (node.left != null) {
            node.left.color = !node.left.color;
        }
        if (node.right != null) {
            node.right.color = !node.right.color;
        }
    }

    // Поиск узла
    private Node findNode(Node node, Integer key) {
        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp == 0) {
                return node;
            } else if (cmp < 0) {
                node = node.left;
            } else {
                node = node.right;
            }
        }
        return null;
    }

    // Поиск минимального узла
    private Node findMin(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    // Поиск максимального узла
    private Node findMax(Node node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    // Инфиксный обход для toString()
    private void inOrderToString(Node node, StringBuilder sb) {
        if (node != null) {
            inOrderToString(node.left, sb);
            sb.append(node.key).append("=").append(node.value).append(", ");
            inOrderToString(node.right, sb);
        }
    }

    // Поиск значения
    private boolean containsValueHelper(Node node, Object value) {
        if (node == null) {
            return false;
        }

        if (value == null ? node.value == null : value.equals(node.value)) {
            return true;
        }

        return containsValueHelper(node.left, value) || containsValueHelper(node.right, value);
    }

    // Сбор элементов для headMap
    private void collectHeadMap(Node node, Integer toKey, MyRbMap result) {
        if (node != null) {
            if (node.key.compareTo(toKey) < 0) {
                result.put(node.key, node.value);
                collectHeadMap(node.left, toKey, result);
                collectHeadMap(node.right, toKey, result);
            } else {
                collectHeadMap(node.left, toKey, result);
            }
        }
    }

    // Сбор элементов для tailMap
    private void collectTailMap(Node node, Integer fromKey, MyRbMap result) {
        if (node != null) {
            if (node.key.compareTo(fromKey) >= 0) {
                result.put(node.key, node.value);
                collectTailMap(node.left, fromKey, result);
                collectTailMap(node.right, fromKey, result);
            } else {
                collectTailMap(node.right, fromKey, result);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Дополнительные методы интерфейса Map         ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public Set<Integer> keySet() {
        Set<Integer> keys = new TreeSet<>();
        inOrderKeys(root, keys);
        return keys;
    }

    private void inOrderKeys(Node node, Set<Integer> keys) {
        if (node != null) {
            inOrderKeys(node.left, keys);
            keys.add(node.key);
            inOrderKeys(node.right, keys);
        }
    }

    @Override
    public Collection<String> values() {
        Collection<String> values = new ArrayList<>();
        inOrderValues(root, values);
        return values;
    }

    private void inOrderValues(Node node, Collection<String> values) {
        if (node != null) {
            inOrderValues(node.left, values);
            values.add(node.value);
            inOrderValues(node.right, values);
        }
    }

    @Override
    public Set<Entry<Integer, String>> entrySet() {
        Set<Entry<Integer, String>> entries = new TreeSet<>(Comparator.comparing(Entry::getKey));
        inOrderEntries(root, entries);
        return entries;
    }

    private void inOrderEntries(Node node, Set<Entry<Integer, String>> entries) {
        if (node != null) {
            inOrderEntries(node.left, entries);
            entries.add(new SimpleEntry<>(node.key, node.value));
            inOrderEntries(node.right, entries);
        }
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends String> map) {
        for (Entry<? extends Integer, ? extends String> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Comparator<? super Integer> comparator() {
        return null; // Естественный порядок
    }

    @Override
    public SortedMap<Integer, String> subMap(Integer fromKey, Integer toKey) {
        MyRbMap result = new MyRbMap();
        collectSubMap(root, fromKey, toKey, result);
        return result;
    }

    private void collectSubMap(Node node, Integer fromKey, Integer toKey, MyRbMap result) {
        if (node != null) {
            int cmpFrom = node.key.compareTo(fromKey);
            int cmpTo = node.key.compareTo(toKey);

            if (cmpFrom >= 0 && cmpTo < 0) {
                result.put(node.key, node.value);
            }

            if (cmpFrom > 0) {
                collectSubMap(node.left, fromKey, toKey, result);
            }
            if (cmpTo < 0) {
                collectSubMap(node.right, fromKey, toKey, result);
            }
        }
    }

    // Простая реализация Entry
    private static class SimpleEntry<K, V> implements Entry<K, V> {
        private final K key;
        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry<?, ?> entry)) return false;
            return Objects.equals(key, entry.getKey()) && Objects.equals(value, entry.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }

    // Простая реализация Objects
    private static class Objects {
        public static boolean equals(Object a, Object b) {
            return (a == b) || (a != null && a.equals(b));
        }

        public static int hash(Object... values) {
            int result = 1;
            for (Object element : values) {
                result = 31 * result + (element == null ? 0 : element.hashCode());
            }
            return result;
        }
    }
}
