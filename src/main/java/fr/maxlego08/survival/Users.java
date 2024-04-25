package fr.maxlego08.survival;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class Users {

    private final Map<UUID, User> users = new HashMap<>();

    public Map<UUID, User> getUsers() {
        return users;
    }

    public User computeIfAbsent(UUID uniqueId, Function<UUID, User> function) {
        return users.computeIfAbsent(uniqueId, function);
    }

    public int getTotal(UUID uniqueId) {
        return this.users.getOrDefault(uniqueId, new User()).getMaterials().size();
    }
}
