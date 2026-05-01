package com.consulthub.legacy.admin;

import com.consulthub.legacy.consultant.Consultant;

public class Admin {
	private static final Admin instance = new Admin();
	
	/**
	 * Admin is a singleton, there is only one admin allowed which controls policy configurations.
	 * <br><br>
	 * Global policy singleton instances are passed to the Admin's method configurePolicy(PolicyCommand command) during a value change.
	 * To configure a policy, pass an instance of a policy command with the policy and new value.
	 * <br><br>
	 * Example of changing cancellation policy:<br> 
	 * Admin.getInstance().configurePolicy(new CancellationPolicyCommand(CancellationPolicy.getInstance(), 5));
	 */
	private Admin() { 
		
	}
	
	public static Admin getInstance() {
		return instance;
	}
	
	/**
	 * Configure a policy using its policy command.
	 * @param command	the command for the policy that will be changed
	 */
	public void configurePolicy(PolicyCommand command) {
		command.update();
	}
	
	/**
	 * Checks if a consultant object is valid.
	 * Validity is based on the name and email String variables of the consultant,
	 * specifically if they are of the correct form and not empty/null.
	 * @param consultant	the consultant that will be reviewed
	 * @return a boolean value indicating whether the consultant was approved
	 */
	public boolean reviewConsultant(Consultant consultant) {
		if(consultant == null) {
			System.out.println("Consultant not approved");
			return false;
		}
		else if(consultant.getName().equals("") || consultant.getName() == null){
			System.out.println("Consultant not approved");
			return false;
		}
		else if(consultant.getEmail().equals("") || consultant.getEmail() == null){
			System.out.println("Consultant not approved");
			return false;
		}
		else if(!consultant.getEmail().contains("@")) {
			System.out.println("Consultant not approved");
			return false;
		}
		else if(consultant.getStatus().equals("") || consultant.getStatus() == null){
			System.out.println("Consultant not approved");
			return false;
		}
		else {
			System.out.println("Consultant approved");
			return true;
		}
	}

	/**
     * Approves a consultant registration after validation.
     *
     * @param consultant consultant to approve
     * @return true if approved
     */
    public boolean approveConsultantRegistration(Consultant consultant) {
        boolean approved = reviewConsultant(consultant);
        if (approved) {
            consultant.setStatus("APPROVED");
            System.out.println("Consultant approved");
        }
        return approved;
    }

    /**
     * Rejects a consultant registration.
     *
     * @param consultant consultant to reject
     */
    public void rejectConsultantRegistration(Consultant consultant) {
        if (consultant == null) {
            throw new IllegalArgumentException("Consultant cannot be null.");
        }
        consultant.setStatus("REJECTED");
        System.out.println("Consultant rejected");
    }
	
}




