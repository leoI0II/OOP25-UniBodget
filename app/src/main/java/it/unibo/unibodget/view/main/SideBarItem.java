package it.unibo.unibodget.view.main;

import java.util.UUID;

/**
 * Immutable view model for a single entry in the sidebar wallet list.
 *
 * @param id       the unique identifier of the wallet
 * @param name     the display name of the wallet
 * @param balance  the formatted balance string shown under the name
 * @param selected whether this item is currently selected in the sidebar
 */
public record SideBarItem(
        UUID id,
        String name,
        String balance,
        boolean selected
) { }
