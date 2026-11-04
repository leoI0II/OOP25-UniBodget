# 💰 OOP25-UniBodget

Progetto per il corso di Programmazione a Oggetti (OOP) — Università di Bologna (A.A. 2024/2025).  
**Gruppo:** Arianna Palmucci, Daniele Zappalà, Favour Osaretin Emuwahen.

---

## 📌 Descrizione e Funzionamento

**UniBodget** è un'applicazione desktop in Java 21 e JavaFX per la gestione della finanza personale, organizzata in tre moduli principali:

- **Dashboard:** monitoraggio del saldo cassa, registrazione atomica delle transazioni, soglie di budget e trend mensili comparativi.
- **Portfolio Investimenti:** compravendita di titoli azionari e crypto, calcolo posizioni a costo medio ponderato e storico andamento.
- **Convertitore Valute:** triangolazione multivaluta con tassi live, gestione commissioni bancarie e watchlist con notifiche di soglia.
- **Impostazioni (Settings):** personalizzazione a caldo del tema visivo (Dark/Light), valuta base globale e storico configurazioni (Memento).

### 🚀 Avvio dell'Applicazione
Per eseguire il software da riga di comando:
```bash
./gradlew run
```
Opzionalmente è possibile specificare una chiave Alpha Vantage per le quotazioni azionarie con:
```bash
ALPHA_VANTAGE_API_KEY=CHIAVE_PERSONALE ./gradlew run
```

---

## 💾 Persistenza dei Dati (Cartella `data/`)

Tutte le informazioni dell'utente vengono gestite e salvate automaticamente su disco. Indicazioni operative fondamentali:

- **Struttura dei file:** le informazioni sono salvate in formato JSON nella cartella `data/` (e le istantanee del portfolio in `data/csv/snapshots/`).
- **Salvataggio automatico:** ogni modifica a portafogli, transazioni, budget, preferenze e watchlist persiste all'istante su file.
- **Portabilità e Backup:** per conservare i dati o spostarli su un altro computer è sufficiente copiare l'intera cartella `data/`.
- **Risoluzione percorsi (`AppDataPathResolver`):** l'applicazione individua autonomamente la cartella `data/` anche avviata da directory diverse.
- **Dati dimostrativi (`data-1` in `demo-data/`):** per esplorare l'app con dati già popolati, estrarre la cartella `data-1` da `demo-data/` e rinominarla in `data/` prima dell'avvio.
- **Ripristino automatico:** se la cartella `data/` è assente, UniBodget genera una nuova struttura vergine con impostazioni predefinite senza blocchi.

---

## 🧱 Architettura del Progetto

Il software adotta il pattern architetturale **Model-View-Controller (MVC)** con totale disaccoppiamento:

- **`model/`:** logica di dominio finanziario pura (conti, transazioni, valute, investimenti, budget) 100% priva di dipendenze da JavaFX.
- **`view/`:** interfaccia grafica JavaFX modulare (Dashboard, Portfolio, Convertitore, Sidebar condivisa) basata su Passive View.
- **`controller/`:** gestione del flusso e orchestrazione dei comandi utente senza dipendenze dirette da nodi grafici.
- **`persistency/`:** serializzazione e deserializzazione JSON basata su Jackson, gestione file e risoluzione dei percorsi.
- **`MessageBus`:** canale di eventi Publish/Subscribe per la notifica reattiva delle modifiche tra moduli senza accoppiamento.

---

## 🧪 Qualità del Codice e Test

Tutti i vincoli di qualità e verifica statica sono controllati via Gradle:
- **Build e QA:** `./gradlew build check` (Checkstyle, PMD, SpotBugs e Spotless con zero violazioni).
- **Test automatizzati:** oltre 320 test unitari e di integrazione con JUnit 5 (`./gradlew test`).
