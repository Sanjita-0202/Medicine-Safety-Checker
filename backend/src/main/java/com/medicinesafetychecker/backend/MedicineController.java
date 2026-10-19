package com.medicinesafetychecker.backend;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medicinesafetychecker.core.Medicine;
import com.medicinesafetychecker.core.OverdoseRiskException;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineRepository medicineRepository;

    public MedicineController(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @GetMapping
    public List<MedicineDocument> getAllMedicines() {
        return medicineRepository.findAll();
    }

    /** GET /api/medicines/{id}/dosage-check?doseMg=650&timesPerDay=3 */
    @GetMapping("/{id}/dosage-check")
    public boolean isDosageSafe(@PathVariable String id, @RequestParam double doseMg,
                                 @RequestParam int timesPerDay) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown medicine id: " + id))
                .toDomain();
        try {
            medicine.validateDose(doseMg, timesPerDay);
            return true;
        } catch (OverdoseRiskException e) {
            return false;
        }
    }
}
