package com.medicinesafetychecker.core;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.medicinesafetychecker.core.tools.InteractionCsvBuilder;

class SeverityOverrideTest {
    @TempDir Path directory;

    private Path prepare(String filename, String content) throws Exception {
        Path file = directory.resolve(filename); Files.writeString(file, content); return file;
    }

    @Test void manualOverrideWinsForReversedCaseInsensitivePairAndDuplicatesAreRemoved() throws Exception {
        Path medicines = prepare("medicines.csv", "id,name,type,dosageUnit,maxSingleDose,maxDailyDose\na,Aspirin,PAINKILLER,mg,500,1000\nb,Ibuprofen,PAINKILLER,mg,400,1200\n");
        Path raw = prepare("raw.csv", "a,b,description\nAspirin,Ibuprofen,Fatal bleeding\nIbuprofen,Aspirin,Fatal bleeding\n");
        Path overrides = prepare("overrides.csv", "medicineNameA,medicineNameB,severity\nibuprofen,ASPIRIN,LOW\n");
        Path output = directory.resolve("output.csv");
        InteractionCsvBuilder.main(new String[]{raw.toString(), medicines.toString(), output.toString(), overrides.toString()});
        var interactions = CsvLoader.loadInteractions(output);
        assertEquals(1, interactions.size());
        assertEquals(Severity.LOW, interactions.getFirst().getSeverity());
        assertTrue(Files.readString(output).contains("Fatal bleeding"));
    }

    @Test void invalidAndConflictingOverridesFailBeforeChangingOutput() throws Exception {
        Path medicines = prepare("medicines.csv", "id,name,type,dosageUnit,maxSingleDose,maxDailyDose\na,Aspirin,PAINKILLER,mg,500,1000\nb,Ibuprofen,PAINKILLER,mg,400,1200\n");
        Path raw = prepare("raw.csv", "a,b,description\nAspirin,Ibuprofen,Bleeding\n");
        Path output = prepare("output.csv", "original");
        for (String rows : new String[]{"Aspirin,Ibuprofen,BAD", "Unknown,Aspirin,MAJOR", "Aspirin,Ibuprofen,MAJOR\nIbuprofen,Aspirin,MINOR"}) {
            Path overrides = prepare("overrides.csv", "a,b,severity\n" + rows + "\n");
            assertThrows(IOException.class, () -> InteractionCsvBuilder.main(new String[]{raw.toString(), medicines.toString(), output.toString(), overrides.toString()}));
            assertEquals("original", Files.readString(output));
        }
    }

    @Test void headerOnlyOverridesRetainKeywordClassification() throws Exception {
        Path medicines = prepare("medicines.csv", "id,name,type,dosageUnit,maxSingleDose,maxDailyDose\na,Aspirin,PAINKILLER,mg,500,1000\nb,Ibuprofen,PAINKILLER,mg,400,1200\n");
        Path raw = prepare("raw.csv", "a,b,description\nAspirin,Ibuprofen,Fatal bleeding\n");
        Path overrides = prepare("overrides.csv", "medicineNameA,medicineNameB,severity\n");
        Path output = directory.resolve("output.csv");
        InteractionCsvBuilder.main(new String[]{raw.toString(), medicines.toString(), output.toString(), overrides.toString()});
        assertEquals(Severity.SEVERE, CsvLoader.loadInteractions(output).getFirst().getSeverity());
    }
}
