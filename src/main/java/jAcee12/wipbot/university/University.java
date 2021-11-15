package jAcee12.wipbot.university;

import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class University {
    private String name;
    private String acronym;
    private Role role;
    private final ArrayList<Course> courses = new ArrayList<Course>();
    private final HashMap<Degree, ArrayList<Course>> degrees = new HashMap<Degree, ArrayList<Course>>();
    //private HashMap<Semester, LinkedList<Course>> courses;

    public University(String name, String acronym, Role role) {
        this.name = name;
        this.acronym = acronym;
        this.role = role;
        //this.degrees = new HashMap<>();
    }

    public University(String name, String acronym) {
        this.name = name;
        this.acronym = acronym;
        this.role = null;
        //this.degrees = new HashMap<>();
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return this.role;
    }

    public void addDegree(String name, int duration, Role role) {
        Degree new_degree = new Degree(name, duration, role);

    }

    public String getName() {
        return this.name;
    }

    public String getAcronym() {
        return this.acronym;
    }

    public boolean equals(University other) {
        if (other == null) {
            return false;
        } else {
            return this.name.equals(other.name) && this.acronym.equals(other.acronym);
        }
    }


}
