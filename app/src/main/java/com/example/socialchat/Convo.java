package com.example.socialchat;

public class Convo
{
    //Variabele
    public boolean seen;
    public long timestamp;

    //Empty constructor
    public Convo(){

    }

    //Getters and Setters
    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    //Filled constructor
    public Convo(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }
}
