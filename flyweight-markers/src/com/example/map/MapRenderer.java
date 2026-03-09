package com.example.map;

import java.util.List;

/**
 * Renders markers to console (simulating a UI renderer).
 */
public class MapRenderer {

    public void render(List<MapMarker> markers) {
        System.out.println("Rendering " + markers.size() + " markers...");
        int shown = 0;

        for (MapMarker m : markers) {
            // Simulate some "rendering" work
            if (shown < 8) {
                System.out.println(format(m));
                shown++;
            }
        }

        if (markers.size() > shown) {
            System.out.println("... (" + (markers.size() - shown) + " more not shown)");
        }
    }

    private String format(MapMarker m) {
        return String.format("%s @ (%.4f, %.4f) style=%s",
                m.getLabel(), m.getLat(), m.getLng(), m.getStyle());
    }
}
