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

import static jAcee12.wipbot.GuildManagement.*;

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

            if (findTextChannel(code, Objects.requireNonNull(event.getGuild())) == null) {
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
