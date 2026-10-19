package com.medicinesafetychecker.backend;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medicinesafetychecker.core.DosageChecker;
import com.medicinesafetychecker.core.Medicine;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineRepository medicineRepository;
    private final DosageChecker dosageChecker = new DosageChecker();

    public MedicineController(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @GetMapping
    public List<MedicineDocument> getAllMedicines() {
        return medicineRepository.findAll();
    }

    /** GET /api/medicines/{id}/dosage-check?doseMg=650 */
    @GetMapping("/{id}/dosage-check")
    public boolean isDosageSafe(@PathVariable String id, @RequestParam double doseMg) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown medicine id: " + id))
                .toDomain();
        return dosageChecker.isDosageSafe(medicine, doseMg);
    }
}
