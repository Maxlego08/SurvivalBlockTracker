package fr.maxlego08.survival;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class User {

    private final Set<Material> materials = new HashSet<>();

    public User() {
    }

    public Set<Material> getMaterials() {
        return materials;
    }
}
