package com.consulthub.legacy.admin;

public class CancellationPolicyCommand implements PolicyCommand {
	private CancellationPolicy policy;
	private int newWindowTime;
	
	
	public CancellationPolicyCommand(CancellationPolicy policy, int newWindowTime) {
        this.policy = policy;
        this.newWindowTime = newWindowTime;
    }
	
	@Override
	public boolean update() {
		 policy.applyTimeWindowChange(newWindowTime);
	     return true;
	}
}


