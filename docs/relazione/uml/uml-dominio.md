# OOP25-UniBodget — Schema UML del Dominio Completo

Questo documento unifica i due diagrammi di dominio presentati nella relazione:
- **Parte 1:** Conti di liquidità, investimenti, storico transazioni, posizioni e gestione budget (cfr. [uml-dominio-conti.md](uml-dominio-conti.md))
- **Parte 2:** Valute multivaluta, mercati, exchange rates, watchlist e banche (cfr. [uml-dominio-valute.md](uml-dominio-valute.md))

```mermaid
classDiagram
direction TB

namespace AccountAndTransactions {
    class Wallet {
        <<abstract>>
        -String id
        -String name
        +getId() String
        +getName() String
        +getBalance() Asset
        +getBaseCurrency() CurrencyUnit
        +getHistory() Historical
    }

    class Historical {
        +getTransactions() List
        +addTransaction(Transaction) void
        +removeTransaction(Transaction) boolean
        +replaceTransaction(Transaction, Transaction) boolean
    }

    class CashAccount {
        +getBudgetSettings() BudgetSettings
        +setBudgetSettings(BudgetSettings) void
    }

    class InvestmentAccount {
        +getPositions() List
        +getTotalProfitLoss() Asset
        +getTotalCostBasis() Asset
        +canSell(CurrencyUnit, Decimal) boolean
    }

    class Transaction {
        <<abstract>>
        -String id
        -Date date
        -String description
        -String notes
        +getId() String
        +getAsset() Asset
        +getDate() Date
    }

    class CashTransaction {
        -String friendName
        -String friendLoanId
        +getFriendName() String
        +isFriendLoanTransaction() boolean
    }

    class InvestmentTransaction {
        +getUnitPrice() Asset
        +getFee() Asset
    }

    class Position {
        -Decimal quantity
        +getAssetUnit() CurrencyUnit
        +getQuantity() Decimal
        +getAveragePrice() Decimal
    }

    class Category {
        -String name
        +getName() String
        +getType() CategoryType
    }

    class CategoryType {
        <<enumeration>>
        INCOME
        EXPENSE
        TRANSFER
        FRIEND_LOAN
    }

    class BudgetSettings {
        -Decimal limitValue
        -Decimal warningThreshold
        +getLimitValue() Decimal
        +getWarningThreshold() Decimal
    }

    class BudgetStatus {
        <<enumeration>>
        SAFE
        WARNING
        CRITICAL
    }
}

namespace CurrenciesAndRates {
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

    class Asset {
        -Decimal amount
        +amount() Decimal
        +currency() CurrencyUnit
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
        +check(Decimal, CurrencyUnit, CurrencyUnit) boolean
    }

    class ExchangeRateAPI {
        <<interface>>
        +getLatestRates(CurrencyUnit) Map
    }

    class Bank {
        -String name
        -Decimal fixedFee
        -Decimal percentageFee
    }

    class CurrencyConversionResult {
        -Decimal amount
        -Decimal convertedAmount
        -Decimal appliedRate
    }
}

Wallet <|-- CashAccount
Wallet <|-- InvestmentAccount
Wallet "1" *-- "1" Historical
Historical "1" o-- "0..*" Transaction

Transaction <|-- CashTransaction
Transaction <|-- InvestmentTransaction
Transaction "0..*" --> "1" Asset

InvestmentAccount "1" *-- "0..*" Position
CashAccount "1" --> "0..1" BudgetSettings
BudgetSettings "1" ..> "1" BudgetStatus

CashTransaction "0..*" --> "1" Category
Category "0..*" --> "1" CategoryType

CurrencyUnit <|.. Currency
CurrencyUnit <|.. FiatCurrency
CurrencyUnit <|.. CryptoCurrency
CurrencyUnit <|.. StockMarketCurrency

Asset ..> CurrencyUnit : expressed in
Position ..> CurrencyUnit : uses
Wallet ..> CurrencyUnit : base currency

WatchList "1" --> "0..*" WatchListPair
CurrencyAlert "0..*" --> "2" CurrencyUnit
ExchangeRateAPI "1" ..> CurrencyConversionResult : calculates
Bank "0..*" --> "1" CurrencyConversionResult : fee
```

