package by.it.group351051.gulidin.lesson14;

import java.util.*;

public class SitesB {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Хранение уникальных идентификаторов для каждого сайта
        Map<String, Integer> id = new HashMap<>();

        // Список пар связанных сайтов
        List<String[]> pairs = new ArrayList<>();

        // Индекс для уникальных идентификаторов
        int index = 0;

        // Чтение пар сайтов, пока не встретится строка "end"
        while (true) {
            String line = sc.nextLine();
            if (line.equals("end")) break;

            // Разделение строки на два сайта
            String[] split = line.split("\\+");
            String a = split[0];
            String b = split[1];

            // Присваиваем уникальные идентификаторы каждому сайту, если это еще не сделано
            if (!id.containsKey(a)) id.put(a, index++);
            if (!id.containsKey(b)) id.put(b, index++);

            // Добавляем пару сайтов в список
            pairs.add(new String[]{a, b});
        }

        // Инициализация структуры DSU для объединения сайтов
        DSU dsu = new DSU(index);

        // Объединение сайтов по найденным связям
        for (String[] pair : pairs) {
            dsu.union(id.get(pair[0]), id.get(pair[1]));
        }

        // Группировка сайтов по их родителям и подсчет размеров групп
        Map<Integer, Integer> groupSizes = new HashMap<>();
        for (int i = 0; i < index; i++) {
            int root = dsu.find(i);  // Находим родительский элемент для сайта
            groupSizes.put(root, groupSizes.getOrDefault(root, 0) + 1);  // Увеличиваем размер группы
        }

        // Получаем список размеров групп и сортируем их по убыванию
        List<Integer> sizes = new ArrayList<>(groupSizes.values());
        sizes.sort(Collections.reverseOrder());

        // Выводим количество сайтов в каждой группе
        for (int size : sizes) {
            System.out.print(size + " ");
        }
    }


    static class DSU {
        int[] parent;  // Массив для хранения родителей сайтов


        DSU(int size) {
            parent = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;  // Каждому сайту присваиваем его собственный индекс как родителя
            }
        }


        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);  // Сжимаем путь, делая родителем непосредственного предка
            }
            return parent[x];
        }


        void union(int x, int y) {
            parent[find(x)] = find(y);  // Объединяем группы, присваивая родителя одного из сайтов
        }
    }
}