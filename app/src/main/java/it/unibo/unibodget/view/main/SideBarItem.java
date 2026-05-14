package it.unibo.unibodget.view.main;

import java.util.UUID;

public record SideBarItem(
        UUID id,
        String name,
        String balance,
        boolean selected
) {}
