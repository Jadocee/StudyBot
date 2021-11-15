package jAcee12.wipbot.university;

import net.dv8tion.jda.api.entities.Role;

public class Degree {
    private final String name;
    private final int duration;
    private final Role role;

    public Degree(String name, int duration, Role role) {
        this.duration = duration;
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public Role getRole() {
        return role;
    }
}
