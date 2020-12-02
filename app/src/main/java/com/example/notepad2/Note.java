package com.example.notepad2;

public class Note {

    private String contents;
    private String date;
     int item_index;
     private boolean isChecked;


    public Note(String contents, String date,int i) {
        this.contents = contents;
        this.date = date;
        this.item_index=i;
        isChecked=false;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }


    public int getItem_index() {
        return item_index;
    }
    public void setItem_index(int i) {
        this.item_index = i;
    }


    public boolean isChecked() {
        return isChecked;
    }
    public boolean setChecked() {
        return isChecked;
    }
}
