package com.consulthub.legacy.admin;

public class RefundPolicy {
	private String acceptedState;
	
	private static final RefundPolicy instance = new RefundPolicy();
	
	private RefundPolicy() {
		this.acceptedState = "SUCCESSFUL";
	}
	
	public static RefundPolicy getInstance() {
		return instance;
	}
	
	/**
	 * Get the refund window time in days the policy enforces.
	 * @return the String value representing the number of days the refund window lasts
	 */
	public String getRefundPolicy() {
		return this.acceptedState;
	}
	
	/**
	 * Change the value of this policy with the passed parameter
	 * @param state 	The String value that the policy's variable will be set to
	 */
	private void change(String state) {
		this.acceptedState = state;
		System.out.println("Refund Policy changed.");
	}
	
	/**
	 * Invoke the method to change this policy's variable with the given parameter
	 * @param state 	The String value that the policy's variable will be set to
	 */
	public void applyChange(String state) {
		change(state);
	}
}



