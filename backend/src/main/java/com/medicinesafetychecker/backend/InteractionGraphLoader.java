package com.medicinesafetychecker.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.medicinesafetychecker.core.InteractionGraph;
import com.medicinesafetychecker.core.Medicine;

import jakarta.annotation.PreDestroy;

/**
 * Builds the shared {@link InteractionGraph} ({@link Medicine#getKnownInteractions()})
 * from MongoDB on a background thread, so the rest of startup isn't blocked by
 * however long the load takes. {@link PrescriptionController} checks
 * {@link #isReady()} and tells callers to retry if a request comes in before
 * loading has finished.
 *
 * <p>Runs on {@link ApplicationReadyEvent}, which fires after {@link DataSeeder}
 * (an ApplicationRunner) has completed - so the medicines/interactions
 * collections are guaranteed to already have data by the time this reads them.
 */
@Component
public class InteractionGraphLoader {

    private static final Logger log = LoggerFactory.getLogger(InteractionGraphLoader.class);

    private final MedicineRepository medicineRepository;
    private final InteractionRepository interactionRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "interaction-graph-loader");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean ready = new AtomicBoolean(false);

    public InteractionGraphLoader(MedicineRepository medicineRepository,
                                   InteractionRepository interactionRepository) {
        this.medicineRepository = medicineRepository;
        this.interactionRepository = interactionRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startLoading() {
        executor.submit(this::load);
    }

    private void load() {
        try {
            InteractionGraph graph = Medicine.getKnownInteractions();

            Map<String, Medicine> medicinesByName = new HashMap<>();
            for (MedicineDocument doc : medicineRepository.findAll()) {
                Medicine medicine = doc.toDomain();
                medicinesByName.put(medicine.getName().toLowerCase(), medicine);
            }

            int loaded = 0;
            for (InteractionDocument doc : interactionRepository.findAll()) {
                Medicine a = medicinesByName.get(doc.getMedicineNameA().toLowerCase());
                Medicine b = medicinesByName.get(doc.getMedicineNameB().toLowerCase());
                if (a == null || b == null) {
                    log.warn("Skipping interaction {} <-> {}: medicine not found in medicines collection",
                            doc.getMedicineNameA(), doc.getMedicineNameB());
                    continue;
                }
                graph.addInteraction(a, b, doc.getSeverity(), doc.getDescription());
                loaded++;
            }

            ready.set(true);
            log.info("Interaction graph ready: {} medicines, {} interactions loaded",
                    medicinesByName.size(), loaded);
        } catch (RuntimeException e) {
            log.error("Failed to load interaction graph", e);
        }
    }

    public boolean isReady() {
        return ready.get();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
