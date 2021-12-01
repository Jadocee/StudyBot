package jAcee12.wipbot.university;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseType that = (CourseType) o;
        return Objects.equals(name, that.name) && Objects.equals(categoryId, that.categoryId);
    }
}
