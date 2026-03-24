package com.example;

public class Main {
    public static void main(String[] args) {
        System.out.println("========== PEN DESIGN SYSTEM ==========\n");

        // Create colors
        Color black = new Color("Black", "#000000");
        Color red = new Color("Red", "#FF0000");
        Color blue = new Color("Blue", "#0000FF");

        // ===== BALLPOINT PEN DEMO =====
        System.out.println("--- BALLPOINT PEN ---");
        Pen ballpointPen = PenFactory.createBallpointPen("Parker", "Jotter", black);
        System.out.println(ballpointPen);
        System.out.println("Ink status: " + ballpointPen.getRefill().getInk());
        
        ballpointPen.write("This won't work because pen is closed");
        ballpointPen.open();
        for (int i = 0; i < 3; i++) {
            ballpointPen.write("Writing with ballpoint pen");
        }
        System.out.println("Remaining ink: " + ballpointPen.getRefill().getInk().getQuantityInMl() + " ml\n");
        ballpointPen.close();

        // ===== GEL PEN DEMO =====
        System.out.println("--- GEL PEN ---");
        Pen gelPen = PenFactory.createGelPen("Pilot", "G2", red);
        System.out.println(gelPen);
        
        gelPen.open();
        for (int i = 0; i < 2; i++) {
            gelPen.write("Smooth gel writing experience");
        }
        System.out.println("Remaining ink: " + gelPen.getRefill().getInk().getQuantityInMl() + " ml\n");
        gelPen.close();

        // ===== FOUNTAIN PEN DEMO =====
        System.out.println("--- FOUNTAIN PEN ---");
        Pen fountainPen = PenFactory.createFountainPen("Montblanc", "Meisterstück", blue);
        System.out.println(fountainPen);
        
        fountainPen.open();
        fountainPen.write("Elegant fountain pen writing");
        fountainPen.write("Beautiful handwriting");
        System.out.println("Remaining ink: " + fountainPen.getRefill().getInk().getQuantityInMl() + " ml\n");
        fountainPen.close();

        // ===== CUSTOM PEN DEMO =====
        System.out.println("--- CUSTOM PEN CREATION ---");
        Color green = new Color("Green", "#008000");
        Pen customBallpoint = PenFactory.createCustomPen("ballpoint", "Cello", "Butterflow", green, 1.2);
        System.out.println(customBallpoint);
        
        customBallpoint.open();
        customBallpoint.write("Custom ballpoint pen");
        customBallpoint.close();
        System.out.println();

        // ===== REFILL REPLACEMENT DEMO =====
        System.out.println("--- REFILL REPLACEMENT ---");
        System.out.println("Original ballpoint pen state:");
        System.out.println(ballpointPen.getRefill().getInk().getColor().getName());
        
        Color purple = new Color("Purple", "#800080");
        Ink newInk = new Ink(purple, 10.0, "Oil-based", 0.8);
        Nib newNib = new Nib(1.0, "Steel", Nib.NibType.BALLPOINT);
        Refill newRefill = new Refill(newInk, newNib);
        
        ballpointPen.replaceRefill(newRefill);
        System.out.println("After refill replacement:");
        System.out.println(ballpointPen.getRefill().getInk().getColor().getName());
        
        ballpointPen.open();
        ballpointPen.write("Now writing with new purple ink");
        ballpointPen.close();
        System.out.println();

        // ===== INK DEPLETION DEMO =====
        System.out.println("--- INK DEPLETION ---");
        Pen lowInkPen = PenFactory.createBallpointPen("Generic", "Cheap", black);
        lowInkPen.getRefill().getInk().depleteInk(9.5); // Almost empty
        System.out.println("Ink level: " + lowInkPen.getRefill().getInk().getQuantityInMl() + " ml");
        
        lowInkPen.open();
        for (int i = 0; i < 3; i++) {
            lowInkPen.write("Writing with low ink");
        }
        System.out.println("Final ink level: " + lowInkPen.getRefill().getInk().getQuantityInMl() + " ml");
    }
}
