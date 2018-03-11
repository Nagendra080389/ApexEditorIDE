package com.forceFilesEditor.model;

/**
 * Model Class for person to hold Login Username and Password.
 */
public class Person {

    private String username;
    private String password;

    public Person() {
    }

    public Person(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
