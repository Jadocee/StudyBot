package jAcee12.wipbot.university.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import jAcee12.wipbot.Bot;
import jAcee12.wipbot.configuration.BotCommand;
import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Year;
import java.util.Objects;

import static jAcee12.wipbot.Bot.hasRole;
import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class ImportCourses extends BotCommand {
    private final CommandData commandData;
    private final University university;


    public ImportCourses() {
        this.university = null;
        this.commandData =  new CommandData("import", "Import data")
                .addOptions(
                        new OptionData(STRING, "url", "Absolute URL to the program handbook to import", true),
                        new OptionData(INTEGER, "semester", "Import courses that are available during the specified semester.", false)
                                .addChoices(
                                        new Command.Choice("Semester 1", 1),
                                        new Command.Choice("Semester 2", 2)
                                ),
                        new OptionData(STRING, "year", "Import courses that are available during the specified year.", false)
                                .addChoices(
                                        new Command.Choice(Year.now().toString(), Year.now().toString()),
                                        new Command.Choice(Year.now().plusYears(1).toString(), Year.now().plusYears(1).toString())
                                )
                );
    }

    public ImportCourses(University university) {
        this.university = university;
        this.commandData =  new CommandData("import", "Import data")
                .addOptions(
                        new OptionData(STRING, "url", "Absolute URL to the program handbook to import", true),
                        new OptionData(INTEGER, "semester", "Import courses that are available during the specified semester.", false)
                                .addChoices(
                                        new Command.Choice("Semester 1", 1),
                                        new Command.Choice("Semester 2", 2)
                                ),
                        new OptionData(STRING, "year", "Import courses that are available during the specified year.", false)
                                .addChoices(
                                        new Command.Choice(Year.now().toString(), Year.now().toString()),
                                        new Command.Choice(Year.now().plusYears(1).toString(), Year.now().plusYears(1).toString())
                                )
                );
    }

    public CommandData getCommandData() {
        return this.commandData;
    }

    public void run(SlashCommandEvent event) {
        var url = Objects.requireNonNull(event.getOption("url")).getAsString();
        String sem;
        String year;
        if (event.getOption("semester") != null) {
            sem = event.getOption("semester").getAsString();
        } else {
            sem = "Semester 1"; //TODO current sem
        }

        if (event.getOption("year") != null) {
            year = event.getOption("year").getAsString();
        } else {
            year = Year.now().toString();
        }
        try {
            this.importCourses(event, url, sem, year);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importCourses(SlashCommandEvent event, String url, String sem, String year) throws IOException {
        String courseName, courseCode;

        Document doc = Jsoup.connect(url)
                .userAgent("Chrome")
                .get();

        // Get core courses
        Elements coreCourses = doc.select("div#section-core-courses-required tr:has(td.course-code)");

        for (Element el : coreCourses) {

            courseCode = el.select("td.course-code").text();
            courseName = el.select("td.title").text();

            System.out.println(courseCode);
            System.out.println(courseName);

            for (Element ul : el.select("td.availability").select("ul")) {
                for (Element li : ul.select("li.location_callaghan")) {
                    System.out.println("loop");
                    if (li.text().contains("2022") && li.text().contains("Semester 1")) {
                        System.out.println("o");


                        if (hasRole(Objects.requireNonNull(event.getGuild()), courseCode)) {
                            System.out.println("Role doesn't exist. Creating new role.");

                            //addCourse(courseName, courseCode, null);

                            final String finalCourseCode = courseCode;
                            final String finalCourseName = courseName;

                            new Thread(() -> {
                                Objects.requireNonNull(event.getGuild()).createRole()
                                        .setName(finalCourseCode)
                                        .setMentionable(true)
                                        .queue(role -> {
                                            this.university.addCourse(finalCourseName, finalCourseCode, role.getIdLong());  // Save course with role


                                        });
                            }).start();

                        } else {
                            for (Role role : event.getGuild().getRoles()) {
                                if (role.getName().equals(courseCode)) {
                                    //this.university.im(event, courseName, courseCode, role);
                                }
                                // TODO else
                            }
                        }
                    }

                }
            }
        }
    }
}
