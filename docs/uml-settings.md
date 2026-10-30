
classDiagram
direction TB

class SettingsManager {
    <<Caretaker>>
    -settings: Settings
    -history: List~SettingsSnapshot~
    +getSettings() Settings
    +saveSettings(settings: Settings) void
    +getHistory() List~SettingsSnapshot~
    +restoreSnapshot(snapshot: SettingsSnapshot) void
}

class Settings {
    <<Originator>>
    -theme: Theme
    -baseCurrency: CurrencyUnit
    -windowPreferences: WindowPreferences
    +createSnapshot() SettingsSnapshot
    +restoreFromSnapshot(snapshot: SettingsSnapshot) void
    +getTheme() Theme
    +setTheme(theme: Theme) void
}

class SettingsSnapshot {
    <<record>>
    <<Memento>>
    -timestamp: Instant
    -theme: Theme
    -baseCurrency: CurrencyUnit
    +timestamp() Instant
    +theme() Theme
    +baseCurrency() CurrencyUnit
}

class ThemeManager {
    <<Observer / CSS Registry>>
    -currentTheme: Theme
    -registeredScenes: List~Scene~
    +applyThemeToScene(scene: Scene) void
    +registerScene(scene: Scene) void
    +getCurrentTheme() Theme
    +setTheme(theme: Theme) void
}

SettingsManager --> Settings : persists
SettingsManager --> SettingsSnapshot : stores history
Settings --> SettingsSnapshot : creates / restores
Settings ..> ThemeManager : notifies
