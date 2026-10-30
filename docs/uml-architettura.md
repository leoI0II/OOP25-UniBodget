
classDiagram
direction TB

namespace View {
    class MainViewController {
        +showDashboard() void
        +showInvestments() void
        +showConverter() void
    }

    class DashboardView {
        <<interface>>
        +render(state: DashboardViewState) void
        +promptCreateTransaction(...)
    }

    class InvestmentsViewController {
        +initialize() void
    }

    class CurrencyConverterViewFX {
        +buildContent(c: CurrencyConverterController) Parent
    }

    class SettingsPopupFX {
        +show(owner: Stage) void
    }
}

namespace Controller {
    class DashboardViewActions {
        <<interface>>
        +onWalletSelected(id: UUID) void
        +onCreateTransactionRequested() void
    }

    class DefaultDashboardController {
        +init() void
        +update() void
    }

    class InvestmentController {
        <<interface>>
        +executeBuyOrder(account, source: PaymentSource, ...) OrderResult
        +getPositions() List~Position~
    }

    class CurrencyConverterController {
        +convert(amount, from, to) BigDecimal
        +getHistoryPoints(...) List~CurrencyHistoryPoint~
    }

    class SettingsController {
        +changeTheme(t: Theme) void
        +changeBaseCurrency(code: String) void
    }
}

namespace Model {
    class DashboardFacade {
        <<interface>>
        +loadDashboard() DashboardSnapshot
        +selectWallet(id: UUID) void
    }

    class CashAccountService {
        <<final>>
        +addTransaction(t: CashTransaction) void
    }

    class InvestmentAccountService {
        <<final>>
        +getWallets() List~InvestmentAccount~
    }

    class CurrencyConverter {
        <<interface>>
        +convert(amount, from, to) CurrencyConversionResult
    }

    class Settings {
        <<final>>
        +getTheme() Theme
        +getBaseCurrencyUnit() CurrencyUnit
    }

    class WalletService~T,W~ {
        <<interface>>
        +getWallets() List~W~
        +selectWallet(id: UUID) boolean
        +addTransaction(t: T) void
    }

    class WalletAutoSaver {
        <<final>>
        +start() void
        +saveAll() void
    }

    class MessageBus {
        <<final>>
        +send(e: MessageEvent) void
        +subscribe(type, s: Consumer) Consumer
    }
}

MainViewController ..> DashboardView : visualizza
MainViewController ..> InvestmentsViewController : visualizza
MainViewController ..> CurrencyConverterViewFX : visualizza
MainViewController ..> SettingsPopupFX : visualizza

DashboardView ..> DashboardViewActions : inoltra azioni
DefaultDashboardController ..|> DashboardViewActions : implementa
DefaultDashboardController ..> DashboardView : render stato
DefaultDashboardController o-- DashboardFacade : comanda

InvestmentsViewController ..> InvestmentController : richiede ordini
InvestmentController o-- CashAccountService : delega liquidita
InvestmentController o-- InvestmentAccountService : delega investimenti
InvestmentController ..> CurrencyConverter : prezzi di mercato

CurrencyConverterViewFX ..> CurrencyConverterController : richiede tassi e conversioni
CurrencyConverterController o-- CurrencyConverter : comanda

SettingsPopupFX ..> SettingsController : inoltra le scelte
SettingsController ..> Settings : aggiorna

DashboardFacade ..> CashAccountService : delega cassa

CashAccountService ..> WalletService : usa
InvestmentAccountService ..> WalletService : usa

WalletService ..> MessageBus : pubblica eventi
WalletAutoSaver ..> MessageBus : ascolta e salva su file
MainViewController ..> MessageBus : sottoscrive per notifiche
