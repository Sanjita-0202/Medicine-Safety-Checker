package com.medicinesafetychecker.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads the seed CSV files (data/seed/medicines.csv, data/seed/interactions.csv)
 * into domain objects. Lines starting with '#' are comments; the first
 * non-comment line is the column header. A row that fails to parse is
 * skipped (with a warning on stderr) rather than aborting the whole load -
 * one bad line in a hand-edited CSV shouldn't take down the rest of it.
 */
public final class CsvLoader {

    private CsvLoader() {
    }

    /** Reads a medicines CSV (id,name,type,dosageUnit,maxSingleDose,maxDailyDose). */
    public static List<Medicine> loadMedicines(Path csvPath) throws IOException {
        List<Medicine> medicines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            int lineNumber = 0;
            boolean headerSeen = false;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (!headerSeen) {
                    headerSeen = true;
                    continue;
                }
                try {
                    medicines.add(parseMedicine(line));
                } catch (RuntimeException e) {
                    System.err.println("Skipping bad medicine row " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        return medicines;
    }

    private static Medicine parseMedicine(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length != 6) {
            throw new IllegalArgumentException("expected 6 columns, found " + fields.length);
        }
        String id = fields[0].strip();
        String name = fields[1].strip();
        String type = fields[2].strip().toUpperCase(Locale.ROOT);
        String dosageUnit = fields[3].strip();
        double maxSingleDose = Double.parseDouble(fields[4].strip());
        double maxDailyDose = Double.parseDouble(fields[5].strip());

        return switch (type) {
            case "ANTIBIOTIC" -> new Antibiotic(id, name, dosageUnit, maxSingleDose, maxDailyDose);
            case "PAINKILLER" -> new Painkiller(id, name, dosageUnit, maxSingleDose, maxDailyDose);
            case "ANTIHYPERTENSIVE" -> new Antihypertensive(id, name, dosageUnit, maxSingleDose, maxDailyDose);
            default -> throw new IllegalArgumentException("unknown medicine type: " + type);
        };
    }

    /** Reads an interactions CSV (medicineNameA,medicineNameB,severity,description). */
    public static List<Interaction> loadInteractions(Path csvPath) throws IOException {
        List<Interaction> interactions = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            int lineNumber = 0;
            boolean headerSeen = false;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (!headerSeen) {
                    headerSeen = true;
                    continue;
                }
                try {
                    interactions.add(parseInteraction(line));
                } catch (RuntimeException e) {
                    System.err.println("Skipping bad interaction row " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        return interactions;
    }

    private static Interaction parseInteraction(String line) {
        String[] fields = splitCsvLine(line);
        if (fields.length != 4) {
            throw new IllegalArgumentException("expected 4 columns, found " + fields.length);
        }
        String nameA = fields[0].strip();
        String nameB = fields[1].strip();
        Severity severity = parseSeverity(fields[2].strip());
        String description = fields[3].strip();
        return new Interaction(nameA, nameB, severity, description);
    }

    private static Severity parseSeverity(String csvSeverity) {
        return switch (csvSeverity.toUpperCase(Locale.ROOT)) {
            case "MAJOR" -> Severity.SEVERE;
            case "MODERATE" -> Severity.MODERATE;
            case "MINOR" -> Severity.LOW;
            default -> throw new IllegalArgumentException("unknown severity: " + csvSeverity);
        };
    }

    /**
     * Splits one CSV line into fields, respecting double-quoted fields that
     * may contain commas (the raw Kaggle dataset quotes some descriptions
     * this way, and {@code InteractionCsvBuilder} carries that quoting through).
     */
    public static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    /** Quotes a CSV field if it contains a comma or a double quote. */
    public static String escapeCsvField(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
