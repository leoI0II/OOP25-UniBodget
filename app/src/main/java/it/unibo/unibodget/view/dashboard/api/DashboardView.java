package it.unibo.unibodget.view.dashboard.api;

import it.unibo.unibodget.view.dashboard.state.DashboardViewState;

/**
 * Contract for the dashboard view.
 *
 * <p>The dashboard view is responsible only for rendering UI state
 * and forwarding user interactions through bound actions.</p>
 */
public interface DashboardView {

    /**
     * Binds the action callbacks used by this view.
     *
     * @param actions the action handlers exposed by the controller
     */
    void bindActions(DashboardViewActions actions);

    /**
     * Renders the given immutable dashboard UI state.
     *
     * @param state the state to render
     */
    void render(DashboardViewState state);

    /**
     * Displays an error message in the view.
     *
     * @param message the message to show
     */
    void showError(String message);
}