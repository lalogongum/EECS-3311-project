package com.consulthub.legacy.admin;

public class RefundPolicyCommand implements PolicyCommand{
	private RefundPolicy policy;
	private String newState;
	
	public RefundPolicyCommand(RefundPolicy policy, String acceptedState) {
		this.policy = policy;
		this.newState = acceptedState;
	}
	
	@Override
	public boolean update() {
		policy.applyChange(newState);
		return true;
	}
}

