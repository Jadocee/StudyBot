package jAcee12.wipbot.university;

import jAcee12.wipbot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import okhttp3.EventListener;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.management.relation.RoleList;
import java.awt.*;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class University extends ListenerAdapter {

    private final String name;
    private final String acronym;
    private final HashMap<String, Vector<Course>> courses = new HashMap<String, Vector<Course>>();
    private final HashMap<Year, Vector<Semester>> semesters = new HashMap<Year, Vector<Semester>>();
    private final Vector<Degree> degrees = new Vector<Degree>();
    private final Vector<Course> newCourses = new Vector<>();


    public University(String name, String acronym) {
        this.name = name;
        this.acronym = acronym;
    }

    public void removeCourse(Role toDel) {
        String k = toDel.getName().substring(0, toDel.getName().length() / 2);
        this.courses.get(k).removeIf(course -> course.getRole().equals(toDel.getIdLong()));
    }

    public boolean inDegree(@NotNull SlashCommandEvent event) {
        Member sender = event.getMember();
        for (Role role : sender.getRoles()) {
            for (Degree degree : this.degrees) {
                if (degree.getRole().equals(role.getIdLong())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean joinedDegree(Member member, Role role) {
        for (Role memberRole : member.getRoles()) {
            if (memberRole.getId().equals(role.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isCourse(Role role) {
        String k = role.getName().substring(0, role.getName().length() / 2);
        if (this.courses.containsKey(k)) {
            for (Course course : this.courses.get(k)) {
                if (course.getRole().equals(role.getIdLong())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDegree(Role role) {
        for (Degree degree : this.degrees) {
            if (degree.getRole().equals(role.getIdLong())) {
                return true;
            }
        }
        return false;
    }

    public int numOfCourses() {
        if (this.courses.isEmpty()) {
            return 0;
        } else {
            AtomicInteger count = new AtomicInteger(0);
            courses.forEach((key, list) -> {
                count.set(count.addAndGet(list.size()));
            });
            return count.intValue();
        }
    }

    public Set<String> getKeys() {
        return this.courses.keySet();
    }

    public int numOfDegrees() {
        return degrees.size();
    }

    public Vector<Degree> getDegrees() {
        return this.degrees;
    }

    public void addCourse(@NotNull Course course) {
        String type = course.getCode().substring(course.getCode().length() / 2);
        for (String k : this.courses.keySet()) {
            if (k.equals(type)) {
                this.courses.get(k).add(course);
                return;
            }
        }
        this.courses.put(type, new Vector<Course>());
        this.courses.get(type).add(course);
    }

    public Vector<Course> getCourseCategory(String key) {
        return this.courses.get(key);
    }

    public void addCourse(String name, @NotNull String code, Long roleId) {
        System.out.println(1);
        String type = code.substring(0, code.length() / 2);

        if (this.courses.containsKey(type)) {
            if (!this.courses.get(type).isEmpty()) {
                for (Course course : this.courses.get(type)) {
                    if (course.getName().equals(name) && course.getCode().equals(code)) {
                        try {
                            throw new Exception("The course \"" + course.getName() + "\" already exists.");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            }
        } else {
            this.courses.put(type, new Vector<Course>());
        }

        Course newCourse = new Course(name, code, roleId);
        this.courses.get(type).add(newCourse);

        if (newCourse.getRole() == null) {
            this.newCourses.addElement(newCourse);
        }
    }

    public void addCourses(SlashCommandEvent event, Vector<ArrayList<Object>> newRoles) throws Exception {
        System.out.println(newRoles.size());
        if (newRoles.isEmpty()) {
            throw new Exception("List is empty");
        }

        System.out.println("Creating courses from imported roles.\n");

        newRoles.forEach(
                newRole -> {
                    String cName = newRole.get(0).toString();
                    String cCode = newRole.get(1).toString();
                    Long roleId = Long.getLong(newRole.get(2).toString());
                    String key = cCode.substring(0, cCode.length() / 2);

                    /*String roleName2 = event.getGuild().getRoleById(roleId).getName();
                    String key2 = roleName.substring(0, roleName.length()/2);*/

                    System.out.println("Looking for key \"" + key + "\"");

                    if (!this.courses.containsKey(key)) {
                        System.out.println("Not found. Creating new key.");
                        this.courses.put(key, new Vector<Course>());
                    } else {
                        System.out.println("Found.");
                    }

                    System.out.println("Looking for course that matches role \"" + cName + "\"");
                    if (this.courses.get(key).stream().anyMatch(course -> course.getRole().equals(roleId))) {
                        System.out.println("Found.");
                        System.out.println("The course already exists.");
                    } else {
                        this.courses.get(key).addElement(
                                new Course(cName, cCode, roleId)
                        );

                        /*Role role = event.getGuild().getRoleById(roleId);
                        this.courses.get(key).add(new Course(role.getName(),));*/
                    }

                    /*this.courses.get(key).forEach(course -> {
                        if (course.getRole().equals(roleId)) {

                        }
                    });*/
                }
        );
    }

    public void addDegree(@NotNull Degree degree) {

    }

    public void addDegree(String name, int duration, Long roleId) throws Exception {
        for (Degree degree : this.degrees) {
            if (degree.getName().equals(name)) {
                throw new Exception("Degree already exists");
            }
        }
        this.degrees.add(new Degree(name, duration, roleId));
    }

    public String getName() {
        return this.name;
    }

    public String getAcronym() {
        return this.acronym;
    }

    public boolean equals(University other) {
        if (other == null) {
            return false;
        } else {
            return this.name.equals(other.name) && this.acronym.equals(other.acronym);
        }
    }

/*    @Override
    public void onRoleCreate(@NotNull RoleCreateEvent event) {
        if (this.newCourses.isEmpty()) {
            return;
        }

        this.newCourses.removeIf(course -> {
            if (course.getCode().equals(event.getRole().getIdLong())) {
                String k = course.getCode().substring(0, course.getCode().length() / 2);
                int index = this.courses.get(k).indexOf(course);
                this.courses.get(k).get(index).setRoleId(event.getRole().getIdLong());

                TextChannel newTextChannel = Bot.findTextChannel(event.getGuild(), course.getCode());
                if (newTextChannel == null) {
                    newTextChannel = Bot.createTextChannel(event.getGuild(), course.getCode());
                }

                Category parent = Bot.findCategory(event.getGuild(), course.getCode().substring(0, course.getCode().length()/2));
                if (parent == null) {
                    parent = Bot.createCategory(event.getGuild(), course.getCode().substring(0, course.getCode().length()/2));
                }

                newTextChannel.getManager().setParent(parent).queue();

                if (Bot.containsCategory(event.getGuild(), course.getCode().substring(0, course.getCode().length()/2))) {

                }

                return true;
            } else {
                return false;
            }
        });
    }*/

    private void createCourseChannel(Guild guild, String chName) {
        var a = guild.getCategories().stream().filter(category -> {
            return category.getName().equals(chName);
        }).collect(Collectors.toList());

        if (!a.isEmpty()) {

        } else {

        }
    }


/*    public static Queue<CommandData> getCommands() {
        Queue<CommandData> commands = new LinkedList<>();
        commands.add(
                new CommandData("add", "University related commands")
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
                        )
        );

        commands.add(
                new CommandData("import", "Import data")
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
                        )
        );
        commands.add(
                new CommandData("join", "Join something idk")
                        .addSubcommands(
                                new SubcommandData("course", "Join a course")
                                        .addOptions(
                                                new OptionData(ROLE, "mention", "Mention the role of the course", true)
                                        ),
                                new SubcommandData("degree", "Join a degree")
                                        .addOptions(
                                                new OptionData(ROLE, "mention", "Mention the role of the degree", true)
                                        )
                        )
        );
        commands.add(
                new CommandData("view", "view")
                        .addSubcommands(
                                new SubcommandData("courses", "View available courses"),
                                new SubcommandData("degrees", "View available degrees")
                        )
        );
        commands.add(
                new CommandData("remove", "remove")
                        .addSubcommands(
                                new SubcommandData("course", "Remove a course")
                                        .addOptions(
                                                new OptionData(ROLE, "course", "Mention the role of the course", true)
                                        ),
                                new SubcommandData("degree", "Remove a degree")
                                        .addOptions(
                                                new OptionData(ROLE, "degree", "Mention the role of the degree", true)
                                        )
                        )
        );
        return commands;
    }*/

    /*@Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            // Only accept commands from guilds
            if (event.isFromGuild()) {

                switch (event.getName()) {
                    case "remove" -> {
                        switch (Objects.requireNonNull(event.getSubcommandName())) {
                            case "course" -> {
                                Role toDel = Objects.requireNonNull(event.getOption("course")).getAsRole();

                                if (!isCourse(toDel)) {
                                    throw new Exception("The role mentioned is not a course.");
                                }

                                removeCourse(toDel);
                                Objects.requireNonNull(Objects.requireNonNull(event.getGuild())
                                                .getRoleById(toDel.getId()))
                                        .delete()
                                        .queue(v -> {
                                            event.reply("The course **" + toDel.getName() + "** has been removed.")
                                                    .setEphemeral(true)
                                                    .queue();
                                        });

                            }
                            case "degree" -> {

                            }
                        }
                    }
                    case "add" -> {
                        //TODO delete
                        // TODO already exists
                        switch (Objects.requireNonNull(event.getSubcommandName())) {
                            case "course" -> {
//                                String name = Objects.requireNonNull(event.getOption("name")).getAsString();
                                //StringBuilder name = new StringBuilder();
                                //String[] name = Objects.requireNonNull(event.getOption("name")).getAsString().split(" ");
                                String code = Objects.requireNonNull(event.getOption("code")).getAsString().toUpperCase();
                                String name = Bot.capitalise(Objects.requireNonNull(event.getOption("name")).getAsString().split(" "));

                                if (!Bot.hasRole(Objects.requireNonNull(event.getGuild()), code)) {
                                    Objects.requireNonNull(event.getGuild()).createRole()
                                            .setName(code)
                                            .setMentionable(true)
                                            .queue(role -> {
                                                System.out.println(
                                                        "New role \"" + role.getName() + "\" created @ " +
                                                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(role.getTimeCreated())
                                                );

                                                try {
                                                    this.addCourse(name, code, role.getIdLong());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                } else {
                                    for (Role role : event.getGuild().getRoles()) {
                                        if (role.getName().equals(code)) {
                                            this.addCourse(name, code, role.getIdLong());
                                        }
                                        // TODO else
                                    }
                                }
                            }
                            case "degree" -> {
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
                                                this.addDegree(name, duration, role.getIdLong());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            event.reply("Ong we just added **" + name + "** 🥶🧊" +
                                                            "\nYou can join this degree by using \"/join degree " +
                                                            role.getAsMention() + "\"")
                                                    .setEphemeral(true)
                                                    .queue();
                                        });
                            }
                        }
                    }
                    case "view" -> {
                        switch (Objects.requireNonNull(event.getSubcommandName())) {
                            case "courses" -> {
                                EmbedBuilder eb = new EmbedBuilder();
                                eb.setTitle("Available Courses", null);
                                eb.setColor(Color.BLUE);
                                eb.setDescription("List of all the available courses to join.");
                                eb.addBlankField(false);
                                for (String k : this.getKeys()) {
                                    for (Course item : this.getCourseCategory(k)) {
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
                            case "degrees" -> {
                                EmbedBuilder eb = new EmbedBuilder();
                                eb.setTitle("Available Courses", null);
                                eb.setColor(Color.green);
                                eb.setDescription("List of all the available degrees to join.");
                                eb.addBlankField(false);
                                for (Degree degree : this.getDegrees()) {
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
                    }
                    case "join" -> {
                        switch (Objects.requireNonNull(event.getSubcommandName())) {
                            case "course" -> {
                                Role toJoin = Objects.requireNonNull(event.getOption("mention")).getAsRole();

                                if (!isCourse(toJoin)) {
                                    throw new Exception("The role mentioned is not a course.");
                                }

                                Objects.requireNonNull(event.getGuild()).addRoleToMember(
                                        Objects.requireNonNull(event.getMember()),
                                        toJoin
                                ).queue(v -> {
                                    event.reply("You are now part of **" + toJoin.getName() + "**! 🤗")
                                            .setEphemeral(true)
                                            .queue();
                                });
                                // TODO verify role
                            }

                            case "degree" -> {
                                // TODO check if already in degree
                                if (this.inDegree(event)) {
                                    throw new Exception("You are already in a degree." +
                                            "\nTo change your degree: use `/change degree`");
                                } else {
                                    Role role = Objects.requireNonNull(event.getOption("mention")).getAsRole();

                                    // Check if mention is a degree
                                    if (!isDegree(role)) {
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
                    }
                    case "import" -> {
                        *//*String url = Objects.requireNonNull(event.getOption("url")).getAsString();
                        String sem, year;
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
                        this.importCourses(event, url, sem, year);*//*


                    }
                    default -> {
                        event.reply("There was a problem handling your request.").setEphemeral(true).queue();
                    }
                }
            }
        } catch (Exception e) {
            // TODO discord embed
            *//*EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.RED);
            eb.setTitle("Oops...\t Something went wrong 😦");
            eb.setDescription("Reason:\t`" + e.getMessage() + "`");
            event.replyEmbeds(eb.build())
                    .setEphemeral(true)
                    .queue();*//*
            e.printStackTrace();
        }
    }*/

    /*public void importCourses(SlashCommandEvent event, String url, String sem, String year) throws Exception {
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

                            addCourse(courseName, courseCode, null);

                            Objects.requireNonNull(event.getGuild()).createRole()
                                    .setName(courseCode)
                                    .setMentionable(true)
                                    .queue();
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
    }*/}

