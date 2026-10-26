package com.medicinesafetychecker.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public record PatientInput(String name, int age, String gender, double weight, List<String> allergies) {
    public static PatientInput parse(Map<String, String> form) {
        String name = form.getOrDefault("name", "").strip();
        if (name.isBlank() || name.length() > 100 || name.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Name is required and must contain at most 100 printable characters.");
        }
        int age;
        double weight;
        try {
            age = Integer.parseInt(form.getOrDefault("age", "").strip());
            weight = Double.parseDouble(form.getOrDefault("weight", "").strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a whole-number age and a valid weight.");
        }
        if (age < 0 || age > 130) { throw new IllegalArgumentException("Age must be between 0 and 130."); }
        if (!Double.isFinite(weight) || weight <= 0 || weight > 500) {
            throw new IllegalArgumentException("Weight must be greater than 0 and at most 500 kg.");
        }
        String gender = form.getOrDefault("gender", "");
        if (!Set.of("Male", "Female", "Other", "Prefer not to say").contains(gender)) {
            throw new IllegalArgumentException("Select a valid gender.");
        }
        return new PatientInput(name, age, gender, weight, parseAllergies(form.getOrDefault("allergies", "")));
    }

    public static List<String> parseAllergies(String input) {
        if (input == null || input.isBlank()) { return List.of(); }
        if (input.length() > 2000) { throw new IllegalArgumentException("Allergies must contain at most 2000 characters."); }
        List<String> values = new ArrayList<>();
        Set<String> seen = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String part : input.split("[,|]")) {
            String value = part.strip();
            if (value.isBlank()) { continue; }
            if (value.length() > 80 || value.chars().anyMatch(Character::isISOControl)) {
                throw new IllegalArgumentException("Each allergy must contain at most 80 printable characters.");
            }
            if (seen.add(value)) { values.add(value); }
        }
        if (values.size() > 40) { throw new IllegalArgumentException("Enter at most 40 allergies."); }
        return List.copyOf(values);
    }
}
