package jAcee12.wipbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Objects;

public class RoleManagement {

    public static boolean hasRole(Guild guild, String rName) {
        for (Role role : guild.getRoles()) {
            if (role.getName().equalsIgnoreCase(rName)) {
                return true;
            }
        }
        return false;
    }

    public static void giveRole(SlashCommandEvent event, Role role) {
        Objects.requireNonNull(event.getGuild()).addRoleToMember(Objects.requireNonNull(event.getMember()), role)
                .queue(v -> {
                    event.reply("done")
                            .setEphemeral(true)
                            .queue();
                });
    }

}
