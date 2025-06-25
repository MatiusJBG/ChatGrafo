package graph;

import java.util.*;

/**
 * Clase utilitaria para recorridos en grafos (BFS, DFS) y camino más corto.
 */
public class GraphTraversal {
    /**
     * Recorrido en anchura (BFS) desde un nodo inicial.
     */
    public static List<Integer> bfs(Map<Integer, List<Integer>> adjList, int startId) {
        List<Integer> visitedOrder = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(startId);
        visited.add(startId);
        while (!queue.isEmpty()) {
            int current = queue.poll();
            visitedOrder.add(current);
            for (int neighbor : adjList.getOrDefault(current, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return visitedOrder;
    }

    /**
     * Recorrido en profundidad (DFS) desde un nodo inicial.
     */
    public static List<Integer> dfs(Map<Integer, List<Integer>> adjList, int startId) {
        List<Integer> visitedOrder = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        dfsHelper(adjList, startId, visited, visitedOrder);
        return visitedOrder;
    }

    private static void dfsHelper(Map<Integer, List<Integer>> adjList, int current, Set<Integer> visited, List<Integer> order) {
        visited.add(current);
        order.add(current);
        for (int neighbor : adjList.getOrDefault(current, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                dfsHelper(adjList, neighbor, visited, order);
            }
        }
    }

    /**
     * Encuentra el camino más corto entre dos nodos usando BFS.
     */
    public static List<Integer> shortestPath(Map<Integer, List<Integer>> adjList, int startId, int endId) {
        Map<Integer, Integer> prev = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        queue.add(startId);
        visited.add(startId);
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == endId) break;
            for (int neighbor : adjList.getOrDefault(current, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    prev.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        // Reconstruye el camino
        List<Integer> path = new LinkedList<>();
        for (Integer at = endId; at != null; at = prev.get(at)) {
            path.add(0, at);
            if (at == startId) break;
        }
        if (path.isEmpty() || path.get(0) != startId) return new ArrayList<>(); // No hay camino
        return path;
    }
}
