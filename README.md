# 💰 OOP25-UniBodget  
Progetto universitario per il corso di Programmazione a Oggetti (OOP) 
Gruppo composto da: **Arianna Palmucci**, **Daniele Zappalà**, **Favour Emuwahen**

---

## 📌 Descrizione del progetto

**UniBodget** è un’applicazione Java progettata per gestire:

- transazioni e movimenti
- conversione valute
- investimenti titoli
- preferenze utente e impostazioni personalizzate
- persistenza dei dati tramite parser e serializer generici

---

## 🧱 Architettura generale

Il progetto è organizzato in più parti:

### **1. Model**
Contiene tutte le classi del dominio, suddivise in package:

- `model.currency` → Currency (diff type and values), Asset
- `model.transactions` → Transaction (diff types), Historical
- `model.settings` → Settings, Theme, BudgetLimit
- `model.wallet` → CashAccount, InvestmentAccount, Wallet

Tutte le classi sono compatibili con il parser generico.

---

### **2. Persistency**
Gestione di lettura e scrittura dei dati:

## FileManager
- `FileCreator` → crea un file
- `FileInitializer` → inizializza un file
- `FileOpener` → apre un file

## Parser
- `JsonDataParser<T>` → parser generico ricorsivo
- `JsonDataSerializer<T>` → serializer generico
- `ParserFactory` / `SerializerFactory`  

## Reader
- `JsonDataReader<T>` → json file reader

La persistenza è completamente **data‑driven**:  
i tipi di valuta, investimenti e impostazioni vengono caricati da file JSON.
⚠️ da completare ⚠️

---

### **3. Menu principale**
⚠️ da scrivere ⚠️

---

### **4. History**
⚠️ da scrivere ⚠️

---

### **5. Currency converter**

Il modulo Currency Converter gestisce la conversione tra valute diverse utilizzando:
- valori caricati da file JSON
- un sistema di conversione basato su tassi dinamici
⚠️ da completare ⚠️

---

### **6. Stock Market**
⚠️ da scrivere ⚠️

---

## 📂 Struttura del progetto
⚠️ da completare con struttura cartelle e files ⚠️
