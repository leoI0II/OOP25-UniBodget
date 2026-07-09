package it.unibo.unibodget.model.dashboard.impl;

/**
 * Represents the semantic direction of an insight.
 *
 * <p>This value can be used by the UI layer to determine colors,
 * icons, or emphasis associated with a dashboard message.</p>
 */
public enum InsightTrend {

    /**
     * Indicates an improvement or favorable result.
     */
    POSITIVE,

    /**
     * Indicates a worsening or unfavorable result.
     */
    NEGATIVE,

    /**
     * Indicates no significant change or a purely informational result.
     */
    NEUTRAL
}