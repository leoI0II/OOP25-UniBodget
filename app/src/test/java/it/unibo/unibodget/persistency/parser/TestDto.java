package it.unibo.unibodget.persistency.parser;

/**
 * Simple DTO used for testing parser functionality.
 * Contains a name and a numeric value.
 */
public final class TestDto {

    private String name;
    private int value;

    /**
     * Default constructor required for parser tests and reflective instantiation.
     * 
     * <p>
     * Creates an empty DTO with uninitialized fields.
     * </p>
     */
    public TestDto() { 

    }

    /**
     * Creates a new TestDto with the given name and value.
     *
     * @param name  the name to assign to this DTO
     * @param value the numeric value to assign
     */
    public TestDto(final String name, final int value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the name stored in this DTO.
     *
     * @return the current name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the numeric value stored in this DTO.
     *
     * @return the current numeric value
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Sets the name of this DTO.
     *
     * @param name the new name to assign
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the numeric value of this DTO.
     *
     * @param value the new numeric value to assign
     */
    public void setValue(final int value) {
        this.value = value;
    }
}
