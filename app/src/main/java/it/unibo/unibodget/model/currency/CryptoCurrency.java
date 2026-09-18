package it.unibo.unibodget.model.currency;

/**
 * Supported cryptocurrencies.
 *
 * <p>Each constant defines: symbol, short name, full name, currency code,
 * CoinGecko API id, and whether it is a stablecoin.</p>
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

    private static final int BTC_DECIMALS = 8;
    private static final int ETH_DECIMALS = 6;
    private static final int STABLE_DECIMALS = 2;
    private static final int DEFAULT_DECIMALS = 4;

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
    CryptoCurrency(
            final String symbol,
            final String shortName,
            final String fullName,
            final String code,
            final String apiId,
            final boolean isStableCoin
    ) {
        this.symbol = symbol;
        this.shortName = shortName;
        this.fullName = fullName;
        this.code = code;
        this.apiId = apiId;
        this.isStableCoin = isStableCoin;
    }

    /** {@inheritDoc} */
    @Override
    public CurrencyType getType() {
        return this.type;
    }

    /** {@inheritDoc} */
    @Override
    public String getSymbol() {
        return this.symbol;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortName() {
        return this.shortName;
    }

    /** {@inheritDoc} */
    @Override
    public String getFullName() {
        return this.fullName;
    }

    /** {@inheritDoc} */
    @Override
    public String getCode() {
        return this.code;
    }

    /** {@inheritDoc} */
    @Override
    public int getDisplayDecimals() {
        return switch (this) {
            case BTC -> BTC_DECIMALS;
            case ETH -> ETH_DECIMALS;
            case USDT, EURC -> STABLE_DECIMALS;
            default -> DEFAULT_DECIMALS;
        };
    }

    /**
     * Returns the CoinGecko API identifier used to fetch live price data.
     *
     * @return the API id, e.g. {@code "bitcoin"} for BTC
     */
    public String getApiId() {
        return this.apiId;
    }

    /**
     * Returns whether this cryptocurrency is a stablecoin.
     *
     * @return {@code true} for stablecoins such as USDT, USDC, EURC
     */
    public boolean isStableCoin() {
        return this.isStableCoin;
    }
}
