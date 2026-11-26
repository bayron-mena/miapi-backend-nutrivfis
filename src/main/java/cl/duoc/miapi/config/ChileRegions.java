package cl.duoc.miapi.config;

import java.util.List;
import java.util.Map;

// Copia de src/data/regiones.ts del frontend. Si se agrega una región o comuna
// allá, hay que agregarla también aquí.
public final class ChileRegions {

    public static final Map<String, List<String>> REGION_COMUNAS = Map.ofEntries(
            Map.entry("Arica y Parinacota", List.of("Arica", "Camarones", "Putre", "General Lagos")),
            Map.entry("Tarapacá", List.of("Iquique", "Alto Hospicio", "Pozo Almonte")),
            Map.entry("Antofagasta", List.of("Antofagasta", "Calama", "Tocopilla")),
            Map.entry("Atacama", List.of("Copiapó", "Vallenar", "Chañaral")),
            Map.entry("Coquimbo", List.of("La Serena", "Coquimbo", "Ovalle")),
            Map.entry("Valparaíso", List.of("Valparaíso", "Viña del Mar", "Quilpué")),
            Map.entry("Metropolitana de Santiago", List.of("Santiago", "Providencia", "Las Condes", "Maipú", "Puente Alto", "Ñuñoa", "La Florida", "Peñalolén")),
            Map.entry("Libertador General Bernardo O'Higgins", List.of("Rancagua", "San Fernando", "Rengo")),
            Map.entry("Maule", List.of("Talca", "Curicó", "Linares")),
            Map.entry("Ñuble", List.of("Chillán", "San Carlos", "Bulnes")),
            Map.entry("Biobío", List.of("Concepción", "Talcahuano", "Los Ángeles")),
            Map.entry("La Araucanía", List.of("Temuco", "Villarrica", "Angol")),
            Map.entry("Los Ríos", List.of("Valdivia", "La Unión")),
            Map.entry("Los Lagos", List.of("Puerto Montt", "Osorno", "Castro")),
            Map.entry("Aysén", List.of("Coyhaique", "Puerto Aysén")),
            Map.entry("Magallanes", List.of("Punta Arenas", "Puerto Natales"))
    );

    private ChileRegions() {
    }

    public static boolean isValidRegion(String region) {
        return region != null && REGION_COMUNAS.containsKey(region);
    }

    public static boolean isValidComuna(String region, String comuna) {
        return isValidRegion(region) && REGION_COMUNAS.get(region).contains(comuna);
    }
}
