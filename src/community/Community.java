package community;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una comunidad dentro de la red social.
 */
public class Community {
    private final String name;
    private final String color;
    private final List<Integer> memberIds;

    public Community(String name, String color) {
        this.name = name;
        this.color = color;
        this.memberIds = new ArrayList<>();
    }

    /**
     * Agrega un usuario a la comunidad.
     */
    public void addMember(int userId) {
        memberIds.add(userId);
    }

    /**
     * Devuelve la lista de IDs de los miembros de la comunidad.
     */
    public List<Integer> getMembers() {
        return memberIds;
    }

    /**
     * Devuelve el nombre de la comunidad.
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve el color asociado a la comunidad.
     */
    public String getColor() {
        return color;
    }
}
