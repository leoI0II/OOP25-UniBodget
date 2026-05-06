package it.unibo.unibodget.model.settings;

public final class ThemeManager {

    private static Theme currentTheme = Theme.DEFAULT;

    private ThemeManager() { }

    public static Theme getTheme() {
        return currentTheme;
    }

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }
}
