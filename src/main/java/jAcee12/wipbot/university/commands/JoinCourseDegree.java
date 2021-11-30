package jAcee12.wipbot.university.commands;

import jAcee12.wipbot.configuration.BotCommand;

import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Objects;

import static net.dv8tion.jda.api.interactions.commands.OptionType.ROLE;

public class JoinCourseDegree extends BotCommand {
    private final University university;
    private final CommandData commandData;

    public JoinCourseDegree() {
        this.commandData =  new CommandData("join", "Join something idk")
                .addSubcommands(
                        new SubcommandData("course", "Join a course")
                                .addOptions(
                                        new OptionData(ROLE, "mention", "Mention the role of the course", true)
                                ),
                        new SubcommandData("degree", "Join a degree")
                                .addOptions(
                                        new OptionData(ROLE, "mention", "Mention the role of the degree", true)
                                )
                );
        university = null;
    }

    public JoinCourseDegree(University university) {
        this.university = university;
        this.commandData =  new CommandData("join", "Join something idk")
                .addSubcommands(
                        new SubcommandData("course", "Join a course")
                                .addOptions(
                                        new OptionData(ROLE, "mention", "Mention the role of the course", true)
                                ),
                        new SubcommandData("degree", "Join a degree")
                                .addOptions(
                                        new OptionData(ROLE, "mention", "Mention the role of the degree", true)
                                )
                );
    }

    @Override
    public CommandData getCommandData() {
        return this.commandData;
    }

    @Override
    public void run(SlashCommandEvent slashCommandEvent) {
        try {
            if (Objects.equals(slashCommandEvent.getSubcommandName(), "course")) {
                joinCourse(slashCommandEvent);
                // TODO verify role
            } else if (Objects.equals(slashCommandEvent.getSubcommandName(), "degree")) {
                joinDegree(slashCommandEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinCourse(SlashCommandEvent slashCommandEvent) throws Exception {
        Role toJoin = Objects.requireNonNull(slashCommandEvent.getOption("mention")).getAsRole();

        if (!this.university.isCourse(toJoin)) {
            throw new Exception("The role mentioned is not a course.");

        }

        Objects.requireNonNull(slashCommandEvent.getGuild()).addRoleToMember(
                Objects.requireNonNull(slashCommandEvent.getMember()),
                toJoin
        ).queue(v -> {
            slashCommandEvent.reply("You are now part of **" + toJoin.getName() + "**! 🤗")
                    .setEphemeral(true)
                    .queue();
        });
    }

    private void joinDegree(SlashCommandEvent event) throws Exception {
        // TODO check if already in degree
        if (this.university.inDegree(event)) {
            throw new Exception("You are already in a degree." +
                    "\nTo change your degree: use `/change degree`");
        } else {
            Role role = Objects.requireNonNull(event.getOption("mention")).getAsRole();

            // Check if mention is a degree
            if (!this.university.isDegree(role)) {
                throw new Exception("The mention used in this command is not a Degree.");
            }

            Objects.requireNonNull(event.getGuild()).addRoleToMember(Objects.requireNonNull(event.getMember()), role)
                    .queue(v -> {
                        // TODO verify join
                        event.reply("Success. You just joined **" +
                                        Objects.requireNonNull(event.getOption("mention"))
                                                .getAsMentionable().getAsMention() + "**! 🤗")
                                .setEphemeral(true)
                                .queue();
                    });
            System.out.println(role);
        }
    }
}
