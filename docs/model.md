# OOP25-UniBodget — Model - UML Class Diagram
GitHub renders the diagram below automatically.

```mermaid
classDiagram
    direction TB

    %% ============================
    %% ======= CATEGORIES =========
    %% ============================

    class BasicCategory {
        <<abstract>>
        -String name
        -ARGBColor color
        -CategoryType type
        +getName()
        +getColor()
        +getType()
    }

    class Category {
        <<final>>
        +Category(String, ARGBColor, CategoryType)
    }

    class CategoryPreset {
        <<enum>>
        +toCategory() Category
    }

    class CategoryType {
        <<enum>>
        INCOME
        EXPENSE
        FRIEND_LOAN
        BANK_LOAN
    }

    %% Relations
    Category --|> BasicCategory
    CategoryPreset --> Category
    BasicCategory --> CategoryType
    BasicCategory --> ARGBColor


    %% ============================
    %% ========= CURRENCY =========
    %% ============================

    class CurrencyUnit {
        <<interface>>
        +getSymbol() String
        +getShortName() String
        +getFullName() String
        +getByCode(String) CurrencyUnit
        +allCurrencies() List<CurrencyUnit>
    }

    class CurrencyType {
        <<enum>>
        FIAT
        CRYPTO
        STOCK
        CUSTOM
    }

    class Currency {
        <<final>>
        -String name
        -CurrencyType type
        +getName()
        +getType()
    }

    class FiatCurrency {
        <<enum>>
    }

    class StockMarketCurrency {
        <<enum>>
    }

    class CryptoCurrency {
        <<enum>>
        -String apiId
        +getApiId()
    }

    class Asset {
        -CurrencyUnit currency
        -BigDecimal amount
        -char sign
        +getCurrency()
        +getAmount()
        +getSign()
    }

    %% Relations
    Currency ..|> CurrencyUnit
    FiatCurrency ..|> CurrencyUnit
    StockMarketCurrency ..|> CurrencyUnit
    CryptoCurrency ..|> CurrencyUnit

    Currency --> CurrencyType
    FiatCurrency --> CurrencyType
    StockMarketCurrency --> CurrencyType
    CryptoCurrency --> CurrencyType

    Asset --> CurrencyUnit


    %% ============================
    %% ========= SETTINGS =========
    %% ============================

    class BudgetLimit {
        -BigDecimal limit
        +getLimit()
    }

    class Settings {
        -Theme theme
        -CurrencyUnit baseCurrency
        -BudgetLimit globalLimit
        +getTheme()
        +getBaseCurrency()
        +getGlobalLimit()
    }

    class Theme {
        <<final>>
        -String name
        -String primaryHex
        +getName()
        +getPrimaryHex()
    }

    class UserData {
        <<final>>
        -String username
        -String email
        -CurrencyUnit baseCurrency
        -Theme theme
        -Map~String, BudgetLimit~ monthlyLimits
        -List~String~ preferenceHistory
        +getUsername()
        +getEmail()
        +getBaseCurrency()
        +getTheme()
        +getMonthlyLimits()
        +getPreferenceHistory()
    }

    %% Relations
    UserData --> Theme
    UserData --> CurrencyUnit
    UserData --> BudgetLimit
    Settings --> Theme
    Settings --> CurrencyUnit
    Settings --> BudgetLimit


    %% ============================
    %% ======== TRANSACTIONS ======
    %% ============================

    class Transaction {
        -Asset asset
        -Category category
        -LocalDate date
        -String description
        -String notes
        +getAsset()
        +getCategory()
        +getDate()
        +getDescription()
        +getNotes()
    }

    class CashTransaction {
        <<final>>
        +CashTransaction(Asset, Category, LocalDate, String, String)
    }

    class InvestmentTransaction {
        -Asset unitPrice
        -Asset fee
        +getUnitPrice()
        +getFee()
    }

    class Historical~T extends Transaction~ {
        -List~T~ history
        +addTransaction(T)
        +getTransactions() List~T~
        +removeTransaction(T) boolean
        +replaceTransaction(T, T) boolean
        +clear()
        +equals(Object)
        +hashCode()
    }

    %% Relations
    CashTransaction --|> Transaction
    InvestmentTransaction --|> Transaction
    Historical --> Transaction

    Transaction --> Asset
    Transaction --> Category
    InvestmentTransaction --> Asset : unitPrice
    InvestmentTransaction --> Asset : fee


    %% ============================
    %% ========= UTILITIES =========
    %% ============================

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
        +toFXColor() Color
    }
