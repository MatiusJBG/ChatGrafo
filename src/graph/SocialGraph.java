package graph;

import user.User;
import java.util.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.Node;

/**
 * Representa la red social como un grafo usando listas de adyacencia y GraphStream para visualización.
 */
public class SocialGraph {
    private final Map<Integer, User> users;
    private final Map<Integer, List<Integer>> adjList;

    public SocialGraph() {
        users = new HashMap<>();
        adjList = new HashMap<>();
    }

    /**
     * Agrega un usuario a la red social.
     */
    public boolean addUser(int id, String name) {
        if (users.containsKey(id)) return false;
        users.put(id, new User(id, name));
        adjList.put(id, new ArrayList<>());
        return true;
    }

    /**
     * Elimina un usuario de la red social.
     */
    public boolean removeUser(int id) {
        if (!users.containsKey(id)) return false;
        users.remove(id);
        adjList.remove(id);
        for (List<Integer> friends : adjList.values()) {
            friends.remove(Integer.valueOf(id));
        }
        return true;
    }

    /**
     * Agrega una amistad entre dos usuarios.
     */
    public boolean addFriendship(int id1, int id2) {
        if (!users.containsKey(id1) || !users.containsKey(id2) || id1 == id2) return false;
        if (!adjList.get(id1).contains(id2)) adjList.get(id1).add(id2);
        if (!adjList.get(id2).contains(id1)) adjList.get(id2).add(id1);
        users.get(id1).addFriend(id2);
        users.get(id2).addFriend(id1);
        return true;
    }

    /**
     * Elimina una amistad entre dos usuarios.
     */
    public boolean removeFriendship(int id1, int id2) {
        if (!users.containsKey(id1) || !users.containsKey(id2)) return false;
        adjList.get(id1).remove(Integer.valueOf(id2));
        adjList.get(id2).remove(Integer.valueOf(id1));
        users.get(id1).removeFriend(id2);
        users.get(id2).removeFriend(id1);
        return true;
    }

    /**
     * Devuelve el objeto User dado su ID.
     */
    public User getUser(int id) {
        return users.get(id);
    }

    /**
     * Devuelve la lista de amigos de un usuario.
     */
    public List<Integer> getFriends(int id) {
        return adjList.getOrDefault(id, new ArrayList<>());
    }

    /**
     * Devuelve el conjunto de todos los IDs de usuario en la red.
     */
    public Set<Integer> getAllUserIds() {
        return users.keySet();
    }

    /**
     * Devuelve un grafo GraphStream para visualización.
     */
    public Graph getGraphForDisplay(Map<Integer, String> communityByUser, Map<String, String> colorByCommunity) {
        Graph graph = new SingleGraph("Social Network");
        for (int id : users.keySet()) {
            Node node = graph.addNode(String.valueOf(id));
            node.setAttribute("ui.label", users.get(id).getName());
            String community = communityByUser.get(id);
            if (community != null) {
                node.setAttribute("ui.class", community.replace(" ", "_"));
                node.setAttribute("ui.group", community.replace(" ", "_"));
            }
        }
        for (int id : users.keySet()) {
            for (int friendId : adjList.get(id)) {
                if (id < friendId) {
                    String edgeId = id + "_" + friendId;
                    if (graph.getEdge(edgeId) == null) {
                        graph.addEdge(edgeId, String.valueOf(id), String.valueOf(friendId));
                    }
                }
            }
        }
        return graph;
    }
}
