classDiagram
direction TB

class SettingsManager {
  -Path settingsFilePath
  +Settings getSettings()
  +void saveSettings(...)
}
class AppDataPathResolver {
  <<Single Source of Truth>>
  -String DATA_DIR_NAME = "data"
  -int MAX_SEARCH_DEPTH = 3
  -int MAX_VISITED_DIRECTORIES = 500
  -Path cachedDataDir
  +Path getDataDir()
  +Path resolve(String relativePath)
  +Path resolveJson(String subpath)
  +Path resolveCsv(String subpath)
  +boolean isValidUniBodgetDataDir(Path candidate)
  +void setCustomDataDir(Path path)
  +void reset()
}
class ThemeList {
  -Path themesFilePath
  +List~Theme~ getThemes()
}
class BankList {
  -Path bankFilePath
  +List~Bank~ getBanks()
}
class WatchList {
  -Path watchlistPath
  +WatchList load()
}
class ModelFileManager {
  <<Self-Healing>>
  +void ensureFileExists(Path target, String resourceTemplate)
}

SettingsManager ..> AppDataPathResolver : resolveJson
ThemeList ..> AppDataPathResolver : resolveJson
BankList ..> AppDataPathResolver : resolveJson
WatchList ..> AppDataPathResolver : resolveJson
AppDataPathResolver ..> ModelFileManager : fallback templates