package org.daisleyharrison.security.samples.spring.web.models;

public class Test {
    private String id;
    private int one;
    private int two;
    private String three;

    public Test() {
        this(null);
    }

    public Test(String id) {
        this.id = id;
        this.one = 1;
        this.two = 2;
        this.three = "three";
    }

    public int getOne() {
        return one;
    }

    public void setOne(int one) {
        this.one = one;
    }

    public int getTwo() {
        return two;
    }

    public void setTwo(int two) {
        this.two = two;
    }

    public String getThree() {
        return three;
    }

    public void setThree(String three) {
        this.three = three;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}