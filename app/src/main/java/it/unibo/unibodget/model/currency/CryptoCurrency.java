package it.unibo.unibodget.model.currency;

/**
 * Enum to represent cryptocurrencies.
 * 
 * <p>
 * Each enum constant defines:
 * - type (fixed to "Crypto")
 * - symbol
 * - short ISO name
 * - full name
 * - ISO code
 * - API identifier for price fetching
 */
public enum CryptoCurrency implements CurrencyUnit {

    BTC("₿", "BTC", "Bitcoin", "BTC", "bitcoin", false),
    ETH("Ξ", "ETH", "Ethereum", "ETH", "ethereum", false),
    SOL("◎", "SOL", "Solana", "SOL", "solana", false),
    USDT("₮", "USDT", "Tether", "USDT", "tether", true),
    USDC("₩", "USDC", "USD Coin", "USDC", "usd-coin", true),
    EURC("€", "EURC", "Euro Coin", "EURC", "euro-coin", true),
    XMR("ɱ", "XMR", "Monero", "XMR", "monero", false),
    ADA("₳", "ADA", "Cardano", "ADA", "cardano", false),
    XRP("✕", "XRP", "Ripple", "XRP", "ripple", false),
    DOT("●", "DOT", "Polkadot", "DOT", "polkadot", false),
    DOGE("Ð", "DOGE", "Dogecoin", "DOGE", "dogecoin", false),
    AVAX("A", "AVAX", "Avalanche", "AVAX", "avalanche-2", false),
    MATIC("M", "MATIC", "Polygon", "MATIC", "matic-network", false),
    LTC("Ł", "LTC", "Litecoin", "LTC", "litecoin", false),
    XLM("Ł", "XLM", "Stellar", "XLM", "stellar", false),
    LINK("⛓", "LINK", "Chainlink", "LINK", "chainlink", false),
    UNI("U", "UNI", "Uniswap", "UNI", "uniswap", false);

    private final CurrencyType type = CurrencyType.CRYPTO;
    private final String symbol;
    private final String shortName;
    private final String fullName;
    private final String code;
    private final String apiId;
    private final boolean isStableCoin;

    /**
     * Constructs a cryptocurrency definition.
     *
     * @param symbol    the graphical symbol of the cryptocurrency (e.g., "₿", "Ξ")
     * @param shortName the short identifier or ticker (e.g., "BTC", "ETH")
     * @param fullName  the full descriptive name of the cryptocurrency
     * @param code      the standardized currency code (often equal to the ticker)
     * @param apiId     the identifier used by external APIs or data providers
     * @param isStableCoin whether the cryptocurrency is a stablecoin (e.g., USDT)
     */
    CryptoCurrency(final String symbol, final String shortName, final String fullName, 
                    final String code, final String apiId, final boolean isStableCoin) {
        this.symbol = symbol;
        this.shortName = shortName;
        this.fullName = fullName;
        this.code = code;
        this.apiId = apiId;
        this.isStableCoin = isStableCoin;
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

    /**
     * Get ApiId
     * 
     * @return apiId
     */
    public String getApiId() {
        return this.apiId;
    }

    /** 
     * Return true if it's stable, false otherwise
     * 
     * @return true if stable
     */
    public boolean isStableCoin() {
        return this.isStableCoin;
    }

}
