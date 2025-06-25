package analysis;

import graph.SocialGraph;
import user.User;
import java.util.*;

/**
 * Proporciona funciones de análisis para la red social.
 */
public class SocialAnalysis {
    /**
     * Encuentra los amigos en común entre dos usuarios.
     */
    public static List<Integer> commonFriends(SocialGraph graph, int userId1, int userId2) {
        List<Integer> friends1 = graph.getFriends(userId1);
        List<Integer> friends2 = graph.getFriends(userId2);
        List<Integer> common = new ArrayList<>(friends1);
        common.retainAll(friends2);
        return common;
    }

    /**
     * Sugiere amigos para un usuario (amigos de amigos que no son amigos directos).
     */
    public static List<Integer> suggestFriends(SocialGraph graph, int userId) {
        Set<Integer> suggestions = new HashSet<>();
        List<Integer> friends = graph.getFriends(userId);
        Set<Integer> directFriends = new HashSet<>(friends);
        directFriends.add(userId);
        for (int friendId : friends) {
            for (int fof : graph.getFriends(friendId)) {
                if (!directFriends.contains(fof)) {
                    suggestions.add(fof);
                }
            }
        }
        return new ArrayList<>(suggestions);
    }

    /**
     * Encuentra las comunidades (componentes conexas) en la red social.
     */
    public static List<Set<Integer>> findCommunities(SocialGraph graph) {
        Set<Integer> visited = new HashSet<>();
        List<Set<Integer>> communities = new ArrayList<>();
        for (int userId : graph.getAllUserIds()) {
            if (!visited.contains(userId)) {
                Set<Integer> community = new HashSet<>();
                Queue<Integer> queue = new LinkedList<>();
                queue.add(userId);
                visited.add(userId);
                while (!queue.isEmpty()) {
                    int current = queue.poll();
                    community.add(current);
                    for (int neighbor : graph.getFriends(current)) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
                communities.add(community);
            }
        }
        return communities;
    }

    /**
     * Calcula los usuarios más influyentes (mayor centralidad de grado).
     */
    public static List<Integer> mostInfluentialUsers(SocialGraph graph) {
        int maxDegree = -1;
        List<Integer> influential = new ArrayList<>();
        for (int userId : graph.getAllUserIds()) {
            int degree = graph.getFriends(userId).size();
            if (degree > maxDegree) {
                maxDegree = degree;
                influential.clear();
                influential.add(userId);
            } else if (degree == maxDegree) {
                influential.add(userId);
            }
        }
        return influential;
    }
}
