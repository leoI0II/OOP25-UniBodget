package it.unibo.unibodget.persistency.parser;

/**
 * Simple DTO used for testing parser functionality.
 * Contains a name and a numeric value.
 */
public final class TestDto {

    private String name;
    private int value;

    public TestDto() { 

    }

    public TestDto(final String name, final int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final int value) {
        this.value = value;
    }
}
