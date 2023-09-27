package com.hieujoyce.firstdemo.models;

public class User {
    int srcId;
    String name, dec;
    public User(int srcId, String name, String dec) {
        this.srcId = srcId;
        this.name = name;
        this.dec = dec;
    }

    public int getSrcId() {
        return srcId;
    }

    public String getName() {
        return name;
    }

    public String getDec() {
        return dec;
    }
}
