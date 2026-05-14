package it.unibo.unibodget.view.main;

import java.util.List;
import java.util.UUID;

public interface SideBarDelegate {
    List<SideBarItem> getItems();
    String getTotalAggregatedBalance();
    void onItemSelected(UUID id);
}
