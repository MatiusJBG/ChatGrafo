package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

// --- CLASES AUXILIARES PARA UN EJEMPLO FUNCIONAL ---

class Community {
    private final String name;
    private final String color;
    private final Set<Integer> members = new HashSet<>();

    public Community(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public Set<Integer> getMembers() { return members; }
    public void addMember(int userId) { members.add(userId); }
}

class SocialGraph {
    private final Graph graph;
    private final Map<Integer, Set<Integer>> friendships = new HashMap<>();
    private final Map<Integer, String> userNames = new HashMap<>();

    public SocialGraph() {
        this.graph = new MultiGraph("SocialNetwork");
    }

    public void addUser(int id, String name) {
        if (!userNames.containsKey(id)) {
            userNames.put(id, name);
            friendships.put(id, new HashSet<>());
            graph.addNode(String.valueOf(id));
        }
    }

    public boolean addFriendship(int userId1, int userId2) {
        if (!userNames.containsKey(userId1) || !userNames.containsKey(userId2) || userId1 == userId2) {
            return false;
        }
        if (friendships.get(userId1).contains(userId2)) {
            return false;
        }
        friendships.get(userId1).add(userId2);
        friendships.get(userId2).add(userId1);
        String edgeId = "E" + Math.min(userId1, userId2) + "-" + Math.max(userId1, userId2);
        if (graph.getEdge(edgeId) == null) {
            graph.addEdge(edgeId, String.valueOf(userId1), String.valueOf(userId2));
        }
        return true;
    }

    public Set<Integer> getAllUserIds() {
        return userNames.keySet();
    }

    public Graph getGraphForDisplay(Map<Integer, String> comunidadPorUsuario) {
        for (Node node : graph) {
            int userId = Integer.parseInt(node.getId());
            String communityName = comunidadPorUsuario.get(userId);
            if (communityName != null) {
                node.setAttribute("ui.class", communityName.replace(" ", "_"));
            }
        }
        return graph;
    }
}


/**
 * Controlador principal de la red social.
 * Versión modificada para un layout estático con comunidades hexagonales separadas.
 */
public class SocialNetworkApp {
    private final SocialGraph network;
    private final Map<Integer, String> userNames;
    private final List<Community> communities = new ArrayList<>();
    private final Map<Integer, String> comunidadPorUsuario = new HashMap<>();
    private final Map<String, String> colorPorComunidad = new HashMap<>();

    public SocialNetworkApp() {
        System.setProperty("org.graphstream.ui", "swing");
        this.network = new SocialGraph();
        this.userNames = new HashMap<>();
    }
    
    // Carga usuarios desde un archivo CSV con formato: id,nombre
    public void loadUsersFromCSV(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                network.addUser(id, name);
                userNames.put(id, name);
            }
        } catch (Exception e) {
            System.err.println("Error leyendo usuarios desde CSV: " + e.getMessage());
            // Para el ejemplo, si falla, cargamos usuarios por defecto.
            loadDefaultUsers(50);
        }
    }

    // Método de respaldo para cargar usuarios si el CSV falla o no se proporciona
    public void loadDefaultUsers(int userCount) {
        for (int i = 1; i <= userCount; i++) {
            network.addUser(i, "User" + i);
            userNames.put(i, "User" + i);
        }
    }

    public void createCommunities(int numCommunities) {
        String[] colors = {"#E57373", "#81C784", "#64B5F6", "#FFF176", "#BA68C8", "#FFB74D", "#4DB6AC"};
        numCommunities = Math.min(numCommunities, colors.length);

        communities.clear();
        colorPorComunidad.clear();
        for (int i = 0; i < numCommunities; i++) {
            Community c = new Community("Comunidad " + (i + 1), colors[i]);
            communities.add(c);
            colorPorComunidad.put(c.getName(), c.getColor());
        }
        
        List<Integer> ids = new ArrayList<>(network.getAllUserIds());
        Collections.shuffle(ids, new Random());
        for (int i = 0; i < ids.size(); i++) {
            int communityIdx = i % numCommunities;
            communities.get(communityIdx).addMember(ids.get(i));
            comunidadPorUsuario.put(ids.get(i), communities.get(communityIdx).getName());
        }
    }

    public void createRandomFriendships(int minFriends, int maxFriends) {
        Random rand = new Random();
        for (Community community : communities) {
            List<Integer> ids = new ArrayList<>(community.getMembers());
            if (ids.size() < 2) continue;

            for (int id : ids) {
                int numFriends = Math.min(minFriends + rand.nextInt(maxFriends - minFriends + 1), ids.size() - 1);
                
                List<Integer> possibleFriends = new ArrayList<>(ids);
                possibleFriends.remove(Integer.valueOf(id));
                Collections.shuffle(possibleFriends, rand);

                int added = 0;
                for (int friendId : possibleFriends) {
                    if (added >= numFriends) break;
                    if (network.addFriendship(id, friendId)) {
                        added++;
                    }
                }
            }
        }
    }

    public void visualize() {
        Graph graph = network.getGraphForDisplay(comunidadPorUsuario);

        // --- INICIO DE LA LÓGICA DE LAYOUT ESTÁTICO ---
        
        // PARÁMETROS DE DISEÑO (ajusta estos valores para cambiar la separación)
        // Radio del círculo grande donde se posicionan las comunidades. Aumentado para separar más las comunidades.
        final double GLOBAL_RADIUS = 120.0; 
        // Radio del primer hexágono. Aumentado para separar más los nodos dentro de cada comunidad.
        final double HEXAGON_BASE_RADIUS = 14.0; 

        int nCommunities = communities.size();
        if (nCommunities == 0) return;

        // Bucle principal para posicionar cada comunidad
        for (int i = 0; i < nCommunities; i++) {
            Community community = communities.get(i);
            List<Integer> members = new ArrayList<>(community.getMembers());
            int nMembers = members.size();
            if (nMembers == 0) continue;

            // 1. Calcular el centro de la comunidad actual en un gran círculo
            double angleCommunity = 2 * Math.PI * i / nCommunities;
            double commCenterX = Math.cos(angleCommunity) * GLOBAL_RADIUS;
            double commCenterY = Math.sin(angleCommunity) * GLOBAL_RADIUS;

            // 2. Posicionar los miembros en hexágonos concéntricos alrededor del centro de la comunidad
            if (nMembers == 1) {
                 // Caso especial para una comunidad con un solo miembro
                graph.getNode(String.valueOf(members.get(0))).setAttribute("xy", commCenterX, commCenterY);
            } else {
                int membersPlaced = 0;
                // Coloca el primer miembro en el centro del hexágono
                graph.getNode(String.valueOf(members.get(0))).setAttribute("xy", commCenterX, commCenterY);
                membersPlaced++;
                
                // Organiza el resto en anillos hexagonales
                for (int h = 1; membersPlaced < nMembers; h++) {
                    double radius = HEXAGON_BASE_RADIUS * h;
                    int nodesInThisHex = 6 * h;
                    
                    for (int j = 0; j < nodesInThisHex && membersPlaced < nMembers; j++) {
                        int userId = members.get(membersPlaced);
                        double angle = (2 * Math.PI * j) / nodesInThisHex;
                        
                        double x = commCenterX + Math.cos(angle) * radius;
                        double y = commCenterY + Math.sin(angle) * radius;
                        
                        graph.getNode(String.valueOf(userId)).setAttribute("xy", x, y);
                        membersPlaced++;
                    }
                }
            }
        }

        // Asignar el nombre de la persona como etiqueta a cada nodo
        for (Node node : graph) {
            int userId = Integer.parseInt(node.getId());
            String nombre = userNames.get(userId);
            if (nombre != null) {
                node.setAttribute("ui.label", nombre);
            }
        }

        // --- FIN DE LA LÓGICA DE LAYOUT ESTÁTICO ---

        // --- DEFINICIÓN DEL ESTILO (CSS) ---
        StringBuilder styleSheet = new StringBuilder();
        styleSheet.append("graph { fill-mode: plain; fill-color: #F0F0F0; padding: 60px; }");
        
        styleSheet.append("node { " +
                "size: 15px; " +
                "shape: circle; " +
                "text-size: 12; " +
                "text-color: #222; " +
                "text-style: bold; " +
                "text-alignment: under; " +
                "stroke-mode: plain; " +
                "stroke-color: #555; " +
                "stroke-width: 1px; }");

        styleSheet.append("node:hover { stroke-color: #000; stroke-width: 2px; }");
        styleSheet.append("edge { size: 1.2px; fill-color: #AAB; }");
        
        for (Community c : communities) {
            String communityClass = c.getName().replace(" ", "_");
            styleSheet.append("node.").append(communityClass)
                      .append(" { fill-color: ").append(c.getColor()).append("; }");
        }
        
        graph.setAttribute("ui.stylesheet", styleSheet.toString());
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.zoom", 1.0); // Nivel de zoom inicial
        
        System.out.println("[INFO] Puedes hacer zoom con la rueda del ratón y mover el grafo arrastrando con el mouse.");
        
        graph.display(false);
    }

    public static void main(String[] args) {
        SocialNetworkApp app = new SocialNetworkApp();
        
        // Carga usuarios (intenta desde un CSV, si no, usa los de por defecto)
        // Crea un archivo "usuarios.csv" en la raíz de tu proyecto o cambia la ruta.
        // Formato: 1,Alice
        //          2,Bob
        app.loadUsersFromCSV("usuarios.csv");
        if (app.userNames.isEmpty()) {
            System.out.println("No se encontró 'usuarios.csv' o estaba vacío. Cargando 40 usuarios por defecto.");
            app.loadDefaultUsers(40);
        }
        
        app.createCommunities(5); // Crear 5 comunidades
        app.createRandomFriendships(1, 3); // Cada usuario tendrá entre 1 y 3 amigos en su comunidad
        
        app.visualize();
    }
}
