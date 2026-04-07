# OOP25-UniBodget — Persistency - UML Class Diagram
GitHub renders the diagram below automatically.

```mermaid
classDiagram
    direction TB

    %% ========== FILEMANAGER API ==========

    class FileCreator {
        <<interface>>
        +Path open(Path path) throws IOException
    }

    class FileInitializer {
        <<interface>>
        +initialize() throws FileInitializationException
    }

    class FileOpener {
        <<interface>>
        +Path open(Path path) throws IOException
    }

    class FileInitializationException {
        <<exception>>
    }

    %% ========== FILEMANAGER IMPL ==========

    class BasicFileCreator {
        <<abstract>>
    }

    class JsonFileCreator {
    }

    class FileCreatorFactory {
        <<static>>
        +create(Path)
    }

    class BasicFileInitializer {
        <<abstract>>
    }

    class CurrencyFileInitializer {
    }

    class CategoryFileInitializer {
    }

    class MovementFileInitializer {
    }

    class UserDataFileInitializer {
    }

    class FileInitializerFactory {
        <<static>>
        +create(Path)
    }

    class SafeFileOpener {
    }

    %% ========== PARSER API ==========

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

    %% ========== PARSER IMPL ==========

    class BasicDataParser {
        <<abstract>>
    }

    class CategoryParser {
    }

    class CurrencyParser {
    }

    class MovementDataParser {
    }

    class UserDataParser {
    }

    class ParserFactory {
        <<static>>
        +create(String)
    }

    class BasicDataSerializer {
        <<abstract>>
    }

    class JsonDataSerializer {
    }

    class SerializerFactory {
        <<static>>
        +create(String)
    }

    %% ========== READER API ==========

    class FileReader~T~ {
        <<interface>>
        +T readFile() throws IOException
    }

    %% ========== READER IMPL ==========

    class BasicReader~T~ {
        <<abstract>>
        -String path
        +getPath()
        -validatePath()
    }

    class JsonReader {
    }

    class FileReaderFactory {
        <<static>>
        +create(String)
    }

    %% ========== UTIL API ==========

    class Logger {
        <<interface>>
        +info(String)
        +warn(String)
        +error(String, Throwable)
    }

    %% ========== UTIL IMPL ==========

    class LoggerImpl {
    }

    %% ========== RELATIONS ==========

    %% FileCreator
    FileCreator <|.. BasicFileCreator : implements
    BasicFileCreator <|-- JsonFileCreator : extends
    FileCreatorFactory --> FileCreator : creates

    %% FileInitializer
    FileInitializer <|.. BasicFileInitializer : implements
    BasicFileInitializer <|-- CurrencyFileInitializer : extends
    BasicFileInitializer <|-- CategoryFileInitializer : extends
    BasicFileInitializer <|-- MovementFileInitializer : extends
    BasicFileInitializer <|-- UserDataFileInitializer : extends
    FileInitializerFactory --> FileInitializer : creates

    %% FileOpener
    FileOpener <|.. SafeFileOpener : implements

    %% Reader
    FileReader <|.. BasicReader : implements
    BasicReader <|-- JsonReader : extends
    FileReaderFactory --> FileReader : creates

    %% Parser
    DataParser <|.. BasicDataParser : implements
    BasicDataParser <|-- CategoryParser : extends
    BasicDataParser <|-- CurrencyParser : extends
    BasicDataParser <|-- MovementDataParser : extends
    BasicDataParser <|-- UserDataParser : extends
    ParserFactory --> DataParser : creates

    DataSerializer <|.. BasicDataSerializer : implements
    BasicDataSerializer <|-- JsonDataSerializer : extends
    SerializerFactory --> DataSerializer : creates

    %% Util
    Logger <|.. LoggerImpl : implements

    %% ========== EXCEPTION RELATIONS ==========

    FileInitializer --> FileInitializationException : throws
    DataParser --> DataParserException : throws
    DataSerializer --> DataSerializerException : throws