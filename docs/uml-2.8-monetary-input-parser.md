classDiagram
direction TB

class MonetaryInputParser {
  <<Single Source of Truth>>
  -Pattern DECIMAL_DELIMITER_PATTERN
  -Pattern STRICT_NUMERIC_PATTERN
  +sanitize(input: String) String
  +parse(input: String) Optional~BigDecimal~
  +parsePositive(input: String) Optional~BigDecimal~
  +parseNonNegative(input: String) Optional~BigDecimal~
  +parsePositiveWithMaxScale(input: String, maxScale: int) Optional~BigDecimal~
  +parseDouble(input: String) Optional~Double~
  +parsePositiveDouble(input: String) Optional~Double~
  +parseNonNegativeDouble(input: String) Optional~Double~
}

class ConverterWidgetFX {
  +parseDecimal(text: String) BigDecimal
}

class BankConverterWidgetFX {
  +parsePositiveAmount(text: String) BigDecimal
  +parseFee(text: String) BigDecimal
}

class CurrencyHistoryChartView {
  +parseThreshold(text: String) Optional~Double~
}

class NewTransactionDialog {
  +parseAmount(text: String) BigDecimal
}

ConverterWidgetFX ..> MonetaryInputParser : parse
BankConverterWidgetFX ..> MonetaryInputParser : parsePositive
CurrencyHistoryChartView ..> MonetaryInputParser : parseDouble
NewTransactionDialog ..> MonetaryInputParser : parse
