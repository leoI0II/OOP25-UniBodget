
classDiagram
direction TB

class MarketAlertView {
    <<View Component>>
    +checkAndShowAlerts(rate: double, from, to) void
}

class CurrencyAlertService {
    -alerts: List~CurrencyAlert~
    +addAlert(alert: CurrencyAlert) void
    +removeAlert(alert: CurrencyAlert) void
    +getAlerts() List~CurrencyAlert~
    +checkAlerts(rate: double, from, to) List~CurrencyAlert~
}

class CurrencyAlert {
    <<record>>
    -baseCurrency: CurrencyUnit
    -targetCurrency: CurrencyUnit
    -threshold: double
    -direction: AlertDirection
    +baseCurrency() CurrencyUnit
    +targetCurrency() CurrencyUnit
    +threshold() double
    +direction() AlertDirection
}

class WatchList {
    -pairs: Set~WatchlistPair~
    -watchlistPath: Path
    +addPair(base: CurrencyUnit, target: CurrencyUnit) boolean
    +removePair(pair: WatchlistPair) boolean
    +getPairs() Set~WatchlistPair~
    +load() WatchList
    +save() void
}

class WatchlistPair {
    <<record>>
    -base: CurrencyUnit
    -target: CurrencyUnit
    +base() CurrencyUnit
    +target() CurrencyUnit
}

MarketAlertView ..> CurrencyAlertService : presents
CurrencyAlertService --> CurrencyAlert : manages
WatchList --> WatchlistPair : contains
