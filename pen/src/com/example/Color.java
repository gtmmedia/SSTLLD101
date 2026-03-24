package com.example;

public class Color {
    private String name;
    private String hexCode;

    public Color(String name, String hexCode) {
        this.name = name;
        this.hexCode = hexCode;
    }

    public String getName() {
        return name;
    }

    public String getHexCode() {
        return hexCode;
    }

    @Override
    public String toString() {
        return "Color{" +
                "name='" + name + '\'' +
                ", hexCode='" + hexCode + '\'' +
                '}';
    }
}
