package jAcee12.wipbot.university;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Course {
    private String name;
    private String code;
    private Long roleId;
    private Long textChannelId;

    Course(String name, String code, Long roleId) {
        this.code = code;
        this.name = name;
        this.roleId = roleId;
    }

    Course(String name, String code, Long roleId, Long textChannelId) {
        this.code = code;
        this.name = name;
        this.roleId = roleId;
        this.textChannelId = textChannelId;
    }

    Course (String name, String code) {
        this.code = code;
        this.name = name;
    }

    public Long getTextChannelId() {
        return textChannelId;
    }

    public void setTextChannel(Long chId) {
        this.textChannelId = chId;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getRole() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public void updateCourse(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
