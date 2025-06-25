package user;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un usuario en la red social.
 */
public class User {
    private final int id;
    private String name;
    private final List<Integer> friends;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
        this.friends = new ArrayList<>();
    }

    /**
     * Devuelve el ID único del usuario.
     */
    public int getId() {
        return id;
    }

    /**
     * Devuelve el nombre del usuario.
     */
    public String getName() {
        return name;
    }

    /**
     * Cambia el nombre del usuario.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Devuelve la lista de IDs de amigos del usuario.
     */
    public List<Integer> getFriends() {
        return friends;
    }

    /**
     * Agrega un amigo a la lista si no existe.
     */
    public void addFriend(int friendId) {
        if (!friends.contains(friendId)) {
            friends.add(friendId);
        }
    }

    /**
     * Elimina un amigo de la lista.
     */
    public void removeFriend(int friendId) {
        friends.remove(Integer.valueOf(friendId));
    }
}
