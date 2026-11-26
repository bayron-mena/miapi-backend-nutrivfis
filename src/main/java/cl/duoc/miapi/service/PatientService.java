package cl.duoc.miapi.service;

import cl.duoc.miapi.model.Patient;
import cl.duoc.miapi.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Optional<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    public Patient create(Patient patient) {
        // Evita que el cliente fuerce un id: lo genera la base de datos
        patient.setId(null);
        return patientRepository.save(patient);
    }

    public Optional<Patient> update(Long id, Patient data) {
        return patientRepository.findById(id).map(existing -> {
            existing.setName(data.getName());
            existing.setSpecies(data.getSpecies());
            existing.setBreed(data.getBreed());
            existing.setAge(data.getAge());
            existing.setOwner(data.getOwner());
            return patientRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!patientRepository.existsById(id)) {
            return false;
        }
        patientRepository.deleteById(id);
        return true;
    }
}
