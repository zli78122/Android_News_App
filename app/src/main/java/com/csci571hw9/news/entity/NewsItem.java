package com.csci571hw9.news.entity;

public class NewsItem {
    private String id;
    private String image;
    private String title;
    private String date;
    private String section;

    public NewsItem(String id, String image, String title, String date, String section) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.date = date;
        this.section = section;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String toString() {
        return "NewsItem{" +
                "image='" + image + '\'' +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", section='" + section + '\'' +
                '}';
    }
}
