package com.example.socialchat;

//Friends is the model to store date
public class Friends
{
    //Declare the date variabele
    public String date;

    public String name;
    public String status;
    public String image;
    public String thumb_image;

    //Empty constructor
    public Friends()
    {

    }

    //Filled constructor
    public Friends(String date, String name, String status, String image, String thumb_image)
    {
        this.date = date;

        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
    }

    //Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getThumb_image()
    {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image)
    {
        this.thumb_image = thumb_image;
    }




}
