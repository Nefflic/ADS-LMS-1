package by.it.group351051.gulidin.lesson14;

import java.util.*;


public class PointsA {


    static class DSU {
        int[] parent;  // Массив для хранения родителей элементов
        int[] size;    // Массив для хранения размера группы


        DSU(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;  // Каждая точка изначально является своим родителем
                size[i] = 1;    // Начальный размер группы для каждой точки равен 1
            }
        }


        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);  // Сжимаем путь
            }
            return parent[x];
        }


        void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX != rootY) {
                // Объединяем меньшую группу в большую, чтобы минимизировать глубину дерева
                if (size[rootX] < size[rootY]) {
                    int temp = rootX;
                    rootX = rootY;
                    rootY = temp;
                }
                parent[rootY] = rootX;
                size[rootX] += size[rootY];
            }
        }
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Чтение расстояния для сравнения
        int distance = scanner.nextInt();
        int maxDistSquared = distance * distance;  // Считываем квадрат расстояния для удобства сравнения
        int n = scanner.nextInt();  // Читаем количество точек

        // Массив для хранения координат точек (x, y, z)
        int[][] points = new int[n][3];
        for (int i = 0; i < n; i++) {
            points[i][0] = scanner.nextInt();
            points[i][1] = scanner.nextInt();
            points[i][2] = scanner.nextInt();
        }

        // Инициализация структуры DSU для объединения точек в группы
        DSU dsu = new DSU(n);

        // Для каждой пары точек вычисляем квадрат расстояния и объединяем точки,
        // если их расстояние меньше заданного
        for (int i = 0; i < n; i++) {
            int[] a = points[i];
            for (int j = i + 1; j < n; j++) {
                int[] b = points[j];
                int dx = a[0] - b[0];  // Разность по оси X
                int dy = a[1] - b[1];  // Разность по оси Y
                int dz = a[2] - b[2];  // Разность по оси Z
                int distSq = dx * dx + dy * dy + dz * dz;  // Квадрат расстояния между точками
                if (distSq < maxDistSquared) {
                    // Если расстояние меньше, то объединяем точки в одну группу
                    dsu.union(i, j);
                }
            }
        }

        // Группируем точки по их корням (т.е. по родителям)
        Map<Integer, Integer> groupSizes = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = dsu.find(i);  // Находим корень для каждой точки
            groupSizes.put(root, groupSizes.getOrDefault(root, 0) + 1);  // Увеличиваем размер группы
        }

        // Получаем список размеров групп и сортируем их по убыванию
        List<Integer> result = new ArrayList<>(groupSizes.values());
        result.sort(Collections.reverseOrder());

        // Выводим результаты: количество точек в каждой группе
        for (int size : result) {
            System.out.print(size + " ");
        }
    }
}
