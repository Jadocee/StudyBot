package jAcee12.wipbot.configuration;

import com.jagrosh.jdautilities.command.SlashCommand;
import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.List;


public abstract class BotCommand{

    public abstract CommandData getCommandData();

    public abstract void run(SlashCommandEvent slashCommandEvent);
}
