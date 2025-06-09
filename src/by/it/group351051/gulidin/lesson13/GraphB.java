package by.it.group351051.gulidin.lesson13;

import java.util.*;

public class GraphB {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();

        // Строим граф из входной строки
        Graph graph = parseGraph(input);

        // Проверяем наличие циклов
        boolean hasCycle = detectCycle(graph);

        // Выводим результат
        System.out.println(hasCycle ? "yes" : "no");

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

                    // Обрабатываем случай множественных целей
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

    // Цвета для DFS
    private enum Color {
        WHITE,  // Не посещена
        GRAY,   // В процессе обработки (в стеке рекурсии)
        BLACK   // Полностью обработана
    }

    // Основной метод обнаружения циклов через DFS
    private static boolean detectCycle(Graph graph) {
        Set<String> allVertices = graph.getAllVertices();

        if (allVertices.isEmpty()) {
            return false;
        }

        // Инициализируем все вершины как белые
        Map<String, Color> colors = new HashMap<>();
        for (String vertex : allVertices) {
            colors.put(vertex, Color.WHITE);
        }

        // Запускаем DFS для каждой непосещенной вершины
        for (String vertex : allVertices) {
            if (colors.get(vertex) == Color.WHITE) {
                if (dfsDetectCycle(graph, vertex, colors)) {
                    return true;
                }
            }
        }

        return false;
    }

    // DFS для обнаружения циклов
    private static boolean dfsDetectCycle(Graph graph, String vertex, Map<String, Color> colors) {
        // Помечаем вершину как серую (в процессе обработки)
        colors.put(vertex, Color.GRAY);

        // Проверяем всех соседей
        for (String neighbor : graph.getNeighbors(vertex)) {
            Color neighborColor = colors.get(neighbor);

            if (neighborColor == Color.GRAY) {
                // Нашли обратное ребро - цикл!
                return true;
            }

            if (neighborColor == Color.WHITE) {
                // Рекурсивно проверяем непосещенного соседа
                if (dfsDetectCycle(graph, neighbor, colors)) {
                    return true;
                }
            }

            // Если сосед черный, то это просто прямое или перекрестное ребро
        }

        // Помечаем вершину как черную (полностью обработана)
        colors.put(vertex, Color.BLACK);
        return false;
    }

    // Альтернативный метод через топологическую сортировку
    private static boolean detectCycleWithTopologicalSort(Graph graph) {
        Set<String> allVertices = graph.getAllVertices();

        if (allVertices.isEmpty()) {
            return false;
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

        // Алгоритм Кана
        Queue<String> queue = new LinkedList<>();

        // Добавляем все вершины с нулевой входящей степенью
        for (String vertex : allVertices) {
            if (inDegree.get(vertex) == 0) {
                queue.offer(vertex);
            }
        }

        int processedVertices = 0;

        while (!queue.isEmpty()) {
            String current = queue.poll();
            processedVertices++;

            // Обрабатываем всех соседей
            for (String neighbor : graph.getNeighbors(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);

                // Если входящая степень стала 0, добавляем в очередь
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // Если обработали не все вершины, значит есть цикл
        return processedVertices != allVertices.size();
    }

    // Итеративная версия DFS (избегает переполнения стека)
    private static boolean detectCycleIterative(Graph graph) {
        Set<String> allVertices = graph.getAllVertices();

        if (allVertices.isEmpty()) {
            return false;
        }

        Map<String, Color> colors = new HashMap<>();
        for (String vertex : allVertices) {
            colors.put(vertex, Color.WHITE);
        }

        for (String startVertex : allVertices) {
            if (colors.get(startVertex) == Color.WHITE) {
                if (dfsIterative(graph, startVertex, colors)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean dfsIterative(Graph graph, String startVertex, Map<String, Color> colors) {
        Stack<String> stack = new Stack<>();
        Stack<Boolean> isReturning = new Stack<>(); // true если возвращаемся из рекурсии

        stack.push(startVertex);
        isReturning.push(false);

        while (!stack.isEmpty()) {
            String vertex = stack.peek();
            boolean returning = isReturning.peek();

            if (returning) {
                // Возвращаемся из "рекурсии" - помечаем как черную
                stack.pop();
                isReturning.pop();
                colors.put(vertex, Color.BLACK);
            } else {
                // Первое посещение вершины
                colors.put(vertex, Color.GRAY);
                isReturning.pop();
                isReturning.push(true); // При следующем посещении будем возвращаться

                // Добавляем соседей в стек
                for (String neighbor : graph.getNeighbors(vertex)) {
                    Color neighborColor = colors.get(neighbor);

                    if (neighborColor == Color.GRAY) {
                        return true; // Цикл найден
                    }

                    if (neighborColor == Color.WHITE) {
                        stack.push(neighbor);
                        isReturning.push(false);
                    }
                }
            }
        }

        return false;
    }
}
