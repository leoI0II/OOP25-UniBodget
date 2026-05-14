# OOP25-UniBodget — Persistency - UML Class Diagram
GitHub renders the diagram below automatically.

```mermaid
classDiagram
    direction TB

    %% ============================================================
    %% ========== FILEMANAGER API ==========
    %% ============================================================

    class FileCreator {
        <<interface>>
        +Path create(Path) throws IOException
    }

    class FileInitializer {
        <<interface>>
        +initialize() throws FileInitializationException
    }

    class FileOpener {
        <<interface>>
        +Path open(Path) throws IOException
    }

    class FileInitializationException {
        <<exception>>
    }

    %% ============================================================
    %% ========== FILEMANAGER IMPL ==========
    %% ============================================================

    class BasicFileCreator {
        <<abstract>>
        +create(Path) Path
        #ensureParentExists(Path)
    }

    class JsonFileCreator {
        +create(Path) Path
    }

    class FileCreatorFactory {
        <<static>>
        +create(Path) FileCreator
    }

    class BasicFileInitializer {
        <<abstract>>
        +initialize()
        #writeDefaultContent(Path)
    }

    class CurrencyFileInitializer {
        +initialize()
    }

    class CategoryFileInitializer {
        +initialize()
    }

    class MovementFileInitializer {
        +initialize()
    }

    class UserDataFileInitializer {
        +initialize()
    }

    class FileInitializerFactory {
        <<static>>
        +create(Path) FileInitializer
    }

    class SafeFileOpener {
        +open(Path) Path
    }

    %% ============================================================
    %% ========== PARSER API ==========
    %% ============================================================

    class DataParser~T~ {
        <<interface>>
        +T parse(String) throws DataParserException
    }

    class DataParserException {
        <<exception>>
    }

    class DataSerializer~T~ {
        <<interface>>
        +String serialize(T) throws DataSerializerException
    }

    class DataSerializerException {
        <<exception>>
    }

    %% ============================================================
    %% ========== PARSER IMPL ==========
    %% ============================================================

    class BasicDataParser {
        <<abstract>>
        +parse(String)
        #extractField(String,String)
    }

    class CategoryParser {
        +parse(String) Category
    }

    class CurrencyParser {
        +parse(String) CurrencyUnit
    }

    class MovementDataParser {
        +parse(String) CashTransaction
    }

    class UserDataParser {
        +parse(String) UserData
    }

    class ParserFactory {
        <<static>>
        +create(String) DataParser
    }

    class BasicDataSerializer {
        <<abstract>>
        +serialize(Object)
    }

    class JsonDataSerializer {
        +serialize(T) String
        -serializeValue(Object,Map)
        -serializeObject(Object,Map)
        -serializeRecord(Object,Map)
        -serializeList(List,Map)
        -serializeMap(Map,Map)
        -escape(String)
    }

    class SerializerFactory {
        <<static>>
        +create(String) DataSerializer
    }

    %% ============================================================
    %% ========== READER API ==========
    %% ============================================================

    class FileReader~T~ {
        <<interface>>
        +T readFile() throws IOException
    }

    %% ============================================================
    %% ========== READER IMPL ==========
    %% ============================================================

    class BasicReader~T~ {
        <<abstract>>
        -String path
        +getPath()
        +readFile()
        -validatePath()
    }

    class JsonReader {
        +readFile()
    }

    class FileReaderFactory {
        <<static>>
        +create(String) FileReader
    }

    %% ============================================================
    %% ========== UTIL API ==========
    %% ============================================================

    class Logger {
        <<interface>>
        +info(String)
        +warn(String)
        +error(String,Throwable)
    }

    %% ============================================================
    %% ========== UTIL IMPL ==========
    %% ============================================================

    class LoggerImpl {
        +info(String)
        +warn(String)
        +error(String,Throwable)
    }

    %% ============================================================
    %% ========== WRITER API ==========
    %% ============================================================

    class FileAppender {
        <<interface>>
        +append(Path,String) throws IOException
    }

    class FileOverwriter {
        <<interface>>
        +overwrite(Path,String) throws IOException
    }

    class FileSaver {
        <<interface>>
        +save(Path,String) throws IOException
    }

    class FileUpdater {
        <<interface>>
        +update(Path,String) throws IOException
    }

    %% ============================================================
    %% ========== WRITER IMPL ==========
    %% ============================================================

    class BasicFileAppender {
        <<abstract>>
        +append(Path,String)
    }

    class JsonFileAppender {
        +append(Path,String)
    }

    class BasicFileOverwriter {
        <<abstract>>
        +overwrite(Path,String)
    }

    class JsonFileOverwriter {
        +overwrite(Path,String)
    }

    class BasicFileSaver {
        <<abstract>>
        +save(Path,String)
    }

    class JsonFileSaver {
        +save(Path,String)
    }

    class BasicFileUpdater {
        <<abstract>>
        +update(Path,String)
    }

    class JsonFileUpdater {
        +update(Path,String)
    }

    class WriterFactory {
        <<static>>
        +createAppender(String)
        +createOverwriter(String)
        +createSaver(String)
        +createUpdater(String)
    }

    %% ============================================================
    %% ========== RELATIONS ==========
    %% ============================================================

    %% FileCreator
    FileCreator <|.. BasicFileCreator
    BasicFileCreator <|-- JsonFileCreator
    FileCreatorFactory --> FileCreator

    %% FileInitializer
    FileInitializer <|.. BasicFileInitializer
    BasicFileInitializer <|-- CurrencyFileInitializer
    BasicFileInitializer <|-- CategoryFileInitializer
    BasicFileInitializer <|-- MovementFileInitializer
    BasicFileInitializer <|-- UserDataFileInitializer
    FileInitializerFactory --> FileInitializer

    %% FileOpener
    FileOpener <|.. SafeFileOpener

    %% Reader
    FileReader <|.. BasicReader
    BasicReader <|-- JsonReader
    FileReaderFactory --> FileReader

    %% Parser
    DataParser <|.. BasicDataParser
    BasicDataParser <|-- CategoryParser
    BasicDataParser <|-- CurrencyParser
    BasicDataParser <|-- MovementDataParser
    BasicDataParser <|-- UserDataParser
    ParserFactory --> DataParser

    DataSerializer <|.. BasicDataSerializer
    BasicDataSerializer <|-- JsonDataSerializer
    SerializerFactory --> DataSerializer

    %% Util
    Logger <|.. LoggerImpl

    %% Writer
    FileAppender <|.. BasicFileAppender
    BasicFileAppender <|-- JsonFileAppender

    FileOverwriter <|.. BasicFileOverwriter
    BasicFileOverwriter <|-- JsonFileOverwriter

    FileSaver <|.. BasicFileSaver
    BasicFileSaver <|-- JsonFileSaver

    FileUpdater <|.. BasicFileUpdater
    BasicFileUpdater <|-- JsonFileUpdater

    WriterFactory --> FileAppender
    WriterFactory --> FileOverwriter
    WriterFactory --> FileSaver
    WriterFactory --> FileUpdater

    %% Exceptions
    FileInitializer --> FileInitializationException
    DataParser --> DataParserException
    DataSerializer --> DataSerializerException
