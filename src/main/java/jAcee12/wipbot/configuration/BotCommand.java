package jAcee12.wipbot.configuration;

import com.jagrosh.jdautilities.command.SlashCommand;
import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.List;

public interface BotCommand{

    public CommandData getCommandData();

    public void run(SlashCommandEvent slashCommandEvent);
}
