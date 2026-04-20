package it.unibo.unibodget.model.currency;

/**
 * Enum to represent cryptocurrencies.
 * 
 * Each enum constant defines:
 * - type (fixed to "Crypto")
 * - symbol
 * - short ISO name
 * - full name
 * - ISO code
 * - API identifier for price fetching
 */
public enum CryptoCurrency implements CurrencyUnit {

    BTC("₿", "BTC", "Bitcoin", "BTC", "bitcoin"),
    ETH("Ξ", "ETH", "Ethereum", "ETH", "ethereum"),
    SOL("◎", "SOL", "Solana", "SOL", "solana");

    private final CurrencyType type = CurrencyType.CRYPTO;
    private final String symbol;
    private final String shortName;
    private final String fullName;
    private final String code;
    private final String apiId;

    /**
     * Constructs a cryptocurrency definition.
     *
     * @param type      the type of currency (fixed to "Crypto" for this enum)
     * @param symbol    the graphical symbol of the cryptocurrency (e.g., "₿", "Ξ")
     * @param shortName the short identifier or ticker (e.g., "BTC", "ETH")
     * @param fullName  the full descriptive name of the cryptocurrency
     * @param code      the standardized currency code (often equal to the ticker)
     * @param apiId     the identifier used by external APIs or data providers
     */
    CryptoCurrency(String symbol, String shortName, String fullName, String code, String apiId) {
        this.symbol = symbol;
        this.shortName = shortName;
        this.fullName = fullName;
        this.code = code;
        this.apiId = apiId;
    }

    @Override
    public CurrencyType getType() {
        return this.type;
    }

    @Override
    public String getSymbol() { 
        return this.symbol; 
    }

    @Override
    public String getShortName() { 
        return this.shortName; 
    }

    @Override
    public String getFullName() { 
        return this.fullName; 
    }

    @Override
    public String getCode() {
        return this.code;
    }

    public String getApiId() {
        return this.apiId;
    }

}
