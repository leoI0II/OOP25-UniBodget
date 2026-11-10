package it.unibo.unibodget.view.dashboard.impl;

/** Identifies the kind of friend-loan transaction the user is creating. */
public enum FriendLoanOperation {
    /** Creating a new loan or credit. */
    NEW_LOAN,
    /** Recording a repayment on an existing friend loan. */
    REPAYMENT
}
