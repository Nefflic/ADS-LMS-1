package by.it.group351051.gulidin.lesson14;

import java.util.*;


public class StatesHanoiTowerC {


    static class DSU {
        int[] parent;
        int[] size;

        DSU(int n) {
            parent = new int[n + 1];
            size = new int[n + 1];
            for (int i = 0; i <= n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }


        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX != rootY) {
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

    static void hanoi(int n, char from, char to, char aux, DSU uf, int[] heights, List<Integer> maxHeights) {
        if (n == 1) {
            // Перемещение одного диска с одной башни на другую
            heights[from - 'A']--;
            heights[to - 'A']++;
            // Находим максимальную высоту среди всех башен
            int maxHeight = Math.max(Math.max(heights[0], heights[1]), heights[2]);
            maxHeights.add(maxHeight);  // Добавляем в историю максимальную высоту
            return;
        }
        // Рекурсивный вызов для перемещения (n-1) дисков
        hanoi(n - 1, from, aux, to, uf, heights, maxHeights);
        // Перемещение одного диска с исходной башни на целевую
        heights[from - 'A']--;
        heights[to - 'A']++;
        // Добавляем максимальную высоту
        int maxHeight = Math.max(Math.max(heights[0], heights[1]), heights[2]);
        maxHeights.add(maxHeight);
        // Рекурсивный вызов для перемещения (n-1) дисков с вспомогательной на целевую башню
        hanoi(n - 1, aux, to, from, uf, heights, maxHeights);
    }

    public static void main(String[] args) {
        // Чтение количества дисков
        Scanner scanner = new Scanner(System.in);
        int N = scanner.nextInt();
        // Общее количество шагов для N дисков (формула 2^N - 1)
        int totalMoves = (int) Math.pow(2, N) - 1;

        // Инициализация структуры DSU для отслеживания состояний
        DSU uf = new DSU(totalMoves + 1);

        // Инициализация массива высот для каждой башни
        int[] heights = new int[3];
        heights[0] = N;  // Все диски начинают с первой башни

        // Список для хранения максимальных высот на каждом шаге
        List<Integer> maxHeights = new ArrayList<>();

        // Запуск рекурсивного решения задачи
        hanoi(N, 'A', 'B', 'C', uf, heights, maxHeights);

        // Группировка состояний по максимальной высоте на башнях
        Map<Integer, Integer> groupSizes = new HashMap<>();
        for (int height : maxHeights) {
            groupSizes.put(height, groupSizes.getOrDefault(height, 0) + 1);
        }

        // Сортировка групп по количеству состояний
        List<Integer> sortedGroups = new ArrayList<>(groupSizes.values());
        Collections.sort(sortedGroups);

        // Выводим результат: количество состояний для каждой уникальной высоты
        for (int size : sortedGroups) {
            System.out.print(size + " ");
        }
    }
}