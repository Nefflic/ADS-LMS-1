package by.it.group351051.gulidin.lesson12;

import java.util.*;

public class MyAvlMap implements Map<Integer, String> {

    private Node root;
    private int mapSize;

    public MyAvlMap() {
        this.root = null;
        this.mapSize = 0;
    }

    // Внутренний класс для узла АВЛ-дерева
    private static class Node {
        Integer key;
        String value;
        Node left;
        Node right;
        int height;

        Node(Integer key, String value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
            this.height = 1;
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
        if (oldValue == null) {
            mapSize++;
        }
        return oldValue;
    }

    @Override
    public String remove(Object key) {
        if (key == null || !(key instanceof Integer)) {
            return null;
        }

        Integer intKey = (Integer) key;
        String oldValue = get(intKey);
        if (oldValue != null) {
            root = deleteNode(root, intKey);
            mapSize--;
        }
        return oldValue;
    }

    @Override
    public String get(Object key) {
        if (key == null || !(key instanceof Integer)) {
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

    /////////////////////////////////////////////////////////////////////////
    //////               Методы АВЛ-дерева                            ///////
    /////////////////////////////////////////////////////////////////////////

    // Получить высоту узла
    private int getHeight(Node node) {
        return node == null ? 0 : node.height;
    }

    // Получить баланс узла
    private int getBalance(Node node) {
        return node == null ? 0 : getHeight(node.left) - getHeight(node.right);
    }

    // Обновить высоту узла
    private void updateHeight(Node node) {
        if (node != null) {
            node.height = 1 + Math.max(getHeight(node.left), getHeight(node.right));
        }
    }

    // Правый поворот
    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        // Поворот
        x.right = y;
        y.left = T2;

        // Обновляем высоты
        updateHeight(y);
        updateHeight(x);

        return x;
    }

    // Левый поворот
    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        // Поворот
        y.left = x;
        x.right = T2;

        // Обновляем высоты
        updateHeight(x);
        updateHeight(y);

        return y;
    }

    // Вставка узла с балансировкой
    private Node insertNode(Node node, Integer key, String value) {
        // 1. Обычная вставка в BST
        if (node == null) {
            return new Node(key, value);
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

        // 2. Обновляем высоту предка
        updateHeight(node);

        // 3. Получаем баланс
        int balance = getBalance(node);

        // 4. Если узел несбалансирован, выполняем повороты

        // Left Left Case
        if (balance > 1 && key.compareTo(node.left.key) < 0) {
            return rotateRight(node);
        }

        // Right Right Case
        if (balance < -1 && key.compareTo(node.right.key) > 0) {
            return rotateLeft(node);
        }

        // Left Right Case
        if (balance > 1 && key.compareTo(node.left.key) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Right Left Case
        if (balance < -1 && key.compareTo(node.right.key) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    // Удаление узла с балансировкой
    private Node deleteNode(Node root, Integer key) {
        // 1. Обычное удаление в BST
        if (root == null) {
            return root;
        }

        int cmp = key.compareTo(root.key);
        if (cmp < 0) {
            root.left = deleteNode(root.left, key);
        } else if (cmp > 0) {
            root.right = deleteNode(root.right, key);
        } else {
            // Узел для удаления найден
            if (root.left == null || root.right == null) {
                Node temp = (root.left != null) ? root.left : root.right;

                if (temp == null) {
                    // Нет детей
                    temp = root;
                    root = null;
                } else {
                    // Один ребенок
                    root = temp;
                }
            } else {
                // Два ребенка: находим наименьший в правом поддереве
                Node temp = findMin(root.right);

                // Копируем данные наследника в этот узел
                root.key = temp.key;
                root.value = temp.value;

                // Удаляем наследника
                root.right = deleteNode(root.right, temp.key);
            }
        }

        if (root == null) {
            return root;
        }

        // 2. Обновляем высоту
        updateHeight(root);

        // 3. Получаем баланс
        int balance = getBalance(root);

        // 4. Балансировка

        // Left Left Case
        if (balance > 1 && getBalance(root.left) >= 0) {
            return rotateRight(root);
        }

        // Left Right Case
        if (balance > 1 && getBalance(root.left) < 0) {
            root.left = rotateLeft(root.left);
            return rotateRight(root);
        }

        // Right Right Case
        if (balance < -1 && getBalance(root.right) <= 0) {
            return rotateLeft(root);
        }

        // Right Left Case
        if (balance < -1 && getBalance(root.right) > 0) {
            root.right = rotateRight(root.right);
            return rotateLeft(root);
        }

        return root;
    }

    // Поиск узла
    private Node findNode(Node node, Integer key) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return node;
        } else if (cmp < 0) {
            return findNode(node.left, key);
        } else {
            return findNode(node.right, key);
        }
    }

    // Поиск минимального узла
    private Node findMin(Node node) {
        while (node.left != null) {
            node = node.left;
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

    /////////////////////////////////////////////////////////////////////////
    //////               Дополнительные методы интерфейса Map         ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public boolean containsValue(Object value) {
        return containsValueHelper(root, value);
    }

    private boolean containsValueHelper(Node node, Object value) {
        if (node == null) {
            return false;
        }

        if (value == null ? node.value == null : value.equals(node.value)) {
            return true;
        }

        return containsValueHelper(node.left, value) || containsValueHelper(node.right, value);
    }

    @Override
    public Set<Integer> keySet() {
        Set<Integer> keys = new HashSet<>();
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
        Set<Entry<Integer, String>> entries = new HashSet<>();
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

    // Простая реализация Entry
    private static class SimpleEntry<K, V> implements Entry<K, V> {
        private K key;
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
            if (!(o instanceof Entry)) return false;
            Entry<?, ?> entry = (Entry<?, ?>) o;
            return Objects.equals(key, entry.getKey()) && Objects.equals(value, entry.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }

    // Простая реализация Objects.equals и Objects.hash
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
