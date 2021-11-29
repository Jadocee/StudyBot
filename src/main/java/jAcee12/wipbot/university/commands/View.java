package jAcee12.wipbot.university.commands;

import jAcee12.wipbot.configuration.BotCommand;
import jAcee12.wipbot.university.Course;
import jAcee12.wipbot.university.Degree;
import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.Objects;

public class View implements BotCommand {
    private final University university;
    private final CommandData commandData;


    public View(University university) {
        this.university = university;
        this.commandData = new CommandData("view", "view")
                .addSubcommands(
                        new SubcommandData("courses", "View available courses"),
                        new SubcommandData("degrees", "View available degrees")
                );
    }

    @Override
    public CommandData getCommandData() {
        return this.commandData;
    }

    @Override
    public void run(SlashCommandEvent slashCommandEvent) {
        switch (Objects.requireNonNull(slashCommandEvent.getSubcommandName())) {
            case "courses" -> {
                viewCourses(slashCommandEvent);
            }
            case "degrees" -> {
                viewDegrees(slashCommandEvent);
            }
        }
    }

    private void viewCourses(SlashCommandEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Available Courses", null);
        eb.setColor(Color.BLUE);
        eb.setDescription("There are " + this.university.numOfCourses() + " courses available for you to join.");
        eb.addBlankField(false);
        for (String k : this.university.getKeys()) {
            for (Course item : this.university.getCourseCategory(k)) {
                eb.addField(item.getName(), event.getGuild().getRoleById(item.getRole()).getAsMention(), false);
                eb.addBlankField(false);
            }
        }
        eb.setFooter("You can join one or more of these courses by using the slash command: " +
                "/join course");
        event.replyEmbeds(eb.build())
                .setEphemeral(true)
                .queue();
    }

    private void viewDegrees(SlashCommandEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Available Courses", null);
        eb.setColor(Color.green);
        eb.setDescription("There are " + this.university.numOfDegrees() + " degrees available to pick from.");
        eb.addBlankField(false);
        for (Degree degree : this.university.getDegrees()) {
            eb.addField(degree.getName(), event.getGuild().getRoleById(degree.getRole()).getAsMention(), false);
            eb.addBlankField(false);
        }
        eb.setFooter("You can join one of these degrees by using the slash command: " +
                "/join degree");
        event.replyEmbeds(eb.build())
                .setEphemeral(true)
                .queue();
    }
}
