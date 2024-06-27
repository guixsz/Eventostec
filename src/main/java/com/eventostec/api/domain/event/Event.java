package com.eventostec.api.domain.event;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;
import java.util.UUID;

@Table(name = "event")
@Entity
public class Event {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String description;
    private String imgUrl;
    private Boolean remote;
    private Date date;

    public Event() {
    }

    public Event(UUID id, String title, String description, String imgUrl, Boolean remote, Date date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imgUrl = imgUrl;
        this.remote = remote;
        this.date = date;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Boolean getRemote() {
        return remote;
    }

    public void setRemote(Boolean remote) {
        this.remote = remote;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
