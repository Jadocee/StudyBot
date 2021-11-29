package jAcee12.wipbot;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.EventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import java.awt.*;
import java.util.HashMap;

public class Moderation extends ListenerAdapter {

    private final HashMap<Long, LinkedList<String>> permissions = new HashMap<>();

    public void newPerms(Long roleId) throws Exception {
        if (this.permissions.containsKey(roleId)) {
            throw new Exception("Permissions for this role ID already exist.");
        } else {
            this.permissions.put(roleId, new LinkedList<String>());
        }
    }

    public void newPerms(Queue<Long> queue) throws Exception {
        if (queue.isEmpty()) {
            throw new Exception("Queue is empty.");
        }

        queue.forEach(role -> {
            if (!this.permissions.containsKey(role)) {
                this.permissions.put(role, new LinkedList<String>());
            }
        });
    }

}
