package jAcee12.wipbot.university.commands;

import jAcee12.wipbot.configuration.BotCommand;
import jAcee12.wipbot.university.Course;
import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Objects;

import static net.dv8tion.jda.api.interactions.commands.OptionType.ROLE;

public class Remove extends BotCommand {
    private final University university;
    private final CommandData commandData;

    public Remove(University university) {
        this.university = university;
        this.commandData = new CommandData("remove", "remove")
                .addSubcommands(
                        new SubcommandData("course", "Remove a course")
                                .addOptions(
                                        new OptionData(ROLE, "course", "Mention the role of the course", true)
                                ),
                        new SubcommandData("degree", "Remove a degree")
                                .addOptions(
                                        new OptionData(ROLE, "degree", "Mention the role of the degree", true)
                                ),
                        new SubcommandData("all", "Remove all")
                );
    }

    @Override
    public CommandData getCommandData() {
        return this.commandData;
    }

    @Override
    public void run(SlashCommandEvent slashCommandEvent) {
        switch (Objects.requireNonNull(slashCommandEvent.getSubcommandName())) {
            case "course" -> {
                try {
                    removeCourse(slashCommandEvent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case "degree" -> {
                removeDegree(slashCommandEvent);
            }
            case "all" -> {
                removeAll(slashCommandEvent);
            }
        }
    }

    private void removeAll(SlashCommandEvent event) {
        this.university.removeAllCourses(event);
    }

    private void removeCourse(SlashCommandEvent event) throws Exception {
        Role toDel = Objects.requireNonNull(event.getOption("course")).getAsRole();

        if (!this.university.isCourse(toDel)) {
            throw new Exception("The role mentioned is not a course.");
        }

        this.university.removeCourse(toDel);
        Objects.requireNonNull(Objects.requireNonNull(event.getGuild())
                        .getRoleById(toDel.getId()))
                .delete()
                .queue(v -> {
                    event.reply("The course **" + toDel.getName() + "** has been removed.")
                            .setEphemeral(true)
                            .queue();
                });
    }

    private void removeDegree(SlashCommandEvent event) {}
}
