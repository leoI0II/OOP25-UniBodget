package it.unibo.unibodget.view.main;

import java.util.List;
import java.util.UUID;

/**
 * Delegate interface between the {@link SideBarViewController} and the currently active content view.
 * The sidebar calls these methods to populate itself and react to user interactions.
 */
public interface SideBarDelegate {

    /**
     * Returns the list of items to display in the sidebar (one per wallet/account).
     *
     * @return an unmodifiable list of {@link SideBarItem}
     */
    List<SideBarItem> getItems();

    /**
     * Returns the total aggregated balance across all accounts, formatted as a display string.
     *
     * @return formatted balance string (e.g. {@code "$ 1 234.56"})
     */
    String getTotalAggregatedBalance();

    /**
     * Called when the user selects a wallet in the sidebar.
     *
     * @param id the UUID of the selected wallet
     */
    void onItemSelected(UUID id);

    /**
     * Called when the user requests to add a new wallet via the sidebar.
     */
    void onAddWalletRequested();

    /**
     * Called when the user confirms a new name for a wallet from the sidebar context menu.
     * The default implementation does nothing.
     *
     * <p>Implementations must let the model's {@link IllegalArgumentException} propagate:
     * the sidebar shows its message and asks for another name.</p>
     *
     * @param id      the UUID of the wallet to rename
     * @param newName the new name typed by the user, already trimmed
     * @throws IllegalArgumentException if the name breaks the wallet naming rules
     */
    default void onRenameRequested(final UUID id, final String newName) { }

    /**
     * Called when the user confirms the deletion of a wallet from the sidebar context menu.
     * The default implementation does nothing.
     *
     * @param id the UUID of the wallet to delete
     */
    default void onDeleteRequested(final UUID id) { }
}
