package cl.duoc.miapi.config;

import cl.duoc.miapi.model.Paciente;
import cl.duoc.miapi.repository.PacienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initPacientes(PacienteRepository pacienteRepository) {
        return args -> {
            if (pacienteRepository.count() == 0) {
                pacienteRepository.save(new Paciente("Luna", "Perro", "Labrador", 3));
                pacienteRepository.save(new Paciente("Milo", "Gato", "Siamés", 2));
                pacienteRepository.save(new Paciente("Rocky", "Perro", "Bulldog", 5));
            }
        };
    }
}
