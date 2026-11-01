classDiagram
direction TB

class DefaultMonthlyInsightService {
    +List~WalletInsight~ getMonthlyInsights(YearMonth month)
}

class AbstractMonthlyInsightTemplate {
    <<abstract>>
    +WalletInsight generateInsight(YearMonth month)
    #BigDecimal calculateValue(YearMonth month)
    #String buildDescription(BigDecimal value)
}

class SpendingInsightTemplate {
    #BigDecimal calculateValue(YearMonth month)
    #String buildDescription(BigDecimal value)
}

class IncomeInsightTemplate {
    #BigDecimal calculateValue(YearMonth month)
    #String buildDescription(BigDecimal value)
}

class SavingsInsightTemplate {
    #BigDecimal calculateValue(YearMonth month)
    #String buildDescription(BigDecimal value)
}

class WalletInsight {
    <<record>>
    +String title
    +String description
    +BigDecimal value
}

DefaultMonthlyInsightService ..> AbstractMonthlyInsightTemplate : invokes
AbstractMonthlyInsightTemplate <|-- SpendingInsightTemplate
AbstractMonthlyInsightTemplate <|-- IncomeInsightTemplate
AbstractMonthlyInsightTemplate <|-- SavingsInsightTemplate
AbstractMonthlyInsightTemplate ..> WalletInsight : creates
