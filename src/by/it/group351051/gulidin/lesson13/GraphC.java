package by.it.group351051.gulidin.lesson13;

import java.util.*;

public class GraphC {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();

        // Строим граф из входной строки
        Graph graph = parseGraph(input);

        // Находим компоненты сильной связности
        List<List<String>> sccs = findStronglyConnectedComponents(graph);

        // Определяем правильный порядок компонентов (исток -> сток)
        List<List<String>> orderedSCCs = orderComponentsByTopology(graph, sccs);

        // Выводим результат
        for (List<String> component : orderedSCCs) {
            Collections.sort(component); // Лексикографический порядок внутри компонента
            for (String vertex : component) {
                System.out.print(vertex);
            }
            System.out.println();
        }

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

        String[] edges = input.split(",");

        for (String edge : edges) {
            edge = edge.trim();

            if (edge.contains("->")) {
                String[] parts = edge.split("->");
                if (parts.length == 2) {
                    String from = parts[0].trim();
                    String to = parts[1].trim();
                    graph.addEdge(from, to);
                }
            }
        }

        return graph;
    }

    // Класс для алгоритма Тарьяна
    private static class TarjanSCC {
        private Graph graph;
        private Map<String, Integer> indices;
        private Map<String, Integer> lowlinks;
        private Set<String> onStack;
        private Stack<String> stack;
        private List<List<String>> sccs;
        private int index;

        public TarjanSCC(Graph graph) {
            this.graph = graph;
            this.indices = new HashMap<>();
            this.lowlinks = new HashMap<>();
            this.onStack = new HashSet<>();
            this.stack = new Stack<>();
            this.sccs = new ArrayList<>();
            this.index = 0;
        }

        public List<List<String>> findSCCs() {
            // Обрабатываем вершины в лексикографическом порядке
            List<String> vertices = new ArrayList<>(graph.getAllVertices());
            Collections.sort(vertices);

            for (String vertex : vertices) {
                if (!indices.containsKey(vertex)) {
                    strongConnect(vertex);
                }
            }

            return sccs;
        }

        private void strongConnect(String v) {
            // Устанавливаем индекс и lowlink для v
            indices.put(v, index);
            lowlinks.put(v, index);
            index++;

            // Помещаем v в стек
            stack.push(v);
            onStack.add(v);

            // Рассматриваем соседей v в лексикографическом порядке
            List<String> neighbors = new ArrayList<>(graph.getNeighbors(v));
            Collections.sort(neighbors);

            for (String w : neighbors) {
                if (!indices.containsKey(w)) {
                    // Сосед w не был посещен; рекурсивно вызываем strongConnect
                    strongConnect(w);
                    lowlinks.put(v, Math.min(lowlinks.get(v), lowlinks.get(w)));
                } else if (onStack.contains(w)) {
                    // Сосед w в стеке и следовательно в текущей SCC
                    lowlinks.put(v, Math.min(lowlinks.get(v), indices.get(w)));
                }
            }

            // Если v - корень SCC, извлекаем компоненту
            if (lowlinks.get(v).equals(indices.get(v))) {
                List<String> component = new ArrayList<>();
                String w;
                do {
                    w = stack.pop();
                    onStack.remove(w);
                    component.add(w);
                } while (!w.equals(v));

                sccs.add(component);
            }
        }
    }

    // Поиск компонент сильной связности с помощью алгоритма Тарьяна
    private static List<List<String>> findStronglyConnectedComponents(Graph graph) {
        TarjanSCC tarjan = new TarjanSCC(graph);
        return tarjan.findSCCs();
    }

    // Упорядочивание компонентов по топологии (исток -> сток)
    private static List<List<String>> orderComponentsByTopology(Graph graph, List<List<String>> sccs) {
        if (sccs.isEmpty()) {
            return new ArrayList<>();
        }

        // Создаем отображение вершина -> индекс компоненты
        Map<String, Integer> vertexToSCC = new HashMap<>();
        for (int i = 0; i < sccs.size(); i++) {
            for (String vertex : sccs.get(i)) {
                vertexToSCC.put(vertex, i);
            }
        }

        // Строим граф компонентов (конденсацию)
        Set<Integer>[] sccGraph = new Set[sccs.size()];
        for (int i = 0; i < sccs.size(); i++) {
            sccGraph[i] = new HashSet<>();
        }

        for (String vertex : graph.getAllVertices()) {
            for (String neighbor : graph.getNeighbors(vertex)) {
                int fromSCC = vertexToSCC.get(vertex);
                int toSCC = vertexToSCC.get(neighbor);
                if (fromSCC != toSCC) {
                    sccGraph[fromSCC].add(toSCC);
                }
            }
        }

        // Вычисляем входящие и исходящие степени компонентов
        int[] inDegree = new int[sccs.size()];
        int[] outDegree = new int[sccs.size()];

        for (int i = 0; i < sccs.size(); i++) {
            outDegree[i] = sccGraph[i].size();
            for (int neighbor : sccGraph[i]) {
                inDegree[neighbor]++;
            }
        }

        // Находим истоки и стоки
        List<Integer> sources = new ArrayList<>();
        List<Integer> sinks = new ArrayList<>();
        List<Integer> middle = new ArrayList<>();

        for (int i = 0; i < sccs.size(); i++) {
            if (inDegree[i] == 0) {
                sources.add(i);
            } else if (outDegree[i] == 0) {
                sinks.add(i);
            } else {
                middle.add(i);
            }
        }

        // Сортируем по лексикографическому порядку первой вершины в компоненте
        Comparator<Integer> comp = (a, b) -> {
            List<String> compA = new ArrayList<>(sccs.get(a));
            List<String> compB = new ArrayList<>(sccs.get(b));
            Collections.sort(compA);
            Collections.sort(compB);
            return compA.get(0).compareTo(compB.get(0));
        };

        sources.sort(comp);
        middle.sort(comp);
        sinks.sort(comp);

        // Формируем результат: истоки + средние + стоки
        List<List<String>> result = new ArrayList<>();

        for (int index : sources) {
            result.add(sccs.get(index));
        }
        for (int index : middle) {
            result.add(sccs.get(index));
        }
        for (int index : sinks) {
            result.add(sccs.get(index));
        }

        return result;
    }
}
