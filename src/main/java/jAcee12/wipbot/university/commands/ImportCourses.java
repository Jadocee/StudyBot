package jAcee12.wipbot.university.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import jAcee12.wipbot.Bot;
import jAcee12.wipbot.GuildManagement;
import jAcee12.wipbot.configuration.BotCommand;
import jAcee12.wipbot.university.University;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.Permissions;
import java.time.Year;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static jAcee12.wipbot.GuildManagement.*;
import static jAcee12.wipbot.RoleManagement.hasRole;
import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;
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

    @Override
    public CommandData getCommandData() {
        return this.commandData;
    }

    @Override
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

        ThreadGroup importCourses = new ThreadGroup("Import Courses");
        Queue<Thread> threads = new LinkedList<>();

        CountDownLatch startSignal = new CountDownLatch(1);

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


                        if (event.getGuild().getRolesByName(courseCode, false).isEmpty()) {
                            System.out.println("Role doesn't exist. Creating new role.");

                            //addCourse(courseName, courseCode, null);

                            String finalCourseCode = courseCode;
                            String finalCourseName = courseName;

                            AtomicLong newRoleId = new AtomicLong();
                            AtomicLong newCatId = new AtomicLong();
                            AtomicLong newChId = new AtomicLong();



                            var createRole = event.getGuild().createRole().setName(finalCourseCode)
                                    .setMentionable(true);

                            var channels = event.getGuild().getTextChannelsByName(finalCourseCode, true);
                            var categories = event.getGuild().getCategoriesByName(finalCourseCode.substring(0, 4), true);

                            if (categories.isEmpty() && channels.isEmpty()) {

                                createRole.flatMap(role -> {
                                            newRoleId.set(role.getIdLong());
                                            return event.getGuild().createCategory(finalCourseCode.substring(0, 4));
                                        })
                                        .flatMap((category) -> {
                                            newCatId.set(category.getIdLong());
                                            return event.getGuild().createTextChannel(finalCourseCode, category);
                                        }).complete();

                            } else if (categories.isEmpty()) {

                                createRole.flatMap(role -> {
                                    newRoleId.set(role.getIdLong());
                                    return event.getGuild().createCategory(finalCourseCode.substring(0, 4));
                                }).flatMap(category -> {
                                    newCatId.set(category.getIdLong());
                                    long channel = event.getGuild().getTextChannelsByName(finalCourseCode, true)
                                            .get(0).getIdLong();
                                    newChId.set(channel);
                                    return event.getGuild().getTextChannelById(channel).getManager().setParent(category);
                                }).complete();

                                /*createRole
                                        .flatMap(role -> {
                                            this.university.addCourse(finalCourseName, finalCourseCode, role.getIdLong());
                                            return event.getGuild().createCategory(finalCourseCode.substring(0, 4));
                                        })
                                        .flatMap(category -> {
                                            this.university.getCourseByName(finalCourseCode.substring(0, 4), finalCourseName)
                                                    .setTextChannel(channels.get(0).getIdLong());
                                            return channels.get(0).getManager().setParent(category);
                                        })
                                        .queue();*/
                            } else if (channels.isEmpty()) {

                                createRole.flatMap(role -> {
                                    newRoleId.set(role.getIdLong());
                                    long category = event.getGuild().getCategoriesByName(finalCourseCode.substring(0, 4), true)
                                            .get(0).getIdLong();
                                    newCatId.set(category);
                                    return event.getGuild().createTextChannel(finalCourseCode, event.getGuild().getCategoryById(category));
                                }).complete();


                               /* createRole
                                        .flatMap(role -> {
                                            this.university.addCourse(finalCourseName, finalCourseCode, role.getIdLong());
                                            return categories.get(0).createTextChannel(finalCourseCode);
                                        })
                                        .queue(channel -> {
                                            this.university.getCourseByName(finalCourseCode.substring(0, 4), finalCourseName)
                                                    .setTextChannel(channel.getIdLong());
                                        });*/
                            }

                            this.university.addCourse(newCatId.get(), courseName, courseCode, newRoleId.get(), newChId.get());


                        } else {
                            // TODO else
                        }
                    }

                }
            }
        }
        /*while (!threads.isEmpty()) {
            Thread curr = threads.remove();
            System.out.println("\n" + curr.getName() + "\n");
            curr.start();
            if (!threads.isEmpty()) {
                try {
                    curr.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }
}
