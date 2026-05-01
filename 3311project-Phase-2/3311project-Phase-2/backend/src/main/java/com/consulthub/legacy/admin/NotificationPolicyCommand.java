package com.consulthub.legacy.admin;
public class NotificationPolicyCommand implements PolicyCommand {
	private NotificationPolicy policy;
	private boolean newEnableStatus;
	
	public NotificationPolicyCommand(NotificationPolicy policy, boolean enableStatus) {
	    this.policy = policy;
	    this.newEnableStatus = enableStatus;
	}
	
	@Override
	public boolean update() {
		policy.applyChange(newEnableStatus);
		return true;
	}
}
