package com.consulthub.legacy.admin;

public class PricingPolicyCommand implements PolicyCommand {
	private PricingPolicy policy;
	private double newMinPrice;
	private double newMaxPrice;
	
	public PricingPolicyCommand(PricingPolicy policy, double minPrice, double maxPrice) {
		this.policy = policy;
		this.newMinPrice = minPrice;
		this.newMaxPrice = maxPrice;
	}
	
	@Override
	public boolean update() {
		policy.applyChange(newMinPrice, newMaxPrice);
		return true;
	}
}
