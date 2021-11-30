package jAcee12.wipbot.university.commands;

import jAcee12.wipbot.Bot;
import jAcee12.wipbot.configuration.BotCommand;
import jAcee12.wipbot.university.Course;
import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static jAcee12.wipbot.RoleManagement.hasRole;
import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Add extends BotCommand {
    private final University university;
    private final CommandData commandData;


    public Add(University university) {
        this.university = university;
        this.commandData = new CommandData("add", "University related commands")
                .addSubcommands(
                        new SubcommandData("course", "Add a course")
                                .addOptions(
                                        new OptionData(STRING, "name", "Name of the University (e.g. 'University of Discord'", true),
                                        new OptionData(STRING, "code", "Acronym of the University name (e.g. UoD)", true)
                                ),
                        new SubcommandData("degree", "Add a degree")
                                .addOptions(
                                        new OptionData(STRING, "name", "Name of the degree (e.g. \"Software Engineering\")", true),
                                        new OptionData(INTEGER, "duration", "Expected number of years to complete", true)
                                )
                );
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }

    @Override
    public void run(SlashCommandEvent slashCommandEvent) {
        switch (Objects.requireNonNull(slashCommandEvent.getSubcommandName())) {
            case "course" -> {
                addCourse(slashCommandEvent);
            }
            case "degree" -> {
                addDegree(slashCommandEvent);
            }
        }
    }

    private void addCourse(SlashCommandEvent event) {
        String code = Objects.requireNonNull(event.getOption("code")).getAsString().toUpperCase();
        String name = Bot.capitalise(Objects.requireNonNull(event.getOption("name")).getAsString().split(" "));

        if (!hasRole(Objects.requireNonNull(event.getGuild()), code)) {
            Objects.requireNonNull(event.getGuild()).createRole()
                    .setName(code)
                    .setMentionable(true)
                    .flatMap(role -> {
                        return event.getGuild().createCategory(code.substring(0, 4));
                    })
                    .flatMap(category -> {
                        return event.getGuild().createTextChannel(code);
                    })
                    .queue(role -> {

                        try {
                            this.university.addAndGetCourse(name, code, role.getIdLong());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } else {
            for (Role role : event.getGuild().getRoles()) {
                if (role.getName().equals(code)) {
                    this.university.addCourse(name, code, role.getIdLong());
                }
                // TODO else
            }
        }
    }

    private void addDegree(SlashCommandEvent event) {
        String name = Objects.requireNonNull(event.getOption("name")).getAsString();
        int duration = (int) Objects.requireNonNull(event.getOption("duration")).getAsLong();

        Objects.requireNonNull(event.getGuild()).createRole()
                .setName(name)
                .setMentionable(true)
                .queue(role -> {
                    System.out.println(
                            "New role \"" + role.getName() + "\" created @ " +
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH::mm::ss").format(role.getTimeCreated())
                    );
                    try {
                        this.university.addDegree(name, duration, role.getIdLong());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    event.reply("Ong we just added **" + name + "** 🥶🧊" +
                                    "\nYou can join this degree by using \"/join degree " +
                                    role.getAsMention() + "\"")
                            .setEphemeral(true)
                            .queue();
                });
    }
}
