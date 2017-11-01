/**
 * Logic class for the Vending Machine
 * SENG 300 Group Assignment 2
 * 
 * Stephen Pollo
 * Simran Rai
 * Roman Sklyar
 * Shawn Sangha
 * Anthony Coulthard
 * Emilie Guidos
 */

package ca.ucalgary.seng300.a2;

import org.lsmr.vending.*;
import org.lsmr.vending.hardware.*;

public class VendingLogic {

	private VendingMachine vend;
	
	/**
	 * The main constructor. Will register itself as listener for
	 * all relevant components of the vending machine it is passed.
	 * @param vend The VendingMachine object to control
	 */
	public VendingLogic(VendingMachine vending) {
		vend = vending;
		
	}
}
