package it.unibo.unibodget.view.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SideBarViewControllerTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> { });
        } catch (final IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    @Test
    void shouldReturnDefaultMinWidthForEmptyOrShortBalance() {
        final SideBarViewController controller = new SideBarViewController();

        assertEquals(SideBarViewController.MIN_SIDEBAR_WIDTH, controller.computeRequiredWidth(null));
        assertEquals(SideBarViewController.MIN_SIDEBAR_WIDTH, controller.computeRequiredWidth(""));
        assertEquals(SideBarViewController.MIN_SIDEBAR_WIDTH, controller.computeRequiredWidth("   "));
        assertEquals(SideBarViewController.MIN_SIDEBAR_WIDTH, controller.computeRequiredWidth("$0.00"));
    }

    @Test
    void shouldExpandWidthForLongBalanceAndClampToMax() {
        final SideBarViewController controller = new SideBarViewController();

        final double wideWidth = controller.computeRequiredWidth("€ -123,456,789.99");
        assertTrue(wideWidth > SideBarViewController.MIN_SIDEBAR_WIDTH,
                "Long balance should expand sidebar beyond min width");
        assertTrue(wideWidth <= SideBarViewController.MAX_SIDEBAR_WIDTH,
                "Expanded width should not exceed max width");

        final String hugeBalance = "€ -".repeat(50);
        final double maxClamped = controller.computeRequiredWidth(hugeBalance);
        assertEquals(SideBarViewController.MAX_SIDEBAR_WIDTH, maxClamped,
                "Very long balance must clamp at maximum sidebar width");
    }

    @Test
    void shouldNotifyWidthAdjustmentHandler() {
        final SideBarViewController controller = new SideBarViewController();
        final AtomicReference<Double> receivedWidth = new AtomicReference<>();

        controller.setWidthAdjustmentHandler(receivedWidth::set);
        controller.updateWidthForTotal("€ -123,456,789.99");

        assertNotNull(receivedWidth.get());
        assertTrue(receivedWidth.get() > SideBarViewController.MIN_SIDEBAR_WIDTH);
    }
}
