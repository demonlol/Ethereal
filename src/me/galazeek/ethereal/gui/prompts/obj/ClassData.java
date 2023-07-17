package me.galazeek.ethereal.gui.prompts.obj;

import java.util.Date;

public class ClassData {

    private String name, teacher;
    private double credits;
    private Date start, end;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "ClassData{" +
                "name='" + name + '\'' +
                ", teacher='" + teacher + '\'' +
                ", credits=" + credits +
                ", start=" + start.getTime() +
                ", end=" + end.getTime() +
                '}';
    }
}
