package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;
import graph.SocialGraph;
import user.User;

public class Main {
    private static SocialGraph network;
    private static Scanner scanner;
    private static final String CSV_PATH = ".github/src/data/nombres.csv";  // Asegúrate de que el archivo esté en esta ruta

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        network = new SocialGraph();

        // Cargar usuarios desde el CSV
        loadUsersFromCSV(CSV_PATH);

        boolean exit = false;
        while (!exit) {
            printMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea

            switch (choice) {
                case 1:
                    viewFriends();
                    break;
                case 2:
                    addFriendship();
                    break;
                case 3:
                    removeFriendship();
                    break;
                case 4:
                    removeUser();
                    break;
                case 5:
                    visualizeNetwork();
                    break;
                case 6:
                    exit = true;
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n--- Menú de la Red Social ---");
        System.out.println("1. Ver amigos de un usuario");
        System.out.println("2. Agregar amistad");
        System.out.println("3. Eliminar amistad");
        System.out.println("4. Eliminar usuario");
        System.out.println("5. Visualizar red");
        System.out.println("6. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static void loadUsersFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("# id") || line.trim().isEmpty() || !line.contains(",")) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        network.addUser(id, name);
                    } catch (NumberFormatException e) {
                        System.err.println("Error: ID no válido en línea - " + line);
                    }
                }
            }
            System.out.println("Usuarios cargados exitosamente desde " + filePath);
            System.out.println("Total de usuarios registrados: " + network.getAllUserIds().size());
            
        } catch (Exception e) {
            System.err.println("Error crítico al cargar el CSV: " + e.getMessage());
            System.out.println("Cargando usuarios de respaldo...");
            loadMinimumSampleUsers();
        }
    }

private static void loadMinimumSampleUsers() {
    // Lista de 100 usuarios predefinidos
    String[][] users = {
        {"1", "Alice"}, {"2", "Bob"}, {"3", "Charlie"}, {"4", "David"}, {"5", "Eve"},
        {"6", "Frank"}, {"7", "Grace"}, {"8", "Henry"}, {"9", "Ivy"}, {"10", "Jack"},
        {"11", "Karen"}, {"12", "Liam"}, {"13", "Mia"}, {"14", "Noah"}, {"15", "Olivia"},
        {"16", "Peter"}, {"17", "Quinn"}, {"18", "Rachel"}, {"19", "Sam"}, {"20", "Tina"},
        {"21", "Uma"}, {"22", "Victor"}, {"23", "Wendy"}, {"24", "Xander"}, {"25", "Yara"},
        {"26", "Zack"}, {"27", "Amy"}, {"28", "Brian"}, {"29", "Cindy"}, {"30", "Daniel"},
        {"31", "Emma"}, {"32", "Fiona"}, {"33", "George"}, {"34", "Hannah"}, {"35", "Ian"},
        {"36", "Julia"}, {"37", "Kevin"}, {"38", "Laura"}, {"39", "Mike"}, {"40", "Nina"},
        {"41", "Oscar"}, {"42", "Paula"}, {"43", "Quentin"}, {"44", "Rita"}, {"45", "Steve"},
        {"46", "Tara"}, {"47", "Ulysses"}, {"48", "Vera"}, {"49", "Walter"}, {"50", "Xena"},
        {"51", "Yvonne"}, {"52", "Zane"}, {"53", "Anna"}, {"54", "Bobby"}, {"55", "Clara"},
        {"56", "Derek"}, {"57", "Elena"}, {"58", "Felix"}, {"59", "Gina"}, {"60", "Hank"},
        {"61", "Iris"}, {"62", "Jake"}, {"63", "Kara"}, {"64", "Leo"}, {"65", "Mona"},
        {"66", "Nate"}, {"67", "Opal"}, {"68", "Paul"}, {"69", "Queen"}, {"70", "Ross"},
        {"71", "Sarah"}, {"72", "Tom"}, {"73", "Ursula"}, {"74", "Vince"}, {"75", "Willa"},
        {"76", "Xander"}, {"77", "Yolanda"}, {"78", "Zelda"}, {"79", "Aaron"}, {"80", "Bella"},
        {"81", "Chris"}, {"82", "Diana"}, {"83", "Ethan"}, {"84", "Faye"}, {"85", "Gwen"},
        {"86", "Hugo"}, {"87", "Irene"}, {"88", "James"}, {"89", "Kyle"}, {"90", "Luna"},
        {"91", "Mason"}, {"92", "Nora"}, {"93", "Owen"}, {"94", "Pam"}, {"95", "Quinn"},
        {"96", "Ryan"}, {"97", "Sofia"}, {"98", "Troy"}, {"99", "Una"}, {"100", "Violet"}
    };

    for (String[] user : users) {
        try {
            int id = Integer.parseInt(user[0]);
            network.addUser(id, user[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error con el ID del usuario: " + user[0]);
        }
    }
    
    System.out.println("100 usuarios de ejemplo cargados (modo respaldo).");
}

    // Métodos para operaciones de la red social (sin cambios)
    private static void viewFriends() {
        System.out.print("Ingrese el ID del usuario: ");
        int userId = scanner.nextInt();
        scanner.nextLine();

        User user = network.getUser(userId);
        if (user == null) {
            System.out.println("Usuario no encontrado.");
            return;
        }

        System.out.println("\nAmigos de " + user.getName() + " (ID: " + user.getId() + "):");
        if (user.getFriends().isEmpty()) {
            System.out.println("No tiene amigos aún.");
        } else {
            for (int friendId : user.getFriends()) {
                User friend = network.getUser(friendId);
                System.out.println("- " + friend.getName() + " (ID: " + friend.getId() + ")");
            }
        }
    }

    private static void addFriendship() {
        System.out.print("Ingrese el ID del primer usuario: ");
        int userId1 = scanner.nextInt();
        System.out.print("Ingrese el ID del segundo usuario: ");
        int userId2 = scanner.nextInt();
        scanner.nextLine();

        User user1 = network.getUser(userId1);
        User user2 = network.getUser(userId2);

        if (user1 == null || user2 == null) {
            System.out.println("Error: Uno o ambos IDs no existen.");
            return;
        }

        if (network.addFriendship(userId1, userId2)) {
            System.out.println("Amistad agregada: " + user1.getName() + " ↔ " + user2.getName());
        } else {
            System.out.println("No se pudo agregar la amistad. ¿Ya son amigos?");
        }
    }

    private static void removeFriendship() {
        System.out.print("Ingrese el ID del primer usuario: ");
        int userId1 = scanner.nextInt();
        System.out.print("Ingrese el ID del segundo usuario: ");
        int userId2 = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea

        if (network.removeFriendship(userId1, userId2)) {
            System.out.println("Amistad eliminada con éxito entre " + 
                             network.getUser(userId1).getName() + " y " + 
                             network.getUser(userId2).getName());
        } else {
            System.out.println("No se pudo eliminar la amistad. Verifique que:");
            System.out.println("- Ambos IDs existan");
            System.out.println("- Sean amigos actualmente");
        }
    }

    private static void removeUser() {
        System.out.print("Ingrese el ID del usuario a eliminar: ");
        int userId = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea

        User user = network.getUser(userId);
        if (user == null) {
            System.out.println("Usuario no encontrado.");
            return;
        }

        if (network.removeUser(userId)) {
            System.out.println("Usuario " + user.getName() + " (ID: " + userId + ") eliminado con éxito.");
        } else {
            System.out.println("No se pudo eliminar el usuario.");
        }
    }

    private static void visualizeNetwork() {
        System.out.println("\n--- Visualización de la Red ---");
        System.out.println("Total de usuarios: " + network.getAllUserIds().size());
        System.out.println("Relaciones de amistad:");

        for (int userId : network.getAllUserIds()) {
            User user = network.getUser(userId);
            System.out.print(user.getName() + " (ID: " + user.getId() + ") → ");
            
            if (user.getFriends().isEmpty()) {
                System.out.println("Sin amigos");
            } else {
                user.getFriends().forEach(friendId -> 
                    System.out.print(network.getUser(friendId).getName() + " ")
                );
                System.out.println();
            }
        }
    }
}