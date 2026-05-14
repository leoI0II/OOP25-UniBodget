package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable read model representing the dashboard summary of a friend loan.
 */
public final class FriendLoanSummary {

    private final UUID friendLoanId;
    private final String friendName;
    private final BigDecimal totalGiven;
    private final BigDecimal totalReceived;
    private final BigDecimal netBalance;

    /**
     * Creates a new friend-loan summary.
     *
     * @param friendLoanId
     *            the identifier of the friend loan; must not be null
     * @param friendName
     *            the related friend's name; must not be null
     * @param totalGiven
     *            the total amount given to the friend; must not be null
     * @param totalReceived
     *            the total amount received from the friend; must not be null
     * @param netBalance
     *            the signed residual balance; must not be null
     */
    public FriendLoanSummary(
            final UUID friendLoanId,
            final String friendName,
            final BigDecimal totalGiven,
            final BigDecimal totalReceived,
            final BigDecimal netBalance) {
        this.friendLoanId = Objects.requireNonNull(friendLoanId);
        this.friendName = Objects.requireNonNull(friendName);
        this.totalGiven = Objects.requireNonNull(totalGiven);
        this.totalReceived = Objects.requireNonNull(totalReceived);
        this.netBalance = Objects.requireNonNull(netBalance);
    }

    /**
     * Returns the identifier of the friend loan.
     *
     * @return the friend-loan identifier
     */
    public UUID getFriendLoanId() {
        return friendLoanId;
    }

    /**
     * Returns the name of the related friend.
     *
     * @return the friend's name
     */
    public String getFriendName() {
        return friendName;
    }

    /**
     * Returns the total amount given to the friend.
     *
     * @return the total given amount
     */
    public BigDecimal getTotalGiven() {
        return totalGiven;
    }

    /**
     * Returns the total amount received from the friend.
     *
     * @return the total received amount
     */
    public BigDecimal getTotalReceived() {
        return totalReceived;
    }

    /**
     * Returns the signed net balance of the friend loan.
     *
     * <p>A positive balance means the user has given more than received.
     * A negative balance means the user has received more than given.</p>
     *
     * @return the signed net balance
     */
    public BigDecimal getNetBalance() {
        return netBalance;
    }
}