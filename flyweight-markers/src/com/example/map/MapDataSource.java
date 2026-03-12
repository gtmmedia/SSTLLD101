package com.example.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates markers for demo/testing.
 *
 * CURRENT STATE (BROKEN ON PURPOSE):
 * - Creates new MarkerStyle per MapMarker via MapMarker constructor.
 *
 * TODO (student):
 * - After introducing MarkerStyleFactory, refactor so identical styles are shared.
 * - Suggested approach:
 *   1) Change MapMarker to accept MarkerStyle directly
 *   2) Use MarkerStyleFactory.get(shape,color,size,filled) here
 */
public class MapDataSource {

    private static final String[] SHAPES = {"PIN", "CIRCLE", "SQUARE"};
    private static final String[] COLORS = {"RED", "BLUE", "GREEN", "ORANGE"};
    private static final int[] SIZES = {10, 12, 14, 16};

    public List<MapMarker> loadMarkers(int count) {
        Random rnd = new Random(7);
        List<MapMarker> out = new ArrayList<>(count);
        MarkerStyleFactory factory = new MarkerStyleFactory();

        for (int i = 0; i < count; i++) {
            double lat = 12.9000 + rnd.nextDouble() * 0.2000;
            double lng = 77.5000 + rnd.nextDouble() * 0.2000;
            String label = "M-" + i;

            // Force many duplicates by choosing from small pools
            String shape = SHAPES[rnd.nextInt(SHAPES.length)];
            String color = COLORS[rnd.nextInt(COLORS.length)];
            int size = SIZES[rnd.nextInt(SIZES.length)];
            boolean filled = rnd.nextBoolean();

            // Use factory to get shared style instance
            MarkerStyle style = factory.get(shape, color, size, filled);
            out.add(new MapMarker(lat, lng, label, style));
        }
        return out;
    }
}
