package cl.duoc.miapi.service;

import cl.duoc.miapi.model.Paciente;
import cl.duoc.miapi.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> buscarPorId(Long id) {
        return pacienteRepository.findById(id);
    }

    public Paciente guardar(Paciente paciente) {
        // Evita que el cliente fuerce un id: lo genera la base de datos
        paciente.setId(null);
        return pacienteRepository.save(paciente);
    }

    public Optional<Paciente> actualizar(Long id, Paciente datos) {
        return pacienteRepository.findById(id).map(existente -> {
            existente.setNombre(datos.getNombre());
            existente.setEspecie(datos.getEspecie());
            existente.setRaza(datos.getRaza());
            existente.setEdad(datos.getEdad());
            return pacienteRepository.save(existente);
        });
    }

    public boolean eliminar(Long id) {
        if (!pacienteRepository.existsById(id)) {
            return false;
        }
        pacienteRepository.deleteById(id);
        return true;
    }
}
