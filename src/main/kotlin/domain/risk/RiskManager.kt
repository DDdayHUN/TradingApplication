package domain.risk

//===========================================================//

/**
 * Applies basic risk management rules to algorithm-generated trading decisions.
 * 
 * 
 * The RiskManager does not create buy or sell signals. It receives the
 * output of an algorithm and adjusts it to make sure it stays within safe
 * limits.
 * 
 */
//===========================================================//
class RiskManager 
