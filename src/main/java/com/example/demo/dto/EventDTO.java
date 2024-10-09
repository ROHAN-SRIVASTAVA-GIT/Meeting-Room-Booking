package com.example.demo.dto;

public class EventDTO {
    private String title;
    private String start;
    private String end;
    private String description;
    private String bookedBy;

    public EventDTO(String title, String start, String end, String description, String bookedBy) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.description = description;
        this.bookedBy = bookedBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }
}
