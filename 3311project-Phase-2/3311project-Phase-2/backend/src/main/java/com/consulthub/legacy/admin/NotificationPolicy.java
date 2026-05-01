package com.consulthub.legacy.admin;
public class NotificationPolicy {
	private boolean enableNotifications;
	
	private static final NotificationPolicy instance = new NotificationPolicy();
	
	private NotificationPolicy() {
		this.enableNotifications = true;
	}
	
	public static NotificationPolicy getInstance() {
		return instance;
	}
	
	/**
	 * Return whether notifications are enabled or not. False is off, true is on.
	 * @return the boolean status of the policy.
	 */
	public boolean getNotificationPolicy() {
		return this.enableNotifications;
	}
	
	/**
	 * Change the value of this policy with the passed parameter
	 * @param enable 	The boolean value that the policy's variable will be set to
	 */
	private void change(boolean enable) {
		this.enableNotifications = enable;
		System.out.println("Notification Policy changed.");
	}
	
	/**
	 * Invoke the method to change this policy's variable with the given parameter
	 * @param enable 	The boolean value that the policy's variable will be set to
	 */
	public void applyChange(boolean enable) {
		change(enable);
	}
}

