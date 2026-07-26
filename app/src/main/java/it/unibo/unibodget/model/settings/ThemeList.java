package it.unibo.unibodget.model.settings;

import it.unibo.unibodget.persistency.ModelFileManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads and provides access to all available {@link Theme} instances.
 *
 * <p>The list of themes is loaded once from
 * <code>data/json/settings/Theme.json</code> and cached statically.
 * If the file is missing or invalid, the list falls back to
 * containing only {@link Theme#DEFAULT}.</p>
 *
 * <p>This class is lightweight and stateless: it exposes only a
 * getter and performs initialization lazily on first construction.</p>
 */
public final class ThemeList {

    private static final Path PATH = Path.of("data/json/settings/Theme.json");
    private static final String RESOURCE = "/json/settings/Theme.json";

    private static boolean initialized = false;

    /** Cached list of loaded themes. */
    private static final List<Theme> LOADED = new ArrayList<>();

    /**
     * Creates a ThemeList and initializes the theme list if needed.
     */
    public ThemeList() {
        if (!initialized) {
            init();
        }
    }

    /**
     * Returns an immutable list of all loaded themes.
     *
     * @return unmodifiable list of themes
     */
    public List<Theme> getThemes() {
        return Collections.unmodifiableList(LOADED);
    }

    /**
     * Loads themes from disk using {@link ModelFileManager}.
     *
     * <p>If loading fails or the file is empty, the list is replaced
     * with a single default theme.</p>
     */
    private static void init() {
        try {
            // Load Theme.json
            final ModelFileManager<Theme> mgr =
                    new ModelFileManager<>(PATH, RESOURCE, Theme.class);
            mgr.open();

            final var list = mgr.loadList("themes");
            mgr.close();

            // If no themes found → fallback to default
            if (list == null || list.isEmpty()) {
                LOADED.clear();
                LOADED.add(Theme.DEFAULT);
            } else {
                LOADED.clear();
                LOADED.addAll(list);
            }

            initialized = true;

        } catch (final Exception e) {
            // On any error → fallback to default theme
            LOADED.clear();
            LOADED.add(Theme.DEFAULT);
            initialized = true;
        }
    }

}
