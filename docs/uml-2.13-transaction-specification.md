classDiagram
direction TB

class DefaultTransactionHistoryFilterService {
    +List~CashTransaction~ filterTransactions(List~CashTransaction~ transactions, TransactionFilterCriteria criteria)
}

class TransactionSpecifications {
    <<utility>>
    +TransactionSpecification byCategoryName(String categoryName)
    +TransactionSpecification byCategoryType(String categoryType)
    +TransactionSpecification byDateRange(LocalDate startDate, LocalDate endDate)
    +TransactionSpecification byKeyword(String keyword)
}

class TransactionSpecification {
    <<interface>>
    +boolean isSatisfiedBy(CashTransaction transaction)
}

class CategoryNameSpecification {
    -String categoryName
    +boolean isSatisfiedBy(CashTransaction transaction)
}

class CategoryTypeSpecification {
    -String categoryType
    +boolean isSatisfiedBy(CashTransaction transaction)
}

class DateRangeSpecification {
    -LocalDate startDate
    -LocalDate endDate
    +boolean isSatisfiedBy(CashTransaction transaction)
}

class KeywordSpecification {
    -String keyword
    +boolean isSatisfiedBy(CashTransaction transaction)
}

class TransactionFilterCriteria {
    <<record>>
    +String categoryName
    +String categoryType
    +LocalDate startDate
    +LocalDate endDate
    +String keyword
}

class CashTransaction

DefaultTransactionHistoryFilterService ..> TransactionSpecifications : uses
DefaultTransactionHistoryFilterService ..> TransactionFilterCriteria : reads
DefaultTransactionHistoryFilterService ..> TransactionSpecification : applies
TransactionSpecifications ..> TransactionSpecification : creates
TransactionSpecification <|.. CategoryNameSpecification
TransactionSpecification <|.. CategoryTypeSpecification
TransactionSpecification <|.. DateRangeSpecification
TransactionSpecification <|.. KeywordSpecification
TransactionSpecification ..> CashTransaction : evaluates
