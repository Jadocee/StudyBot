package jAcee12.wipbot;

import jAcee12.wipbot.configuration.BotCommand;
import jAcee12.wipbot.university.University;
import jAcee12.wipbot.university.commands.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

public class SlashCommandHandler extends ListenerAdapter {
    private final HashMap<String, BotCommand> commandDataList;
    private final HashMap<String, ArrayList<CommandPrivilege>> commandPrivileges;


    public SlashCommandHandler() {
        this.commandPrivileges = new HashMap<>();
        this.commandDataList = new HashMap<>();
    }

    public SlashCommandHandler(University university) {
        this.commandPrivileges = new HashMap<>();
        this.commandDataList = new HashMap<>();
        this.initCommands(university);

    }

    public void initCommands(University university) {
        this.commandDataList.put("import", new ImportCourses(university));
        this.commandDataList.put("join", new JoinCourseDegree(university));
        this.commandDataList.put("add", new Add(university));
        this.commandDataList.put("remove", new Remove(university));
        this.commandDataList.put("view", new View(university));

    }


    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.isFromGuild()) {
            new Thread(() -> {
                this.commandDataList.get(event.getName()).run(event);
            }).start();
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("Registering slash commands...");
        if (event.getJDA().getGuilds().size() > 0) {
            event.getJDA().getGuilds().forEach(guild -> {
                new Thread(() -> {
                    guild.updateCommands().queue(v -> {
                        Queue<CommandData> commandQueue = new LinkedList<>();
                        for (String key : this.commandDataList.keySet()) {
                            commandQueue.add(this.commandDataList.get(key).getCommandData());

                        }

                        guild.updateCommands().addCommands(commandQueue).queue(commands -> {
                            System.out.println(
                                    "\nRegistered in " + guild.getName() + " @ " +
                                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                                                    .format(commands.get(0).getTimeCreated())
                            );
                        });
                    });
                }).start();
            });
        }
    }
}
