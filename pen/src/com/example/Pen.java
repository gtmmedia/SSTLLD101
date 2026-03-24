package com.example;

public abstract class Pen {
    protected String brand;
    protected String model;
    protected Refill refill;
    protected boolean isOpen;

    public Pen(String brand, String model, Refill refill) {
        this.brand = brand;
        this.model = model;
        this.refill = refill;
        this.isOpen = false;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public Refill getRefill() {
        return refill;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void open() {
        if (!isOpen) {
            isOpen = true;
            System.out.println(brand + " " + model + " pen is opened");
        }
    }

    public void close() {
        if (isOpen) {
            isOpen = false;
            System.out.println(brand + " " + model + " pen is closed");
        }
    }

    public abstract void write(String text);

    public abstract String getType();

    public void replaceRefill(Refill newRefill) {
        this.refill = newRefill;
        System.out.println("Refill replaced successfully");
    }

    public boolean hasInk() {
        return !refill.getInk().isEmpty();
    }

    @Override
    public String toString() {
        return "Pen{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", type='" + getType() + '\'' +
                ", isOpen=" + isOpen +
                ", refill=" + refill +
                '}';
    }
}
