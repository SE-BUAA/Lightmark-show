package top.ortus.timemark.backend.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

public class Constant {
    public static final String[] stations = loadStations();

    private Constant() {
    }

    private static String[] loadStations() {
        Set<String> stationSet = new LinkedHashSet<>();
        try (InputStream inputStream = Constant.class.getClassLoader().getResourceAsStream("station.csv")) {
            if (inputStream == null) {
                return new String[0];
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (columns.length > 1 && !columns[1].isBlank()) {
                        stationSet.add(columns[1].trim());
                    }
                }
            }
        } catch (Exception ignored) {
            return new String[0];
        }
        return stationSet.toArray(String[]::new);
    }
}
