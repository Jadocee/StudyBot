package jAcee12.wipbot;


import jAcee12.wipbot.configuration.BotCommand;
import jAcee12.wipbot.configuration.Configuration;
import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Bot extends ListenerAdapter {
    private final University university;
    private JDA jda;

    public Bot() {
        this.university = new University("University of Newcastle", "UON");
    }


    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                throw new Exception("You have to provide a token as first argument!");
                // args[0] should be the token
                // We don't need any intents for this bot. Slash commands work without any intents!
            } else {
                new Bot().start(args[0]);

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void start(String token) throws InterruptedException, LoginException {
        System.out.println("Building JDA...");
       this.jda = JDABuilder.createLight(token, EnumSet.noneOf(GatewayIntent.class)) // slash commands don't need any intents
                .addEventListeners(
                        this,
                        new SlashCommandHandler(this.university)
                )
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.competing("in CS:GO"))
                .build();
        jda.awaitReady();
        jda.updateCommands().queue(); // Clear old commands
        jda.getPresence().setActivity(Activity.watching("tv"));


    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("Bot has finished building!\n");
    }


    @Override
    public void onRoleCreate(@NotNull RoleCreateEvent event) {
        String roleName = event.getRole().getName();

        System.out.println(
                "New role \"" + roleName + "\" created @ " +
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(event.getRole().getTimeCreated())
        );
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

    public static TextChannel findTextChannel(Guild guild, String chName) {
        for (TextChannel channel : guild.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase(chName)) {
                return channel;
            }
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

    public static boolean hasRole(Guild guild, String rName) {
        for (Role role : guild.getRoles()) {
            if (role.getName().equalsIgnoreCase(rName)) {
                return true;
            }
        }
        return false;
    }

    private void giveRole(SlashCommandEvent event, Role role) {
        Objects.requireNonNull(event.getGuild()).addRoleToMember(Objects.requireNonNull(event.getMember()), role)
                .queue(v -> {
                    event.reply("done")
                            .setEphemeral(true)
                            .queue();
                });
    }

    public static String capitalise(String[] string) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String word : string) {
            stringBuilder
                    .append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        stringBuilder.trimToSize();
        return stringBuilder.toString();
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

    private void addCourse(@NotNull SlashCommandEvent event, String name, String code, Long roleId) throws Exception {
        //this.moderation.newPerms(roleId);
        //this.university.addCourse(name, code, roleId);

        var successMsg = event.reply("Yessir, we just added **" + name + "**! 😄" + "\n" +
                        "You can join this course by using \"/join course " +
                        Objects.requireNonNull(event.getGuild()).getRoleById(roleId) + "\"")
                .setEphemeral(true);


        // Find
        if (!containsCategory(Objects.requireNonNull(event.getGuild()), code.substring(0, code.length() / 2))) {
            event.getGuild().createCategory(code.substring(0, code.length() / 2))
                    .queue(
                            category -> {
                                if (findCategory(Objects.requireNonNull(event.getGuild()), code) == null) {

                                    event.getGuild().createTextChannel(code)
                                            .setParent(category)
                                            .queue(channel -> {
                                                successMsg.queue();
                                            });
                                } else {

                                    for (TextChannel channel : event.getGuild().getTextChannels()) {

                                        if (channel.getName().equalsIgnoreCase(code)) {
                                            channel.getManager().setParent(category).queue(
                                                    v -> {
                                                        successMsg.queue();
                                                    }
                                            );
                                            break;
                                        }
                                    }
                                }
                            }
                    );
        } else {
            Category category = findCategory(event.getGuild(), code);

            if (findTextChannel(Objects.requireNonNull(event.getGuild()), code) == null) {
                event.getGuild().createTextChannel(code)
                        .setParent(category)
                        .queue(
                                channel -> {
                                    successMsg.queue();
                                }
                        );
            } else {
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    if (channel.getName().equalsIgnoreCase(code)) {
                        channel.getManager().setParent(category).queue(
                                v -> {
                                    successMsg.queue();
                                }
                        );
                        break;
                    }
                }
            }
        }
    }
}
