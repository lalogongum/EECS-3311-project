package com.consulthub.legacy.admin;
public interface PolicyCommand {
	/**
	 * Command interface which allows the invocation of unique policy commands
	 * that allow the policies to be updated. Uses the command software design pattern.
	 * @return a boolean value indicating a successful command call
	 */
	public boolean update();
}
