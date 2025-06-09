package by.it.group351051.gulidin.lesson13;

import java.util.*;

public class GraphA {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();

        // Строим граф из входной строки
        Graph graph = parseGraph(input);

        // Выполняем топологическую сортировку
        String result = topologicalSort(graph);

        // Выводим результат
        System.out.println(result);

        scanner.close();
    }

    // Внутренний класс для представления графа
    private static class Graph {
        private Map<String, List<String>> adjacencyList;
        private Set<String> allVertices;

        public Graph() {
            this.adjacencyList = new HashMap<>();
            this.allVertices = new HashSet<>();
        }

        public void addEdge(String from, String to) {
            adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
            allVertices.add(from);
            allVertices.add(to);
        }

        public Set<String> getAllVertices() {
            return allVertices;
        }

        public List<String> getNeighbors(String vertex) {
            return adjacencyList.getOrDefault(vertex, new ArrayList<>());
        }
    }

    // Парсинг входной строки в граф
    private static Graph parseGraph(String input) {
        Graph graph = new Graph();

        if (input.isEmpty()) {
            return graph;
        }

        // Разбиваем по запятым
        String[] edges = input.split(",");

        for (String edge : edges) {
            edge = edge.trim();

            if (edge.contains("->")) {
                // Разбиваем по стрелке
                String[] parts = edge.split("->");
                if (parts.length == 2) {
                    String from = parts[0].trim();
                    String to = parts[1].trim();

                    // Обрабатываем случай множественных целей (хотя в примере их нет)
                    String[] targets = to.split("\\s+");
                    for (String target : targets) {
                        if (!target.isEmpty()) {
                            graph.addEdge(from, target);
                        }
                    }
                }
            } else {
                // Изолированная вершина
                String vertex = edge.trim();
                if (!vertex.isEmpty()) {
                    graph.allVertices.add(vertex);
                }
            }
        }

        return graph;
    }

    // Топологическая сортировка с лексикографическим порядком
    private static String topologicalSort(Graph graph) {
        Set<String> allVertices = graph.getAllVertices();

        if (allVertices.isEmpty()) {
            return "";
        }

        // Вычисляем входящие степени
        Map<String, Integer> inDegree = new HashMap<>();
        for (String vertex : allVertices) {
            inDegree.put(vertex, 0);
        }

        for (String vertex : allVertices) {
            for (String neighbor : graph.getNeighbors(vertex)) {
                inDegree.put(neighbor, inDegree.get(neighbor) + 1);
            }
        }

        // Алгоритм Кана с лексикографическим порядком
        List<String> result = new ArrayList<>();

        while (result.size() < allVertices.size()) {
            // Находим все вершины с нулевой входящей степенью
            List<String> candidates = new ArrayList<>();
            for (String vertex : allVertices) {
                if (inDegree.get(vertex) == 0) {
                    candidates.add(vertex);
                }
            }

            if (candidates.isEmpty()) {
                // Цикл в графе - топологическая сортировка невозможна
                return "";
            }

            // Сортируем кандидатов лексикографически
            candidates.sort(String::compareTo);

            // Берем первый (лексикографически наименьший)
            String chosen = candidates.get(0);
            result.add(chosen);

            // Убираем выбранную вершину из рассмотрения
            inDegree.put(chosen, -1);

            // Уменьшаем входящие степени соседей
            for (String neighbor : graph.getNeighbors(chosen)) {
                if (inDegree.get(neighbor) > 0) {
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                }
            }
        }

        // Формируем результат
        return String.join(" ", result);
    }

    // Альтернативная реализация с приоритетной очередью
    private static String topologicalSortWithQueue(Graph graph) {
        Set<String> allVertices = graph.getAllVertices();

        if (allVertices.isEmpty()) {
            return "";
        }

        // Вычисляем входящие степени
        Map<String, Integer> inDegree = new HashMap<>();
        for (String vertex : allVertices) {
            inDegree.put(vertex, 0);
        }

        for (String vertex : allVertices) {
            for (String neighbor : graph.getNeighbors(vertex)) {
                inDegree.put(neighbor, inDegree.get(neighbor) + 1);
            }
        }

        // Используем приоритетную очередь для лексикографического порядка
        PriorityQueue<String> queue = new PriorityQueue<>();

        // Добавляем все вершины с нулевой входящей степенью
        for (String vertex : allVertices) {
            if (inDegree.get(vertex) == 0) {
                queue.offer(vertex);
            }
        }

        List<String> result = new ArrayList<>();

        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);

            // Обрабатываем всех соседей
            for (String neighbor : graph.getNeighbors(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);

                // Если входящая степень стала 0, добавляем в очередь
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // Проверяем на наличие циклов
        if (result.size() != allVertices.size()) {
            return ""; // Цикл в графе
        }

        return String.join(" ", result);
    }
}
