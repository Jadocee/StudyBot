package jAcee12.wipbot.university;

public class Course {
    private String name;
    private String code;

    Course(String name, String code) {
        this.code = code;
        this.name = name;
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

    public void updateCourse(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
