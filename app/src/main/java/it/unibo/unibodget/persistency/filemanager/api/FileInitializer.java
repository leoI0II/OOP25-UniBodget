<<<<<<< HEAD
=======
package it.unibo.unibodget.persistency.filemanager.api;

/**
 * Defines the contract for initializing a file required by the
 * persistence layer.
 *
 * Responsible for ensuring that a target file exists and is ready 
 * to be used by readers, writers, parsers, or serializers. 
 * 
 * Implementations may create the file if it does not exist, 
 * populate it with default content, or validate its
 * current structure.
 */
public interface FileInitializer {
    /**
     * Ensures that the target file is present and properly initialized.
     *
     * If the file does not exist, implementations may create it and
     * optionally write default content. If the file exists but is not
     * valid implementations may repair or reinitialize it.
     *
     * @throws FileInitializationException if the file cannot be created,
     *                                     validated, or initialized
     */
    void initialize() throws FileInitializationException;
}
>>>>>>> 9c6fcfd (updated fileinitializer interface and comments)
