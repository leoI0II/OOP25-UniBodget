classDiagram
    class CurrencyUnit {
      <<interface>>
      +getSymbol() String
      +getShortName() String
      +getFullName() String
    }
    class FiatCurrency{
        <<enum>>
        USD, EUR, RUB, CHF, ...
        -symbol : String
        -shortName : String
        -fullName : String
    }
    class StockMarketCurrency{
        <<enum>>
        AAPL, MSFT, NVDA, AMZN, GOOGL, ...
        -symbol : String
        -shortName : String
        -fullName : String
    }
    class CryptoCurrency{
        <<enum>>
        BTC, ETH, USDT, BNB, SOL, ...
        -symbol : String
        -shortName : String
        -fullName : String
        
        +getApiId() String
    }
    FiatCurrency ..|> CurrencyUnit : implements
    CryptoCurrency ..|> CurrencyUnit : implements
    StockMarketCurrency ..|> CurrencyUnit : implements

    class Asset{
        -currencyUnit : CurrencyUnit
        -amount : BigDecimal

        +Asset(CurrencyUnit, BigDecimal)
        +getCurrencyUnit() CurrencyUnit
        +getAmount() BigDecimal
        +toString() String
    }
    Asset --> CurrencyUnit : uses

    class Transaction {
        <<abstract sealed>>
        -asset : final Asset
        -date : final LocalDateTime
        -category : final Category
        -description : final String
        -notes : final String

        # Transaction(...)
        + getAsset() Asset
        + getDate() LocalDateTime 
        + getDescription() String
        + getNotes() String
        + getCategory() Category
    }

    class CashTransaction {
        <<final>>
        + CashTransaction(...) // fa super(...)
        // niente campi aggiuntivi, eredita tutto
    }
    CashTransaction --|> Transaction : use

    class InvestmentTransaction {
        <<final>>
        -unitPrice : final Asset  //prezzo storico x unit
        -fee : final Asset        // bonus fee opzionale

        +getUnitPrice() Asset
        +getFee() Asset
    }
    InvestmentTransaction --|> Transaction : uses

    class ARGBColor {
        <<record>>

        +TRANSPARENT : static ARGBColor
        +BLACK : static ARGBColor
        +WHITE : static ARGBColor
        +RED : static ARGBColor
        +GREEN : static ARGBColor
        +BLUE : static ARGBColor
        +YELLOW : static ARGBColor
        +CYAN : static ARGBColor
        +MAGENTA : static ARGBColor
        +GRAY : static ARGBColor
        +DARK_GREY : static ARGBColor
        +LIGHT_GREY : static ARGBColor

        -MIN : static int
        -MAX : static int
        
        -alpha : int
        -red : int
        -green : int
        -blue : int
        
        -checkValue(int, int) void
        
        +ARGBColor(int, int, int)
        +ARGBColor(int, int, int, int)
        +ARGBColor(int)
        +ARGBColor(hex : String)
        +parseHexToInt(hex : String) int
        +toHexString() String
    }

    class Historical~T extends Transaction~ {
        -transactionsHistory : List~T~
        +getTransactions() List~T~
        +addTransaction(T) void
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
        +Wallet(CurrencyUnit, Historical)
        +getName() String
        +getBaseCurrency() CurrencyUnit
        +addTransaction(T) void
        +getHistory() Historical~T~
        +getBalance() Asset *abstract // conteggio dinamico
    }
    Wallet --> Historical : uses
    Wallet --> CurrencyUnit : uses

    class CashAccount {
        <<extends Wallet~CashTransaction> >>
        +getBalance() Asset // foreache lista e torna balance
    }
    CashAccount ..|> Wallet : implement

    class InvestmentAccount{
        <<extends Wallet~InvestmentTransaction> >>
        +getBalance() Asset // foreach lista, convert e return
    }
    InvestmentAccount ..|> Wallet : implement

    class CategoryType {
        <<enum>>
        INCOME,
        EXPENSE,
        TRANSFER,
        FRIEND_LOAN,
        BANK_LOAN;
    }

    class BasicCategory {
        <<abstract>>
        - name : String
        - color : ARGBColor
        - type : CategoryType

        + BasicCategory(String, ARGBColor, CategoryType)
        + getName() String
        + getColor() ARGBColor
        + getType() CategoryType
    }
    BasicCategory --|> ARGBColor    : uses
    BasicCategory --|> CategoryType : uses

    class Category {
        - final Category FOOD$
        - final Category RENT$
        - final Category SAVINGS$
        - final Category TRANSPORT$
        - final Category TRANSFER$
        + getDefaultCategories()$ List~Category~
    }