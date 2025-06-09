package by.it.group351051.gulidin.lesson12;

import java.util.*;

public class MySplayMap implements NavigableMap<Integer, String> {

    private Node root;
    private int mapSize;

    public MySplayMap() {
        this.root = null;
        this.mapSize = 0;
    }

    // Внутренний класс для узла splay-дерева
    private static class Node {
        Integer key;
        String value;
        Node left;
        Node right;

        Node(Integer key, String value) {
            this.key = key;
            this.value = value;
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

        if (root == null) {
            root = new Node(key, value);
            mapSize++;
            return null;
        }

        root = splay(root, key);
        int cmp = key.compareTo(root.key);

        if (cmp == 0) {
            String oldValue = root.value;
            root.value = value;
            return oldValue;
        }

        Node newNode = new Node(key, value);
        if (cmp < 0) {
            newNode.left = root.left;
            newNode.right = root;
            root.left = null;
        } else {
            newNode.right = root.right;
            newNode.left = root;
            root.right = null;
        }

        root = newNode;
        mapSize++;
        return null;
    }

    @Override
    public String remove(Object key) {
        if (!(key instanceof Integer) || root == null) {
            return null;
        }

        Integer intKey = (Integer) key;
        root = splay(root, intKey);

        if (!intKey.equals(root.key)) {
            return null;
        }

        String oldValue = root.value;

        if (root.left == null) {
            root = root.right;
        } else if (root.right == null) {
            root = root.left;
        } else {
            Node leftTree = root.left;
            root = root.right;
            root = splay(root, intKey);
            root.left = leftTree;
        }

        mapSize--;
        return oldValue;
    }

    @Override
    public String get(Object key) {
        if (!(key instanceof Integer) || root == null) {
            return null;
        }

        Integer intKey = (Integer) key;
        root = splay(root, intKey);

        if (intKey.equals(root.key)) {
            return root.value;
        }
        return null;
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
    public NavigableMap<Integer, String> headMap(Integer toKey) {
        return headMap(toKey, false);
    }

    @Override
    public NavigableMap<Integer, String> tailMap(Integer fromKey) {
        return tailMap(fromKey, true);
    }

    @Override
    public Integer firstKey() {
        if (root == null) {
            throw new RuntimeException("NoSuchElementException");
        }
        Node min = findMin(root);
        root = splay(root, min.key);
        return min.key;
    }

    @Override
    public Integer lastKey() {
        if (root == null) {
            throw new RuntimeException("NoSuchElementException");
        }
        Node max = findMax(root);
        root = splay(root, max.key);
        return max.key;
    }

    @Override
    public Integer lowerKey(Integer key) {
        if (key == null || root == null) {
            return null;
        }

        Node result = findLower(root, key);
        if (result != null) {
            root = splay(root, result.key);
            return result.key;
        }
        return null;
    }

    @Override
    public Integer floorKey(Integer key) {
        if (key == null || root == null) {
            return null;
        }

        Node result = findFloor(root, key);
        if (result != null) {
            root = splay(root, result.key);
            return result.key;
        }
        return null;
    }

    @Override
    public Integer ceilingKey(Integer key) {
        if (key == null || root == null) {
            return null;
        }

        Node result = findCeiling(root, key);
        if (result != null) {
            root = splay(root, result.key);
            return result.key;
        }
        return null;
    }

    @Override
    public Integer higherKey(Integer key) {
        if (key == null || root == null) {
            return null;
        }

        Node result = findHigher(root, key);
        if (result != null) {
            root = splay(root, result.key);
            return result.key;
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Методы splay-дерева                          ///////
    /////////////////////////////////////////////////////////////////////////

    // Главная операция splay - всплытие узла с заданным ключом к корню
    private Node splay(Node node, Integer key) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);

        if (cmp < 0) {
            if (node.left == null) {
                return node;
            }

            int cmp2 = key.compareTo(node.left.key);
            if (cmp2 < 0) {
                // Zig-Zig (Left-Left)
                node.left.left = splay(node.left.left, key);
                node = rotateRight(node);
            } else if (cmp2 > 0) {
                // Zig-Zag (Left-Right)
                node.left.right = splay(node.left.right, key);
                if (node.left.right != null) {
                    node.left = rotateLeft(node.left);
                }
            }

            if (node.left != null) {
                node = rotateRight(node);
            }
        } else if (cmp > 0) {
            if (node.right == null) {
                return node;
            }

            int cmp2 = key.compareTo(node.right.key);
            if (cmp2 > 0) {
                // Zig-Zig (Right-Right)
                node.right.right = splay(node.right.right, key);
                node = rotateLeft(node);
            } else if (cmp2 < 0) {
                // Zig-Zag (Right-Left)
                node.right.left = splay(node.right.left, key);
                if (node.right.left != null) {
                    node.right = rotateRight(node.right);
                }
            }

            if (node.right != null) {
                node = rotateLeft(node);
            }
        }

        return node;
    }

    // Правый поворот
    private Node rotateRight(Node node) {
        Node newRoot = node.left;
        node.left = newRoot.right;
        newRoot.right = node;
        return newRoot;
    }

    // Левый поворот
    private Node rotateLeft(Node node) {
        Node newRoot = node.right;
        node.right = newRoot.left;
        newRoot.left = node;
        return newRoot;
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

    // Поиск наибольшего ключа меньше заданного
    private Node findLower(Node node, Integer key) {
        Node result = null;
        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp > 0) {
                result = node;
                node = node.right;
            } else {
                node = node.left;
            }
        }
        return result;
    }

    // Поиск наибольшего ключа меньше или равного заданному
    private Node findFloor(Node node, Integer key) {
        Node result = null;
        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp >= 0) {
                result = node;
                node = node.right;
            } else {
                node = node.left;
            }
        }
        return result;
    }

    // Поиск наименьшего ключа больше или равного заданному
    private Node findCeiling(Node node, Integer key) {
        Node result = null;
        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp <= 0) {
                result = node;
                node = node.left;
            } else {
                node = node.right;
            }
        }
        return result;
    }

    // Поиск наименьшего ключа больше заданного
    private Node findHigher(Node node, Integer key) {
        Node result = null;
        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp < 0) {
                result = node;
                node = node.left;
            } else {
                node = node.right;
            }
        }
        return result;
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

    /////////////////////////////////////////////////////////////////////////
    //////               Дополнительные методы NavigableMap           ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public Entry<Integer, String> lowerEntry(Integer key) {
        Integer lowerKey = lowerKey(key);
        return lowerKey != null ? new SimpleEntry<>(lowerKey, get(lowerKey)) : null;
    }

    @Override
    public Entry<Integer, String> floorEntry(Integer key) {
        Integer floorKey = floorKey(key);
        return floorKey != null ? new SimpleEntry<>(floorKey, get(floorKey)) : null;
    }

    @Override
    public Entry<Integer, String> ceilingEntry(Integer key) {
        Integer ceilingKey = ceilingKey(key);
        return ceilingKey != null ? new SimpleEntry<>(ceilingKey, get(ceilingKey)) : null;
    }

    @Override
    public Entry<Integer, String> higherEntry(Integer key) {
        Integer higherKey = higherKey(key);
        return higherKey != null ? new SimpleEntry<>(higherKey, get(higherKey)) : null;
    }

    @Override
    public Entry<Integer, String> firstEntry() {
        if (root == null) {
            return null;
        }
        Integer first = firstKey();
        return new SimpleEntry<>(first, get(first));
    }

    @Override
    public Entry<Integer, String> lastEntry() {
        if (root == null) {
            return null;
        }
        Integer last = lastKey();
        return new SimpleEntry<>(last, get(last));
    }

    @Override
    public Entry<Integer, String> pollFirstEntry() {
        Entry<Integer, String> entry = firstEntry();
        if (entry != null) {
            remove(entry.getKey());
        }
        return entry;
    }

    @Override
    public Entry<Integer, String> pollLastEntry() {
        Entry<Integer, String> entry = lastEntry();
        if (entry != null) {
            remove(entry.getKey());
        }
        return entry;
    }

    @Override
    public NavigableMap<Integer, String> descendingMap() {
        // Простая реализация - создаем новый map в обратном порядке
        MySplayMap result = new MySplayMap();
        collectDescending(root, result);
        return result;
    }

    private void collectDescending(Node node, MySplayMap result) {
        if (node != null) {
            collectDescending(node.right, result);
            result.put(node.key, node.value);
            collectDescending(node.left, result);
        }
    }

    @Override
    public NavigableSet<Integer> navigableKeySet() {
        return keySet();
    }

    @Override
    public NavigableSet<Integer> descendingKeySet() {
        NavigableSet<Integer> result = new TreeSet<>(Collections.reverseOrder());
        collectKeys(root, result);
        return result;
    }

    @Override
    public NavigableMap<Integer, String> subMap(Integer fromKey, boolean fromInclusive,
                                                Integer toKey, boolean toInclusive) {
        MySplayMap result = new MySplayMap();
        collectSubMap(root, fromKey, fromInclusive, toKey, toInclusive, result);
        return result;
    }

    @Override
    public NavigableMap<Integer, String> headMap(Integer toKey, boolean inclusive) {
        MySplayMap result = new MySplayMap();
        collectHeadMap(root, toKey, inclusive, result);
        return result;
    }

    @Override
    public NavigableMap<Integer, String> tailMap(Integer fromKey, boolean inclusive) {
        MySplayMap result = new MySplayMap();
        collectTailMap(root, fromKey, inclusive, result);
        return result;
    }

    // Вспомогательные методы для сбора элементов
    private void collectSubMap(Node node, Integer fromKey, boolean fromInclusive,
                               Integer toKey, boolean toInclusive, MySplayMap result) {
        if (node != null) {
            int cmpFrom = node.key.compareTo(fromKey);
            int cmpTo = node.key.compareTo(toKey);

            boolean includeNode = (fromInclusive ? cmpFrom >= 0 : cmpFrom > 0) &&
                    (toInclusive ? cmpTo <= 0 : cmpTo < 0);

            if (includeNode) {
                result.put(node.key, node.value);
            }

            if (cmpFrom > 0 || (fromInclusive && cmpFrom >= 0)) {
                collectSubMap(node.left, fromKey, fromInclusive, toKey, toInclusive, result);
            }
            if (cmpTo < 0 || (toInclusive && cmpTo <= 0)) {
                collectSubMap(node.right, fromKey, fromInclusive, toKey, toInclusive, result);
            }
        }
    }

    private void collectHeadMap(Node node, Integer toKey, boolean inclusive, MySplayMap result) {
        if (node != null) {
            int cmp = node.key.compareTo(toKey);
            if (inclusive ? cmp <= 0 : cmp < 0) {
                result.put(node.key, node.value);
                collectHeadMap(node.left, toKey, inclusive, result);
                collectHeadMap(node.right, toKey, inclusive, result);
            } else {
                collectHeadMap(node.left, toKey, inclusive, result);
            }
        }
    }

    private void collectTailMap(Node node, Integer fromKey, boolean inclusive, MySplayMap result) {
        if (node != null) {
            int cmp = node.key.compareTo(fromKey);
            if (inclusive ? cmp >= 0 : cmp > 0) {
                result.put(node.key, node.value);
                collectTailMap(node.left, fromKey, inclusive, result);
                collectTailMap(node.right, fromKey, inclusive, result);
            } else {
                collectTailMap(node.right, fromKey, inclusive, result);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Дополнительные методы интерфейса Map         ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public NavigableSet<Integer> keySet() {
        NavigableSet<Integer> keys = new TreeSet<>();
        collectKeys(root, keys);
        return keys;
    }

    private void collectKeys(Node node, Set<Integer> keys) {
        if (node != null) {
            collectKeys(node.left, keys);
            keys.add(node.key);
            collectKeys(node.right, keys);
        }
    }

    @Override
    public Collection<String> values() {
        Collection<String> values = new ArrayList<>();
        collectValues(root, values);
        return values;
    }

    private void collectValues(Node node, Collection<String> values) {
        if (node != null) {
            collectValues(node.left, values);
            values.add(node.value);
            collectValues(node.right, values);
        }
    }

    @Override
    public Set<Entry<Integer, String>> entrySet() {
        Set<Entry<Integer, String>> entries = new TreeSet<>(Comparator.comparing(Entry::getKey));
        collectEntries(root, entries);
        return entries;
    }

    private void collectEntries(Node node, Set<Entry<Integer, String>> entries) {
        if (node != null) {
            collectEntries(node.left, entries);
            entries.add(new SimpleEntry<>(node.key, node.value));
            collectEntries(node.right, entries);
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
        return subMap(fromKey, true, toKey, false);
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
