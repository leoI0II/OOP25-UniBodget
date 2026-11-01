
classDiagram
direction TB

class CurrencyUnit {
    <<interface>>
    +getCode() String
    +getSymbol() String
    +getShortName() String
    +getFullName() String
    +getType() CurrencyType
}

class Currency {
    -String code
    -String symbol
    -String fullName
    +getType() CurrencyType
    +getSymbol() String
    +getCode() String
}

class FiatCurrency {
    <<enumeration>>
    EUR
    USD
    GBP
    JPY
}

class CryptoCurrency {
    <<enumeration>>
    BTC
    ETH
    SOL
}

class StockMarketCurrency {
    <<enumeration>>
    AAPL
    MSFT
    GOOGL
}

class CurrencyType {
    <<enumeration>>
}

class WatchList {
    +add(WatchListPair) boolean
    +remove(WatchListPair) boolean
}

class WatchListPair {
    -String fromCode
    -String toCode
}

class CurrencyAlert {
    -Decimal threshold
    -boolean isLowerThan
    +check(Decimal, CurrencyUnit, CurrencyUnit) boolean
}

class ExchangeRateAPI {
    <<interface>>
    +getLatestRates(CurrencyUnit) Map
    +getHistoricalRates(CurrencyUnit, CurrencyUnit, Date, Date) Map
}

class ExchangeRate {
    -Decimal rate
    -Date timestamp
    +getRate() Decimal
}

class Bank {
    -String name
    -Decimal fixedFee
    -Decimal percentageFee
    +getName() String
    +getFixedFee() Decimal
    +getPercentageFee() Decimal
}

class CurrencyConversionResult {
    -Decimal amount
    -Decimal convertedAmount
    -Decimal appliedRate
    +getAmount() Decimal
    +getConvertedAmount() Decimal
    +getAppliedRate() Decimal
}

CurrencyUnit <|.. Currency : implements
CurrencyUnit <|.. FiatCurrency : implements
CurrencyUnit <|.. CryptoCurrency : implements
CurrencyUnit <|.. StockMarketCurrency : implements

CurrencyUnit ..> CurrencyType : identifies type

WatchList "1" --> "0..*" WatchListPair : tracks

CurrencyAlert "0..*" --> "2" CurrencyUnit : monitors pair

ExchangeRateAPI "1" ..> "0..*" ExchangeRate : provides
ExchangeRate "0..*" --> "2" CurrencyUnit : refers to

CurrencyConversionResult "0..*" --> "2" CurrencyUnit : converts between

Bank "0..*" --> "1" CurrencyConversionResult : applies fee on
