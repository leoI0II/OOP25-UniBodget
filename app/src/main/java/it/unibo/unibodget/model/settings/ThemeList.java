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
    private static final List<Theme> loaded = new ArrayList<>();

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
        return Collections.unmodifiableList(loaded);
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
            ModelFileManager<Theme> mgr =
                    new ModelFileManager<>(PATH, RESOURCE, Theme.class);
            mgr.open();

            var list = mgr.loadList("themes");
            mgr.close();

            // If no themes found → fallback to default
            if (list == null || list.isEmpty()) {
                loaded.clear();
                loaded.add(Theme.DEFAULT);
            } else {
                loaded.clear();
                loaded.addAll(list);
            }

            initialized = true;

        } catch (Exception e) {
            // On any error → fallback to default theme
            loaded.clear();
            loaded.add(Theme.DEFAULT);
            initialized = true;
        }
    }
    
}
