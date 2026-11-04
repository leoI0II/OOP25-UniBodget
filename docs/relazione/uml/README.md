# Indice degli Schemi UML di UniBodget

Questa cartella raccoglie tutti i diagrammi UML in formato **Mermaid** (`.md`) e vettoriale (`.svg`) utilizzati nella documentazione di progetto e all'interno della relazione finale in LaTeX (`docs/relazione/relazione.tex`).

Tutti i diagrammi sono scritti secondo lo standard Mermaid (`classDiagram`) e vengono renderizzati automaticamente sia da GitHub sia dalle estensioni Markdown di VS Code e dei principali IDE.

---

## 1. Architettura e Dominio

| Diagramma Relazione | File Sorgente Markdown | Immagine Inclusa in LaTeX | Descrizione |
| :--- | :--- | :--- | :--- |
| **Dominio (Parte 1)** | [`uml-dominio-conti.md`](uml-dominio-conti.md) | `img/uml-dominio-1.pdf` | Conti, storico transazioni, posizioni e gestione budget |
| **Dominio (Parte 2)** | [`uml-dominio-valute.md`](uml-dominio-valute.md) | `img/uml-dominio-2.pdf` | Mercati, tassi di cambio, watchlist, banche e conversioni |
| **Dominio Completo** | [`uml-dominio.md`](uml-dominio.md) | — | Unione sinottica del dominio di liquidità, investimenti e mercato |
| **Figura 2.1** | [`uml-architettura.md`](uml-architettura.md) | `img/uml-architettura.pdf` | Architettura MVC disaccoppiata e MessageBus |

---

## 2. Design Dettagliato — Arianna Palmucci (Modulo Valute e Mercato)

| Figura Relazione | File Sorgente Markdown | Immagine Inclusa in LaTeX | Descrizione e Pattern |
| :---: | :--- | :--- | :--- |
| **Fig. 2.2** | [`uml-currency-2.2.md`](uml-currency-2.2.md) / [`uml-currency.md`](uml-currency.md) | `img/uml-currency.png` | Gerarchia `CurrencyUnit`, `Asset` e Strategy `CurrencyConverter` |
| **Fig. 2.3** | [`uml-conversion-providers-2.3.md`](uml-conversion-providers-2.3.md) / [`uml-conversion.md`](uml-conversion.md) | `img/uml-conversion.pdf` | Caching con TTL, fallback deterministico e Adapter API esterne |
| **Fig. 2.4** | [`uml-alert-watchlist-2.4.md`](uml-alert-watchlist-2.4.md) / [`uml-alert-watchlist.md`](uml-alert-watchlist.md) | `img/uml-alert-watchlist.png` | Rule engine per soglie alert e modello persistente watchlist |
| **Fig. 2.5** | [`uml-settings-memento-2.5.md`](uml-settings-memento-2.5.md) / [`uml-settings.md`](uml-settings.md) | `img/uml-settings.pdf` | Memento Pattern per le impostazioni e `ThemeManager` a caldo |
| **Fig. 2.6** | [`uml-view-converter-2.6.md`](uml-view-converter-2.6.md) | `img/uml-view-converter.pdf` | Facade e scomposizione modulare dei widget della view |
| **Fig. 2.7** | [`uml-app-data-path-resolver-2.7.md`](uml-app-data-path-resolver-2.7.md) / [`uml-2.7-app-data-path-resolver.md`](uml-2.7-app-data-path-resolver.md) | `img/uml-app-data-path-resolver.pdf` | Risolutore centralizzato dei percorsi dati e template self-healing |
| **Fig. 2.8** | [`uml-monetary-input-parser-2.8.md`](uml-monetary-input-parser-2.8.md) / [`uml-2.8-monetary-input-parser.md`](uml-2.8-monetary-input-parser.md) | `img/uml-monetary-input-parser.pdf` | Parser e normalizzatore numerico tollerante per input monetari |

---

## 3. Design Dettagliato — Favour Osaretin Emuwahen (Modulo Dashboard e Cassa)

| Figura Relazione | File Sorgente Markdown | Immagine Inclusa in LaTeX | Descrizione e Pattern |
| :---: | :--- | :--- | :--- |
| **Fig. 2.9** | [`uml-2.9-dashboard-facade.md`](uml-2.9-dashboard-facade.md) | `img/uml-facade-dashboard.pdf` | Facade Pattern e Passive View per il sottosistema Dashboard |
| **Fig. 2.10** | [`uml-2.10-cash-transaction-builder-factory.md`](uml-2.10-cash-transaction-builder-factory.md) | `img/uml-builder-transaction.pdf` | Builder Pattern e Factory per la creazione sicura di `CashTransaction` |
| **Fig. 2.11** | [`uml-2.11-budget-strategy.md`](uml-2.11-budget-strategy.md) | `img/uml-strategy-budget.pdf` | Strategy Pattern per la valutazione gerarchica delle allerte di budget |
| **Fig. 2.12** | [`uml-2.12-monthly-insights.md`](uml-2.12-monthly-insights.md) | `img/uml-template-walletInsight.pdf` | Template Method Pattern per il calcolo comparativo dei trend mensili |
| **Fig. 2.13** | [`uml-2.13-transaction-specification.md`](uml-2.13-transaction-specification.md) | `img/uml-specifications-filter.pdf` | Specification Pattern per il filtraggio avanzato e componibile |

---

## 4. Design Dettagliato — Daniele Zappalà (Modulo Investimenti, Eventi e Shell)

| Figura Relazione | File Sorgente Markdown | Immagine Inclusa in LaTeX | Descrizione e Pattern |
| :---: | :--- | :--- | :--- |
| **Fig. 2.14** | [`uml-2.14-payment-orders.md`](uml-2.14-payment-orders.md) | `img/uml-dan-ordini.pdf` | Tipi algebrici di dati (sealed interface + record) per sorgenti ed esiti |
| **Fig. 2.15** | [`uml-2.15-investment-balance.md`](uml-2.15-investment-balance.md) | `img/uml-dan-saldo.pdf` | Saldo e posizioni calcolati dallo storico (Template Method e Strategy) |
| **Fig. 2.16** | [`uml-2.16-app-ticker-events.md`](uml-2.16-app-ticker-events.md) | `img/uml-dan-tempo.pdf` | Eventi Publish/Subscribe (`MessageBus`), tempo (`AppTicker`) e salvataggi |
| **Fig. 2.17** | [`uml-2.17-main-window-shell.md`](uml-2.17-main-window-shell.md) | `img/uml-dan-shell.pdf` | Finestra principale, barra laterale (`SideBarDelegate`) e ciclo di vita |

---

## 5. Diagrammi Architetturali di Package Completi

| Documento | Descrizione |
| :--- | :--- |
| [`model.md`](model.md) | Schema UML dettagliato dell'intero layer **Model** |
| [`view.md`](view.md) | Schema UML dettagliato dell'intero layer **View** |
| [`controller.md`](controller.md) | Schema UML dettagliato dell'intero layer **Controller** |
| [`persistency-diagram.md`](persistency-diagram.md) | Architettura completa di persistenza su file JSON, CSV e percorsi |
| [`uml.md`](uml.md) | Diagramma complessivo unificato di tutte le classi dell'applicazione |

