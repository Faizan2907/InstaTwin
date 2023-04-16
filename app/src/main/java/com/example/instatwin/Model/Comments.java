package com.example.instatwin.Model;

public class Comments extends CommentId{
    //name should be same as the firebase
    private String comment, user;

    public String getComment() {
        return comment;
    }

    public String getUser() {
        return user;
    }
}
