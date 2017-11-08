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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class VendingLogic {

	private VendingMachine vend;
	private int credit;
	private Timer timer;
	private int timerCycles;
	
	/**
	 * The main constructor. Will register itself as listener for
	 * all relevant components of the vending machine it is passed.
	 * @param vend The VendingMachine object to control
	 */
	public VendingLogic(VendingMachine vending) {
		vend = vending;
		// Register as listener with relevant components
		
		credit = 0;
		timer = new Timer();
	}
	
	/**
	 * Method to handle what the display should be displaying.
	 * Should be called whenever the value of credit is updated.
	 */
	public void coordinateDisplay() {
		if (credit == 0) {
			timerCycles = 0;
			timer.schedule(timerResponder, 5000);
		}
		else {
			displayWithCredit();
		}
		
	}
	
	/**
	 * Method to handle the display message when the user has no credit in the machine
	 */
	public void displayNoCredit() {
		vend.getDisplay().display("Hi there!");
	}
	
	/**
	 * Method to handle the display message when the user has credit in the machine.
	 */
	public void displayWithCredit() {
		vend.getDisplay().display("Credit: " + credit);
	}
	
	/**
	 * Task that responds to the timer events for the display.
	 */
	TimerTask timerResponder = new TimerTask() {

		@Override
		public void run() {
			if ((timerCycles % 3) == 0) {
				displayNoCredit();
				timerCycles++;
			}
			else if ((timerCycles % 3) == 1) {
				vend.getDisplay().display("");
				timerCycles++;
			}
			else if ((timerCycles % 3) == 2) {
				timerCycles = 0;
			}
			
		}
	};
	
} // end class
