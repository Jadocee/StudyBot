package jAcee12.wipbot.university;

import net.dv8tion.jda.api.entities.Role;

public class Degree {
    private final String name;
    private final int duration;
    private final Long roleId;

    public Degree(String name, int duration, Long roleId) {
        this.duration = duration;
        this.name = name;
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public Long getRole() {
        return roleId;
    }

    public boolean equals(Degree other) {
        return this.getName().equals(other.getName());
    }
}
