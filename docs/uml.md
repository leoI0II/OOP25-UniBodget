classDiagram
    direction TB

    %% ============================================================
    %% =================== CURRENCY DOMAIN =============
    %% ============================================================

    direction TB

    %% ============================================================
    %% =================== CURRENCY DOMAIN =======================
    %% ============================================================

    class CurrencyUnit {
        <<interface>>
        +getType() CurrencyType
        +getSymbol() String
        +getShortName() String
        +getFullName() String
        +getCode() String
        +getDisplayDecimals() int
        +getByCode(String)$ CurrencyUnit
        +allCurrencies()$ List~CurrencyUnit~
        +allCurrenciesObservable()$ ObservableList~CurrencyUnit~
    }

    class CurrencyType {
        <<enum>>
        FIAT
        CRYPTO
        STOCK
        CUSTOM
    }

    class FiatCurrency {
        <<enum>>
        USD, EUR, GBP, JPY, CHF, CAD, AUD, CNY, ...
        -symbol : String
        -shortName : String
        -fullName : String
        -code : String
        -displayDecimals : int
        +getType() CurrencyType
        +getSymbol() String
        +getShortName() String
        +getFullName() String
        +getCode() String
        +getDisplayDecimals() int
    }

    class CryptoCurrency {
        <<enum>>
        BTC, ETH, USDT, BNB, SOL, XRP, DOGE, ADA, ...
        -symbol : String
        -shortName : String
        -fullName : String
        -code : String
        -apiId : String
        +getType() CurrencyType
        +getSymbol() String
        +getShortName() String
        +getFullName() String
        +getCode() String
        +getApiId() String
    }

    class StockMarketCurrency {
        <<enum>>
        AAPL, MSFT, NVDA, AMZN, GOOGL, META, TSLA, ...
        -symbol : String
        -shortName : String
        -fullName : String
        -code : String
        +getType() CurrencyType
        +getSymbol() String
        +getShortName() String
        +getFullName() String
        +getCode() String
    }

    class Currency {
        -code : String
        -name : String
        -symbol : String
        -type : CurrencyType
        -displayDecimals : int
        +Currency(String, String, String, CurrencyType, int)
        +getType() CurrencyType
        +getSymbol() String
        +getShortName() String
        +getFullName() String
        +getCode() String
        +getDisplayDecimals() int
        +saveToFile(Path)$ void
        +loadFromFile(Path)$ List~Currency~
    }

    FiatCurrency ..|> CurrencyUnit : implements
    CryptoCurrency ..|> CurrencyUnit : implements
    StockMarketCurrency ..|> CurrencyUnit : implements
    Currency ..|> CurrencyUnit : implements

    CurrencyUnit --> CurrencyType : classifies

    class Asset {
        -currencyUnit : CurrencyUnit
        -amount : BigDecimal
        +Asset(CurrencyUnit, BigDecimal)
        +getCurrencyUnit() CurrencyUnit
        +getAmount() BigDecimal
        +of(CurrencyUnit, BigDecimal)$ Asset
        +zero(CurrencyUnit)$ Asset
        +plus(Asset) Asset
        +minus(Asset) Asset
        +multiply(BigDecimal) Asset
        +isPositive() boolean
        +isNegative() boolean
        +isZero() boolean
        +toString() String
    }
    Asset --> CurrencyUnit : uses

    %% ============================================================
    %% ================= CONVERSION ENGINE ==============
    %% ============================================================

    class CurrencyConverter {
        <<interface>>
        +convert(BigDecimal, CurrencyUnit, CurrencyUnit) CurrencyConversionResult
        +convert(Asset, CurrencyUnit) CurrencyConversionResult
    }

    class BasicCurrencyConverter {
        -priceService : UniversalPriceService
        -baseReferenceCurrency : CurrencyUnit
        +BasicCurrencyConverter(UniversalPriceService, CurrencyUnit)
        +convert(BigDecimal, CurrencyUnit, CurrencyUnit) CurrencyConversionResult
        +convert(Asset, CurrencyUnit) CurrencyConversionResult
    }

    class CurrencyConversionResult {
        <<record>>
        -fromCurrency : CurrencyUnit
        -toCurrency : CurrencyUnit
        -sourceAmount : BigDecimal
        -convertedAmount : BigDecimal
        -appliedRate : BigDecimal
        -timestamp : Instant
        +fromCurrency() CurrencyUnit
        +toCurrency() CurrencyUnit
        +sourceAmount() BigDecimal
        +convertedAmount() BigDecimal
        +appliedRate() BigDecimal
        +timestamp() Instant
    }

    BasicCurrencyConverter ..|> CurrencyConverter : implements
    BasicCurrencyConverter --> UniversalPriceService : delegates
    CurrencyConverter --> CurrencyConversionResult : produces
    CurrencyConverter --> CurrencyUnit : uses

    class MonetaryInputParser {
        <<utility>>
        +sanitize(String)$ String
        +parse(String)$ Optional~BigDecimal~
        +parsePositive(String)$ Optional~BigDecimal~
        +parseNonNegative(String)$ Optional~BigDecimal~
        +parsePositiveWithMaxScale(String, int)$ Optional~BigDecimal~
        +parseDouble(String)$ OptionalDouble
        +parsePositiveDouble(String)$ OptionalDouble
        +parseNonNegativeDouble(String)$ OptionalDouble
    }

    %% ============================================================
    %% ================= BANK CONVERSION ================
    %% ============================================================

    class Bank {
        <<record>>
        -name : String
        -fixedFee : double
        -percentageSpread : double
        -notes : String
        +name() String
        +fixedFee() double
        +percentageSpread() double
        +notes() String
    }

    class BankCalculator {
        <<utility>>
        +calculateConversion(BigDecimal, double, double)$ BankConversionResult
    }

    class BankConversionResult {
        <<record>>
        -baseAmount : BigDecimal
        -netAmount : BigDecimal
        -spreadCost : BigDecimal
        -fixedFeeCost : BigDecimal
        -totalCost : BigDecimal
        -effectiveRate : BigDecimal
    }

    class BankConversionService {
        +convert(Bank, BigDecimal, CurrencyUnit, CurrencyUnit, CurrencyConverter) BankConversionResult
    }

    class BankList {
        -banks : List~Bank~
        +getBanks()$ List~Bank~
        +saveBanks(List~Bank~)$ void
        +generateMockBanks()$ List~Bank~
    }

    BankConversionService --> BankCalculator : uses
    BankConversionService --> CurrencyConverter : uses
    BankConversionService --> Bank : configures
    BankConversionService --> BankConversionResult : returns
    BankList --> Bank : stores

    %% ============================================================
    %% ================= API & CACHING ==================
    %% ============================================================

    %% ============================================================
    %% ================= CONVERSION ENGINE ========================
    %% ============================================================

    class CurrencyConverter {
        <<interface>>
        +convert(BigDecimal, CurrencyUnit, CurrencyUnit) CurrencyConversionResult
        +convert(Asset, CurrencyUnit) CurrencyConversionResult
    }

    class BasicCurrencyConverter {
        -priceService : UniversalPriceService
        -baseReferenceCurrency : CurrencyUnit
        +BasicCurrencyConverter(UniversalPriceService, CurrencyUnit)
        +convert(BigDecimal, CurrencyUnit, CurrencyUnit) CurrencyConversionResult
        +convert(Asset, CurrencyUnit) CurrencyConversionResult
    }

    class CurrencyConversionResult {
        <<record>>
        -fromCurrency : CurrencyUnit
        -toCurrency : CurrencyUnit
        -sourceAmount : BigDecimal
        -convertedAmount : BigDecimal
        -appliedRate : BigDecimal
        -timestamp : Instant
        +fromCurrency() CurrencyUnit
        +toCurrency() CurrencyUnit
        +sourceAmount() BigDecimal
        +convertedAmount() BigDecimal
        +appliedRate() BigDecimal
        +timestamp() Instant
    }

    BasicCurrencyConverter ..|> CurrencyConverter : implements
    BasicCurrencyConverter --> UniversalPriceService : delegates
    CurrencyConverter --> CurrencyConversionResult : produces
    CurrencyConverter --> CurrencyUnit : uses

    class MonetaryInputParser {
        <<utility>>
        +sanitize(String)$ String
        +parse(String)$ Optional~BigDecimal~
        +parsePositive(String)$ Optional~BigDecimal~
        +parseNonNegative(String)$ Optional~BigDecimal~
        +parsePositiveWithMaxScale(String, int)$ Optional~BigDecimal~
        +parseDouble(String)$ OptionalDouble
        +parsePositiveDouble(String)$ OptionalDouble
        +parseNonNegativeDouble(String)$ OptionalDouble
    }

    %% ============================================================
    %% ================= BANK CONVERSION ==========================
    %% ============================================================

    class Bank {
        <<record>>
        -name : String
        -fixedFee : double
        -percentageSpread : double
        -notes : String
        +name() String
        +fixedFee() double
        +percentageSpread() double
        +notes() String
    }

    class BankCalculator {
        <<utility>>
        +calculateConversion(BigDecimal, double, double)$ BankConversionResult
    }

    class BankConversionResult {
        <<record>>
        -baseAmount : BigDecimal
        -netAmount : BigDecimal
        -spreadCost : BigDecimal
        -fixedFeeCost : BigDecimal
        -totalCost : BigDecimal
        -effectiveRate : BigDecimal
    }

    class BankConversionService {
        +convert(Bank, BigDecimal, CurrencyUnit, CurrencyUnit, CurrencyConverter) BankConversionResult
    }

    class BankList {
        -banks : List~Bank~
        +getBanks()$ List~Bank~
        +saveBanks(List~Bank~)$ void
        +generateMockBanks()$ List~Bank~
    }

    BankConversionService --> BankCalculator : uses
    BankConversionService --> CurrencyConverter : uses
    BankConversionService --> Bank : configures
    BankConversionService --> BankConversionResult : returns
    BankList --> Bank : stores

    %% ============================================================
    %% ================= API & CACHING ============================
    %% ============================================================

    class ExchangeRateAPI {
        <<interface>>
        +getLatestRates(CurrencyUnit) Map~CurrencyUnit, Double~
        +getHistoricalRates(CurrencyUnit, CurrencyUnit, LocalDate, LocalDate) Map~LocalDate, Double~
    }

    class ExchangeRateAPIImpl {
        -CACHE_DURATION : Duration
        -client : HttpClient
        -lastUpdate : Instant
        -cachedRates : Map~CurrencyUnit, Double~
        +getLatestRates(CurrencyUnit) Map~CurrencyUnit, Double~
        +getHistoricalRates(CurrencyUnit, CurrencyUnit, LocalDate, LocalDate) Map~LocalDate, Double~
        -fetchRatesFromAPI(CurrencyUnit) Map~CurrencyUnit, Double~
        -parseRates(String, CurrencyUnit) Map~CurrencyUnit, Double~
    }

    class MockExchangeRateAPI {
        -mockRates : Map~CurrencyUnit, Double~
        +getLatestRates(CurrencyUnit) Map~CurrencyUnit, Double~
        +getHistoricalRates(CurrencyUnit, CurrencyUnit, LocalDate, LocalDate) Map~LocalDate, Double~
    }

    class UniversalPriceService {
        -fiatApi : ExchangeRateAPI
        -cryptoProvider : PriceProvider
        -stockProvider : PriceProvider
        +getPrice(CurrencyUnit, CurrencyUnit) double
        +getHistorical(CurrencyUnit, CurrencyUnit, LocalDate, LocalDate) Map~LocalDate, Double~
    }

    class PriceProvider {
        <<interface>>
        +getPrice(String, String) double
    }

    class AlphaPriceProvider {
        +getPrice(String, String) double
    }

    class CoinGeckoPriceProvider {
        +getPrice(String, String) double
    }

    ExchangeRateAPIImpl ..|> ExchangeRateAPI : implements
    MockExchangeRateAPI ..|> ExchangeRateAPI : implements
    AlphaPriceProvider ..|> PriceProvider : implements
    CoinGeckoPriceProvider ..|> PriceProvider : implements
    UniversalPriceService --> ExchangeRateAPI : delegates fiat
    UniversalPriceService --> PriceProvider : delegates crypto/stock

    %% ============================================================
    %% ============= WATCHLIST & ALERTS =================
    %% ============================================================

    class WatchList {
        -pairs : Set~WatchlistPair~
        +addPair(CurrencyUnit, CurrencyUnit) boolean
        +removePair(WatchlistPair) boolean
        +getPairs() Set~WatchlistPair~
        +load()$ WatchList
        +save() void
    }

    class WatchlistPair {
        <<record>>
        -base : CurrencyUnit
        -target : CurrencyUnit
        +base() CurrencyUnit
        +target() CurrencyUnit
    }

    class CurrencyAlert {
        <<record>>
        -baseCurrency : CurrencyUnit
        -targetCurrency : CurrencyUnit
        -threshold : double
        -direction : AlertDirection
    }

    class CurrencyAlertService {
        -alerts : List~CurrencyAlert~
        +addAlert(CurrencyAlert) void
        +removeAlert(CurrencyAlert) void
        +getAlerts() List~CurrencyAlert~
        +checkAlerts(double, CurrencyUnit, CurrencyUnit) List~CurrencyAlert~
    }

    class MarketAlert {
        <<utility>>
        +showAlert(CurrencyAlert, double)$ void
    }

    WatchList --> WatchlistPair : stores
    CurrencyAlertService --> CurrencyAlert : monitors
    MarketAlert --> CurrencyAlert : notifies

    %% ============================================================
    %% ============= SETTINGS & THEMES =================
    %% ============================================================

    class Settings {
        -theme : Theme
        -baseCurrency : CurrencyUnit
        -globalLimit : BudgetLimit
        -windowPreferences : WindowPreferences
        +getTheme() Theme
        +setTheme(Theme) void
        +getBaseCurrency() CurrencyUnit
        +setBaseCurrency(CurrencyUnit) void
        +getWindowPreferences() WindowPreferences
        +createSnapshot() SettingsSnapshot
        +restoreFromSnapshot(SettingsSnapshot) void
    }

    class SettingsSnapshot {
        <<record>>
        -timestamp : Instant
        -theme : Theme
        -baseCurrency : CurrencyUnit
        +timestamp() Instant
        +theme() Theme
        +baseCurrency() CurrencyUnit
    }

    class SettingsManager {
        -settings : Settings
        -history : List~SettingsSnapshot~
        +getSettings() Settings
        +saveSettings(Settings) void
        +getHistory() List~SettingsSnapshot~
        +restoreSnapshot(SettingsSnapshot) void
    }

    class Theme {
        -name : String
        -primaryHex : String
        -fontFamily : String
        -isDark : boolean
        +getName() String
        +getPrimaryHex() String
        +getFontFamily() String
        +isDark() boolean
    }

    class ThemeManager {
        -currentTheme : Theme
        -registeredScenes : List~Scene~
        +applyThemeToScene(Scene)$ void
        +registerScene(Scene)$ void
        +getCurrentTheme()$ Theme
        +setTheme(Theme)$ void
    }

    class WindowPreferences {
        <<record>>
        -width : double
        -height : double
        -x : double
        -y : double
        -maximized : boolean
    }

    SettingsManager --> Settings : persists (Caretaker)
    Settings --> SettingsSnapshot : creates/restores (Originator)
    Settings --> Theme : configures
    Settings --> WindowPreferences : holds
    ThemeManager --> Theme : applies (Observer)

    %% ============================================================
    %% ============= PERSISTENCE RESOLVER ==============
    %% ============================================================

    class AppDataPathResolver {
        <<utility>>
        -DATA_DIR_NAME : String$
        -MAX_SEARCH_DEPTH : int$
        -MAX_VISITED_DIRECTORIES : int$
        -cachedDataDir : Path$
        +getDataDir()$ Path
        +resolve(String)$ Path
        +resolve(Path)$ Path
        +resolveJson(String)$ Path
        +resolveCsv(String)$ Path
        +isValidUniBodgetDataDir(Path)$ boolean
        +setCustomDataDir(Path)$ void
        +reset()$ void
    }

    class ModelFileManager {
        <<utility>>
        +ensureFileExists(Path, String)$ void
    }

    SettingsManager ..> AppDataPathResolver : resolves paths
    BankList ..> AppDataPathResolver : resolves paths
    WatchList ..> AppDataPathResolver : resolves paths
    Currency ..> AppDataPathResolver : resolves paths
    AppDataPathResolver ..> ModelFileManager : self-healing templates

    %% ============================================================
    %% ================= TRANSACTION & WALLET DOMAIN ==============
    %% ============================================================

    ExchangeRateAPIImpl ..|> ExchangeRateAPI : implements
    MockExchangeRateAPI ..|> ExchangeRateAPI : implements
    AlphaPriceProvider ..|> PriceProvider : implements
    CoinGeckoPriceProvider ..|> PriceProvider : implements
    UniversalPriceService --> ExchangeRateAPI : delegates fiat
    UniversalPriceService --> PriceProvider : delegates crypto/stock

    %% ============================================================
    %% ============= WATCHLIST & ALERTS ===========================
    %% ============================================================

    class WatchList {
        -pairs : Set~WatchlistPair~
        +addPair(CurrencyUnit, CurrencyUnit) boolean
        +removePair(WatchlistPair) boolean
        +getPairs() Set~WatchlistPair~
        +load()$ WatchList
        +save() void
    }

    class WatchlistPair {
        <<record>>
        -base : CurrencyUnit
        -target : CurrencyUnit
        +base() CurrencyUnit
        +target() CurrencyUnit
    }

    class CurrencyAlert {
        <<record>>
        -baseCurrency : CurrencyUnit
        -targetCurrency : CurrencyUnit
        -threshold : double
        -direction : AlertDirection
    }

    class CurrencyAlertService {
        -alerts : List~CurrencyAlert~
        +addAlert(CurrencyAlert) void
        +removeAlert(CurrencyAlert) void
        +getAlerts() List~CurrencyAlert~
        +checkAlerts(double, CurrencyUnit, CurrencyUnit) List~CurrencyAlert~
    }

    class MarketAlert {
        <<utility>>
        +showAlert(CurrencyAlert, double)$ void
    }

    WatchList --> WatchlistPair : stores
    CurrencyAlertService --> CurrencyAlert : monitors
    MarketAlert --> CurrencyAlert : notifies

    %% ============================================================
    %% ============= SETTINGS & THEMES ===========================
    %% ============================================================

    class Settings {
        -theme : Theme
        -baseCurrency : CurrencyUnit
        -globalLimit : BudgetLimit
        -windowPreferences : WindowPreferences
        +getTheme() Theme
        +setTheme(Theme) void
        +getBaseCurrency() CurrencyUnit
        +setBaseCurrency(CurrencyUnit) void
        +getWindowPreferences() WindowPreferences
        +createSnapshot() SettingsSnapshot
        +restoreFromSnapshot(SettingsSnapshot) void
    }

    class SettingsSnapshot {
        <<record>>
        -timestamp : Instant
        -theme : Theme
        -baseCurrency : CurrencyUnit
        +timestamp() Instant
        +theme() Theme
        +baseCurrency() CurrencyUnit
    }

    class SettingsManager {
        -settings : Settings
        -history : List~SettingsSnapshot~
        +getSettings() Settings
        +saveSettings(Settings) void
        +getHistory() List~SettingsSnapshot~
        +restoreSnapshot(SettingsSnapshot) void
    }

    class Theme {
        -name : String
        -primaryHex : String
        -fontFamily : String
        -isDark : boolean
        +getName() String
        +getPrimaryHex() String
        +getFontFamily() String
        +isDark() boolean
    }

    class ThemeManager {
        -currentTheme : Theme
        -registeredScenes : List~Scene~
        +applyThemeToScene(Scene)$ void
        +registerScene(Scene)$ void
        +getCurrentTheme()$ Theme
        +setTheme(Theme)$ void
    }

    class WindowPreferences {
        <<record>>
        -width : double
        -height : double
        -x : double
        -y : double
        -maximized : boolean
    }

    SettingsManager --> Settings : persists (Caretaker)
    Settings --> SettingsSnapshot : creates/restores (Originator)
    Settings --> Theme : configures
    Settings --> WindowPreferences : holds
    ThemeManager --> Theme : applies (Observer)

    %% ============================================================
    %% ============= PERSISTENCE RESOLVER ========================
    %% ============================================================

    class AppDataPathResolver {
        <<utility>>
        -DATA_DIR_NAME : String$
        -MAX_SEARCH_DEPTH : int$
        -MAX_VISITED_DIRECTORIES : int$
        -cachedDataDir : Path$
        +getDataDir()$ Path
        +resolve(String)$ Path
        +resolve(Path)$ Path
        +resolveJson(String)$ Path
        +resolveCsv(String)$ Path
        +isValidUniBodgetDataDir(Path)$ boolean
        +setCustomDataDir(Path)$ void
        +reset()$ void
    }

    class ModelFileManager {
        <<utility>>
        +ensureFileExists(Path, String)$ void
    }

    SettingsManager ..> AppDataPathResolver : resolves paths
    BankList ..> AppDataPathResolver : resolves paths
    WatchList ..> AppDataPathResolver : resolves paths
    Currency ..> AppDataPathResolver : resolves paths
    AppDataPathResolver ..> ModelFileManager : self-healing templates

    %% ============================================================
    %% ================= TRANSACTION & WALLET DOMAIN ==============
    %% ============================================================

    class Transaction {
        <<abstract sealed>>
        -asset : final Asset
        -date : final LocalDateTime
        -category : final Category
        -description : final String
        -notes : final String
        #Transaction(...)
        +getAsset() Asset
        +getDate() LocalDateTime
        +getDescription() String
        +getNotes() String
        +getCategory() Category
    }

    class CashTransaction {
        <<final>>
        +CashTransaction(...)
    }
    CashTransaction --|> Transaction : extends

    class InvestmentTransaction {
        <<final>>
        -unitPrice : final Asset
        -fee : final Asset
        +getUnitPrice() Asset
        +getFee() Asset
    }
    InvestmentTransaction --|> Transaction : extends

    class ARGBColor {
        <<record>>
        +TRANSPARENT : static ARGBColor
        +BLACK : static ARGBColor
        +WHITE : static ARGBColor
        +RED : static ARGBColor
        +GREEN : static ARGBColor
        +BLUE : static ARGBColor
        -alpha : int
        -red : int
        -green : int
        -blue : int
        +toHexString() String
        +toFXColor() Color
    }

    class Historical~T extends Transaction~ {
        -transactionsHistory : List~T~
        +getTransactions() List~T~
        +addTransaction(T) void
        +removeTransaction(T) boolean
    }
    Historical --> Transaction : uses

    class Wallet~T extends Transaction~ {
        <<abstract class>>
        -name : String
        -history : Historical~T~
        -baseCurrency : final CurrencyUnit
        -id : int
        -count : int static
        +Wallet(CurrencyUnit)
        +getName() String
        +getBaseCurrency() CurrencyUnit
        +addTransaction(T) void
        +getHistory() Historical~T~
        +getBalance()* Asset
    }
    Wallet --> Historical : uses
    Wallet --> CurrencyUnit : uses

    class CashAccount {
        +getBalance() Asset
    }
    CashAccount --|> Wallet : extends

    class InvestmentAccount {
        +getBalance() Asset
    }
    InvestmentAccount --|> Wallet : extends

    class CategoryType {
        <<enum>>
        INCOME
        EXPENSE
        TRANSFER
        FRIEND_LOAN
    }

    class AbstractCategory {
        <<abstract>>
        -name : String
        -color : ARGBColor
        -type : CategoryType
        +getName() String
        +getColor() ARGBColor
        +getType() CategoryType
    }
    AbstractCategory --> ARGBColor : uses
    AbstractCategory --> CategoryType : uses

    class Category {
        -final Category FOOD$
        -final Category RENT$
        -final Category SAVINGS$
        -final Category TRANSPORT$
        -final Category TRANSFER$
        +getDefaultCategories()$ List~Category~
    }
    Category --|> AbstractCategory : extends

    Category --|> AbstractCategory : extends
    }
