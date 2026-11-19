package cl.duoc.miapi.config;

import cl.duoc.miapi.model.Categoria;
import cl.duoc.miapi.model.Paciente;
import cl.duoc.miapi.repository.CategoriaRepository;
import cl.duoc.miapi.repository.PacienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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

    @Bean
    CommandLineRunner initCategorias(CategoriaRepository categoriaRepository) {
        return args -> {
            if (categoriaRepository.count() == 0) {
                List<String> nombres = List.of(
                        "Antibióticos", "Antiparasitarios", "Antiinflamatorios",
                        "Dermatología", "Digestivo", "Cardíaco",
                        "Analgésicos", "Vacunas", "Suplementos");
                nombres.forEach(nombre -> categoriaRepository.save(new Categoria(nombre)));
            }
        };
    }
}
