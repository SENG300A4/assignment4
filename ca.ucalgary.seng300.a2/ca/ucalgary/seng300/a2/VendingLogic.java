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

public class VendingLogic implements CoinSlotListener, DisplayListener{

	private VendingMachine vend;
	private int credit;
	private Timer timer;
	private int timerCycles;
	private String displayMessage;
	
	/**
	 * The main constructor. Will register itself as listener for
	 * all relevant components of the vending machine it is passed.
	 * @param vend The VendingMachine object to control
	 */
	public VendingLogic(VendingMachine vending) {
		vend = vending;
		// Register as listener with relevant components
		vend.getCoinSlot().register(this);
		vend.getDisplay().register(this);
		credit = 0;
		timer = new Timer();
		coordinateDisplay();
	}

	
	/**
	 * Method to handle what the display should be displaying.
	 * Should be called whenever the value of credit is updated.
	 */
	public void coordinateDisplay() {
		if (credit == 0) {
			timerCycles = 0;
			timer.schedule(timerResponder, 0, 5000);
		}
		else {
			timer.cancel();
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
			}
			else if ((timerCycles % 3) == 2) {
				timerCycles = 0;
			}
			
		}
	};

	@Override
	public void enabled(AbstractHardware<? extends AbstractHardwareListener> hardware) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disabled(AbstractHardware<? extends AbstractHardwareListener> hardware) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Method to listen to the Coin slot events
	 */
	@Override
	public void validCoinInserted(CoinSlot slot, Coin coin) {
		
		// TODO Auto-generated method stub
		//System.out.println("added coin");
		credit+=coin.getValue();
		coordinateDisplay();
		
	}

	@Override
	public void coinRejected(CoinSlot slot, Coin coin) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Getter for the current display message
	 */
	public String getDisplayMessage() {
		return displayMessage;
	}
	
	
	/**
	 * Method to listen to changes in display messages
	 * @param display - the device on which the event occurred 
	 * @param oldMessage - previous message displayed
	 * @param newMessage - new message to display
	 */
	@Override
	public void messageChange(Display display, String oldMessage, String newMessage) {
		// TODO Auto-generated method stub
		//System.out.println(oldMessage);
		//System.out.println("Message change: " + newMessage);
		displayMessage = newMessage;
	}
	
} // end class
