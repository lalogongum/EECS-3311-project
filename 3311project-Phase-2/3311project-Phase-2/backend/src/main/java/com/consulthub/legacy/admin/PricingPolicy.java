package com.consulthub.legacy.admin;

public class PricingPolicy {
	private double minPrice;
	private double maxPrice;
		
	private static final PricingPolicy instance = new PricingPolicy();
	
	private PricingPolicy() {
		this.minPrice = 0;
		this.maxPrice = 1000;
	}
	
	public static PricingPolicy getInstance() {
		return instance;
	}
	
	/**
	 * Get the minimum price value thats allowed to be set.
	 * @return the double representing the minimum price.
	 */
	public double getMinPrice() {
		return this.minPrice;
	}
	/**
	 * Get the maximum price value thats allowed to be set.
	 * @return the double representing the maximum price.
	 */
	public double getMaxPrice() {
		return this.maxPrice;
	}
	
	/**
	 * Change the value of this policy with the passed parameter
	 * @param newMin 	The double value representing the new minimum price
	 * @param newMax	The double value representing the new maximum price
	 */
	private void change(double newMin, double newMax) {
		this.minPrice = newMin;
		this.maxPrice = newMax;
		System.out.println("Pricing Policy changed. Minimum price: $" + newMin + ". Maximum price: $" + newMax + ".");
	}
	/**
	 * Invoke the method to change this policy's variable with the given parameter
	 * @param newMin 	The double value representing the new minimum price
	 * @param newMax	The double value representing the new maximum price
	 */
	public void applyChange(double newMin, double newMax) {
		change(newMin, newMax);
	}
}

