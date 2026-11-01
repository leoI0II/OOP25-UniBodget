classDiagram
direction TB

class DefaultBudgetMonitor {
    -BudgetAlertStrategy strategy
    +void setStrategy(BudgetAlertStrategy strategy)
    +BudgetStatus getBudgetStatus(BigDecimal spent, BudgetSettings settings)
}

class BudgetAlertStrategy {
    <<interface>>
    +BudgetStatus evaluate(BigDecimal spent, BigDecimal limit, double warningThreshold)
}

class AbstractBudgetAlertStrategy {
    <<abstract>>
    #double getThresholdMultiplier()
    +BudgetStatus evaluate(BigDecimal spent, BigDecimal limit, double warningThreshold)
}

class StrictBudgetAlertStrategy {
    #double getThresholdMultiplier()
}

class WarningBudgetAlertStrategy {
    #double getThresholdMultiplier()
}

class LenientBudgetAlertStrategy {
    #double getThresholdMultiplier()
}

class BudgetStatus {
    <<record>>
    +boolean isOverBudget
    +boolean isNearLimit
    +BigDecimal remaining
}

DefaultBudgetMonitor o-- BudgetAlertStrategy : uses
BudgetAlertStrategy <|.. AbstractBudgetAlertStrategy
AbstractBudgetAlertStrategy <|-- StrictBudgetAlertStrategy
AbstractBudgetAlertStrategy <|-- WarningBudgetAlertStrategy
AbstractBudgetAlertStrategy <|-- LenientBudgetAlertStrategy
AbstractBudgetAlertStrategy ..> BudgetStatus : creates
