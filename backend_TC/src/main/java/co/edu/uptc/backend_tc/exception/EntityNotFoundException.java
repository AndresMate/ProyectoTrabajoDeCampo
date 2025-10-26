package co.edu.uptc.backend_tc.exception;

/**
 * Excepción que indica que una entidad específica no fue encontrada en la base de datos.
 * Hereda de {@link ResourceNotFoundException} para ser manejada como error 404.
 */
public class EntityNotFoundException extends ResourceNotFoundException {

    /**
     * Crea una excepción con mensaje estándar indicando la entidad e ID no encontrados.
     *
     * @param entity Nombre de la entidad (por ejemplo: "Inscripción", "Torneo", "Equipo")
     * @param id     Identificador de la entidad buscada
     */
    public EntityNotFoundException(String entity, Object id) {
        super(String.format("%s con id %s no encontrada", entity, id));
    }

    /**
     * Crea una excepción con un mensaje personalizado.
     *
     * @param message Mensaje descriptivo del error
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Crea una excepción con mensaje y campo específico.
     *
     * @param entity Nombre de la entidad
     * @param field  Campo que generó el error
     * @param value  Valor del campo
     */
    public EntityNotFoundException(String entity, String field, Object value) {
        super(entity + " no encontrada", field, value);
    }
}
