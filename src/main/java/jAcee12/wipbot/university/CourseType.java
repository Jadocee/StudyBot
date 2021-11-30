package jAcee12.wipbot.university;

public class CourseType {
    private final String name;
    private final Long categoryId;

    public CourseType(String name, Long id) {
        this.name = name;
        this.categoryId = id;
    }

    public final String getName(){
        return name;
    }

    public final Long getCategoryId() {
        return categoryId;
    }
}
