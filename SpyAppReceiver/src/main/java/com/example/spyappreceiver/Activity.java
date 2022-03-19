package com.example.spyappreceiver;

public class Activity {
    String title, message, datetime;

    public Activity(String datetime){

    }
    public Activity(String datetime, String message, String title){
        this.datetime = datetime;
        this.message = message;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "type='" + title + '\'' +
                ", activity='" + message + '\'' +
                ", datetime='" + datetime + '\'' +
                '}';
    }

    public String getType() {
        return title;
    }

    public String getActivity() {
        return message;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setType(String type) {
        this.title = title;
    }

    public void setActivity(String activity) {
        this.message = message;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
