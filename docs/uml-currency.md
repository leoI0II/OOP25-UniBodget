
classDiagram
direction TB

class CurrencyUnit {
    <<interface>>
    +getType() CurrencyType
    +getSymbol() String
    +getShortName() String
    +getFullName() String
    +getCode() String
    +getByCode(code: String) CurrencyUnit
    +allCurrencies() List~CurrencyUnit~
}

class FiatCurrency {
    <<enumeration>>
    USD
    EUR
    GBP
    ...
    -symbol: String
    -shortName: String
    +getSymbol() String
    +getCode() String
}

class StockMarketCurrency {
    <<enumeration>>
    AAPL
    MSFT
    ...
    -symbol: String
    -shortName: String
    +getShortName() String
    +getCode() String
}

class CryptoCurrency {
    <<enumeration>>
    BTC
    ETH
    ...
    -symbol: String
    -apiId: String
    +getApiId() String
    +getCode() String
}

class Currency {
    -code: String
    -name: String
    -symbol: String
    +saveToFile(path: Path) void
    +loadFromFile(path: Path) List
}

class CurrencyConverter {
    <<interface>>
    +convert(amount, from, to) CurrencyConversionResult
    +convert(asset, target) CurrencyConversionResult
}

class BasicCurrencyConverter {
    -priceService: UniversalPriceService
    -baseRef: CurrencyUnit
    +convert(amount, from, to) CurrencyConversionResult
}

class CurrencyConversionResult {
    <<record>>
    -fromCurrency: CurrencyUnit
    -toCurrency: CurrencyUnit
    -sourceAmount: BigDecimal
    -convertedAmount: BigDecimal
    -appliedRate: BigDecimal
    -timestamp: Instant
    +fromCurrency() CurrencyUnit
    +convertedAmount() BigDecimal
}

class Asset {
    -currencyUnit: CurrencyUnit
    -amount: BigDecimal
    +of(unit, amount) Asset
    +plus(asset: Asset) Asset
    +multiply(value: BigDecimal) Asset
}

CurrencyUnit <|.. FiatCurrency
CurrencyUnit <|.. StockMarketCurrency
CurrencyUnit <|.. CryptoCurrency
CurrencyUnit <|.. Currency

CurrencyConverter <|.. BasicCurrencyConverter

CurrencyConverter --> CurrencyConversionResult : returns
BasicCurrencyConverter --> Asset : uses
Asset --> CurrencyUnit : holds
