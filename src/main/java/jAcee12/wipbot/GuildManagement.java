package jAcee12.wipbot;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GuildManagement {
    public static TextChannel findTextChannel(String channelName, Guild guild) {
        var channels = guild.getTextChannelsByName(channelName, true);
        if (!channels.isEmpty()) {
            return channels.get(0);
        }
        return null;
    }

    public static TextChannel createTextChannel(Guild guild, String channelName) {
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicReference<TextChannel> newChannel = new AtomicReference<>();
        guild.createTextChannel(channelName).queue(channel -> {
            done.set(true);
            newChannel.set(channel);
        });
        while (!done.get()) {
            continue;
        }
        return newChannel.get();
    }



    public static Category createCategory(Guild guild, String catName) {
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicReference<Category> newCategory = new AtomicReference<>();
        guild.createCategory(catName).queue(category -> {
            done.set(true);
            newCategory.set(category);
        });
        while (!done.get()) {
            continue;
        }
        return newCategory.get();
    }

    public static boolean containsCategory(Guild guild, String catName) {
        for (Category category : guild.getCategories()) {
            if (category.getName().equals(catName)) {
                return true;
            }
        }
        return false;
    }

    public static Category findCategory(Guild guild, String catName) {
        for (Category category : guild.getCategories()) {
            if (category.getName().equals(catName)) {
                return category;
            }
        }
        return null;
    }


}
