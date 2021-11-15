package jAcee12.wipbot;

import com.jagrosh.jdautilities.commons.waiter.*;
import jAcee12.wipbot.university.Degree;
import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.awt.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class Bot extends ListenerAdapter{
    private final ArrayList<University> universities = new ArrayList<University>();
    private final EventWaiter eventWaiter = new EventWaiter();
    private HashMap<Role, List> permissions;

   /* private Bot() {
        this.universities = new ArrayList<University>();
    }*/

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                throw new Exception("You have to provide a token as first argument!");
            }
            // args[0] should be the token
            // We don't need any intents for this bot. Slash commands work without any intents!
            new Bot().start(args[0]);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void start(String token) throws LoginException, InterruptedException {
        System.out.println("Building JDA...");
        JDA jda = JDABuilder.createLight(token, EnumSet.noneOf(GatewayIntent.class)) // slash commands don't need any intents
                .addEventListeners(
                        this,
                        eventWaiter
                )
                .setActivity(Activity.listening("stuff"))
                .build();
        jda.awaitReady();
        jda.updateCommands().queue(); // Clear old commands
        jda.getPresence().setActivity(Activity.watching("tv"));
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("Done!\n");
        System.out.println("Registering slash commands...");
        if (event.getJDA().getGuilds().size() > 0) {
            for (Guild guild: event.getJDA().getGuilds()) {
                this.registerCommands(guild);
            }
        }
        System.out.println("Done!\n");
        System.out.println("Bot is ready!");
    }

    private void registerCommands(@NotNull Guild guild) {
        guild.updateCommands().queue();
        guild.updateCommands()
                .addCommands(
                        new CommandData("course", "University related commands")
                                .addSubcommands(
                                        new SubcommandData("create", "Create a new University for users to join!")
                                                .addOptions(
                                                        new OptionData(STRING, "name", "Name of the University (e.g. 'University of Discord'", true),
                                                        new OptionData(STRING, "acronym", "Acronym of the University name (e.g. UoD)", true)
                                                ),
                                        new SubcommandData("join", "Join a University (i.e., get a role)")
                                                .addOptions(
                                                        new OptionData(ROLE, "name", "Mention of the role of the University to join (e.g. '@UOD'", false)
                                                ),
                                        new SubcommandData("add", "Add to a University")
                                                .addOptions(
                                                        new OptionData(STRING, "item", "Add item to the University", true)
                                                                .addChoices(
                                                                        new Command.Choice("degree", "degree"),
                                                                        new Command.Choice("course", "course")
                                                                )
                                                ),
                                        new SubcommandData("remove", "Remove a University")
                                                .addOptions(
                                                        new OptionData(MENTIONABLE, "role", "Mentioned role for the University", true)
                                                ),
                                        new SubcommandData("wipe", "Remove all Universities")
                                )
                )
                .queue();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        this.registerCommands(event.getGuild());
    }

    @Override
    public void onRoleCreate(@NotNull RoleCreateEvent event) {
        super.onRoleCreate(event);
    }



    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            // Only accept commands from guilds
            if (event.isFromGuild()) {

                switch (event.getName()) {
                    case "uni" -> {
                        switch (Objects.requireNonNull(event.getSubcommandName())) {
                            case "create" -> {
                                String uniName = Objects.requireNonNull(event.getOption("name")).getAsString();
                                String uniAcr = Objects.requireNonNull(event.getOption("acronym")).getAsString();
                                Objects.requireNonNull(event.getGuild()).createRole()
                                        .setName(uniAcr)
                                        .setMentionable(true)
                                        .queue(role -> {
                                            System.out.println(
                                                    "New role \"" + role.getName() + "\" created @ " +
                                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH::mm::ss").format(role.getTimeCreated())
                                            );
                                            this.universities.add(new University(uniName, uniAcr, role));
                                            event.reply("Yessir, we just added **" + uniName + "**! 😄"
                                                            + "\n" + "You can join this university by using \"/uni join "
                                                            + role.getAsMention() + "\"")
                                                    .setEphemeral(true).queue();
                                        });
                                event.getGuild().createCategory(uniName).queue();
                                // TODO if university exists
                            }
                            case "join" -> {
                                if (event.getOption("name") != null) {
                                    Role uni = Objects.requireNonNull(event.getOption("name")).getAsRole();
                                    System.out.println(uni.getAsMention());
                                    for (University university : this.universities) {
                                        System.out.println(university.getRole().getAsMention());
                                        if (university.getRole().getAsMention().equals(uni.getAsMention())) {
                                            Objects.requireNonNull(event.getGuild())
                                                    .addRoleToMember(Objects.requireNonNull(event.getMember()).getId(), university.getRole())
                                                    .queue(v -> {
                                                        event.reply("You're now a part of " + university.getRole().getAsMention()
                                                                        + "! 😄")
                                                                .setEphemeral(true)
                                                                .queue();
                                                    });


                                            /*if (Objects.requireNonNull(event.getMember()).getRoles().contains(university.getRole())) {
                                                event.reply("You're now in the " + university.getName() + "! 😄")
                                                        .setEphemeral(true).queue();
                                            }*/
                                            break;
                                        }
                                    }
                                } else {
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("🎓 Universities", null);
                                    eb.setColor(Color.red);
                                    eb.setDescription("List of available Universities for you to join");
                                    for (University k : this.universities) {
                                        eb.addField(k.getName(), k.getRole().getAsMention(), false);
                                    }
                                    eb.setFooter("You can join a university by using \"/uni join @role\"");
                                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                                }
                            }

                            case "wipe" -> {

                                if (Objects.requireNonNull(event.getMember()).isOwner()) {
                                    wipeUniversities(event.getGuild());
                                }
                                event.reply("Done").setEphemeral(true).queue();
                            }
                        }
                    }
                    default -> {
                        event.reply("test").setEphemeral(false).queue();
                    }
                }
            }
        } catch (Exception e) {
            // TODO discord embed
            e.printStackTrace();
        }
    }
    private void wipeUniversities(Guild guild) {
        for (University university : this.universities) {
            for (Role role : guild.getRoles()) {
                if (role.getAsMention().equals(university.getRole().getAsMention())) {
                    role.delete().queue();
                }
            }
        }
    }
}
