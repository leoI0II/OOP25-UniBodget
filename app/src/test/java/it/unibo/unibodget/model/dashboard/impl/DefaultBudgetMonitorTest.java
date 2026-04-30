// package it.unibo.unibodget.model.dashboard.impl;

// import static org.junit.jupiter.api.Assertions.assertEquals;

// import org.junit.jupiter.api.Test;

// final class DefaultBudgetMonitorTest {

//     @Test
//     void shouldReturnSafeWhenUsageIsBelowWarningThreshold() {
//         final var monitor = new DefaultBudgetMonitor();
//         assertEquals("SAFE", monitor.getBudgetStatus(50.0, 100.0));
//     }

//     @Test
//     void shouldReturnWarningWhenUsageReachesWarningThreshold() {
//         final var monitor = new DefaultBudgetMonitor();
//         assertEquals("WARNING", monitor.getBudgetStatus(80.0, 100.0));
//     }

//     @Test
//     void shouldReturnCriticalWhenUsageReachesCriticalThreshold() {
//         final var monitor = new DefaultBudgetMonitor();
//         assertEquals("CRITICAL", monitor.getBudgetStatus(100.0, 100.0));
//     }

//     @Test
//     void shouldReturnSafeWhenLimitIsNonPositive() {
//         final var monitor = new DefaultBudgetMonitor();
//         assertEquals("SAFE", monitor.getBudgetStatus(10.0, 0.0));
//     }
// }