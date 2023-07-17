package me.galazeek.ethereal.exceptions;

public class CourseNameExistsException extends Exception {

    public CourseNameExistsException(String courseName) {
        super("Course name '" + courseName + "' already exists");
    }
}
