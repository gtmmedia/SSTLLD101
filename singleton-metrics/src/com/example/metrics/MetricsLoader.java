package com.example.metrics;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads default metric keys from a properties file.
 *
 * CURRENT STATE (BROKEN ON PURPOSE):
 * - Uses 'new MetricsRegistry()' instead of the singleton.
 *
 * TODO (student):
 *  - Use MetricsRegistry.getInstance() and remove all direct instantiation.
 */
public class MetricsLoader {

    public MetricsRegistry loadFromFile(String path) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        }

        // Use the singleton instance
        MetricsRegistry registry = MetricsRegistry.getInstance();

        for (String key : props.stringPropertyNames()) {
            String raw = props.getProperty(key, "0").trim();
            long v;
            try {
                v = Long.parseLong(raw);
            } catch (NumberFormatException e) {
                v = 0L;
            }
            registry.setCount(key, v);
        }
        return registry;
    }
}
