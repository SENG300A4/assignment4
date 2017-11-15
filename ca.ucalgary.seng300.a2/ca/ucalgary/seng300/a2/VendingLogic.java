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
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class VendingLogic implements CoinSlotListener, DisplayListener, PushButtonListener, DeliveryChuteListener {

	private VendingMachine vend;
	private int credit;
	private Timer timer;
	private int timerCycles;
	private String displayMessage;
	private Logger eventLog = Logger.getLogger("Event-Log");
	private FileHandler fh;
	private boolean changeLight = false;

	/**
	 * The main constructor. Will register itself as listener for all relevant
	 * components of the vending machine it is passed.
	 * 
	 * @param vend
	 *            The VendingMachine object to control
	 */
	public VendingLogic(VendingMachine vending) {
		vend = vending;
		
		// Register as listener with relevant components
		vend.getCoinSlot().register(this);
		vend.getDisplay().register(this);
		vend.getDeliveryChute().register(this);
		for (int i = 0; i < vend.getNumberOfSelectionButtons(); i++) {
			vend.getSelectionButton(i).register(this);
		}

		credit = 0;
		coordinateDisplay();
		outOfOrderLight(machineEmpty());
		exactChangeLight(exactChangePossible());
	}

	/**
	 * Method to handle what the display should be displaying. Should be called
	 * whenever the value of credit is updated.
	 */
	public void coordinateDisplay() {
		timer = new Timer();
		if (credit == 0) {
			timerCycles = 0;
			try {
				timer.schedule(timerResponder, 0, 5000);
			} catch (IllegalStateException e) {
				// Do nothing
			};
		} else {
			if (timer != null) {
				timer.cancel();
			}
			timer = null;
			displayWithCredit();
		}

	}

	/**
	 * Method to handle the display message when the user has no credit in the
	 * machine
	 */
	private void displayNoCredit() {
		eventLog.info("No Credit, display welcome message");
		vend.getDisplay().display("Hi there!");
	}

	/**
	 * Method to handle the display message when the user has credit in the
	 * machine.
	 */
	private void displayWithCredit() {
		eventLog.info("Credit displayed, current credit: " + credit);
		vend.getDisplay().display("Credit: " + credit);
	}

	/**
	 * Task that responds to the timer events for the display.
	 * Coordinates the 5 seconds of displaying the welcome message,
	 * followed by 10 seconds of displaying nothing.
	 */
	TimerTask timerResponder = new TimerTask() {

		@Override
		public void run() {
			if ((timerCycles % 3) == 0) {
				displayNoCredit();
				timerCycles++;
			} else if ((timerCycles % 3) == 1) {
				vend.getDisplay().display("");
				timerCycles++;
			} else if ((timerCycles % 3) == 2) {
				timerCycles = 0;
			}

		}
	};

	/**
	 * Required method. Event is logged for the hardware that is being listened to
	 */
	@Override
	public void enabled(AbstractHardware<? extends AbstractHardwareListener> hardware) {
		eventLog.info(hardware + " enabled");
	}

	/**
	 * Required method. Event is logged for the hardware that is being listened to
	 */
	@Override
	public void disabled(AbstractHardware<? extends AbstractHardwareListener> hardware) {
		eventLog.info(hardware + " disabled");
	}

	/**
	 * Method to handle insertion of valid coins.
	 * Updates user credit to reflect payment of valid coins.
	 */
	@Override
	public void validCoinInserted(CoinSlot slot, Coin coin) {
		credit += coin.getValue();
		exactChangeLight(exactChangePossible());
		coordinateDisplay();
		eventLog.info("valid: " + coin.getValue() + " coin entered");
	}

	/**
	 * Method to handle insertion of invalid coins
	 */
	@Override
	public void coinRejected(CoinSlot slot, Coin coin) {
		eventLog.info("Coin rejected");
	}

	/**
	 * Getter for the current display message
	 */
	public String getDisplayMessage() {
		return displayMessage;
	}

	/**
	 * Getter for the credit
	 */
	public int getCredit() {
		return credit;
	}

	//TODO 
	/**
	 * Setter for the credit
	 */
	public void setCredit(int newCredit) {
		credit = newCredit;
	}

	/**
	 * Getter for the change light
	 */
	public boolean getChangeLight() {
		return changeLight;
	}
	
	/**
	 * Setter for the change light
	 */
	public void setChangeLight(boolean light) {
		changeLight = light;
	}

	/**
	 * Method to check if exact change is possible
	 * Assumes that Canadian coins are used for the vending machine
	 * 
	 * @return possible - boolean saying whether or not exact change is possible
	 */
	public boolean exactChangePossible() {
		//Need 5 booleans to see if each type of pop can be purchased
		boolean possible0 = true;
		boolean possible1 = true;
		boolean possible2 = true;
		boolean possible3 = true;
		boolean possible4 = true;
		//Also need the remaining credit after a purchase of each kind
		int remainingCredit0 = credit - vend.getPopKindCost(0);
		int remainingCredit1 = credit - vend.getPopKindCost(1);
		int remainingCredit2 = credit - vend.getPopKindCost(2);
		int remainingCredit3 = credit - vend.getPopKindCost(3);
		int remainingCredit4 = credit - vend.getPopKindCost(4);
		//Determine how many total coins are in the racks
		int totalCoins = 0;
		for (int i = 0; i < vend.getNumberOfCoinRacks(); i++)
			totalCoins += vend.getCoinRack(i).size();
		//Set up an array containing all coins in the vending machine's rack
		int [] allCoins = new int [totalCoins];
		//Fill the array with the value of each coin
		//Also determine the maximum change that can be returned
		int counter = 0;
		for (int i = 0; i < vend.getNumberOfCoinRacks(); i++) {
				for (int j = 0; j < vend.getCoinRack(i).size(); j++) {
					allCoins[counter] = vend.getCoinKindForCoinRack(i);
					counter++;
				}
		}
		//Populate all possible combinations of coins and check if they match what needs to be given back as change
		//Used the following website for guidance on creating a powerset
		//https://stackoverflow.com/questions/4640034/calculating-all-of-the-subsets-of-a-set-of-numbers
		for(long i = 0; i < (1<<totalCoins); i++){
			int sum = 0;
		    for(int j = 0; j < totalCoins; j++){
		        if(((i>>j) & 1) == 1){ // bit j is on
		            sum += allCoins[j];
		        }
		    }
		  //Check each possible combination against the user's remaining credit to see if exact change can be given
		    if (remainingCredit0 == sum)
		    	possible0 = false;
		    if (remainingCredit1 == sum)
		    	possible1 = false;
		    if (remainingCredit2 == sum)
		    	possible2 = false;
		    if (remainingCredit3 == sum)
		    	possible3 = false;
		    if (remainingCredit4 == sum)
		    	possible4 = false;
		}
		
		if (possible0 == true || possible1 == true || possible2 == true || possible3 == true || possible4 == true) {
			eventLog.warning("Exact change light turned on. Credit = " + credit);
			messageChange(vend.getDisplay(), displayMessage, "Exact change no longer guaranteed.");
			changeLight = true;
			return true;
		}
		changeLight = false;
		return false;
	}

	//TODO @return vs void
	/**
	 * Method to provide change after pop has been vended
	 * 
	 * @param changeDue
	 *            - the customer's remaining credit after the pop has been
	 *            purchased, and the amount which should be returned to the
	 *            customer. If exact change is not possible, as much of this
	 *            credit as possible is returned without going over.
	 * @return Remaining credit (should be zero if exact change is returned)
	 * 
	 */
	public void provideChange(int changeDue) {
		int changeToReturn = changeDue;
		int numCoins = vend.getNumberOfCoinRacks();
		int typeCoin;
		Coin returnCoin;
		int j = numCoins - 1;

		if (changeDue != 0) {
			while (j > -1) {
				typeCoin = vend.getCoinKindForCoinRack(j); 
				// Coin Kinds are initialized when machine is set up, 
				// assuming they are standard Canadian denominations here
				
				if (((changeDue / typeCoin) >= 1) && vend.getCoinRackForCoinKind(typeCoin).size() != 0) {
					try {
						vend.getCoinRackForCoinKind(typeCoin).releaseCoin(); 
						// Releases specific coin from coin rack
						returnCoin = new Coin(typeCoin); 
						// Coin of the type released						
						eventLog.info(typeCoin + " coin returned to user");
						vend.getCoinReturn().acceptCoin(returnCoin); 
						// Adds coin to coin return						
						changeDue = changeDue - typeCoin; 
						// Reduces change to give back by amount released
						
						//Reduces credit by amount of returned coin and displays the new value
						credit -= typeCoin;
						if (credit == 0)
							displayNoCredit();
						else 
							coordinateDisplay();
						
						if ((changeDue / typeCoin) < 1) {
							j--;
						}
					} catch (CapacityExceededException | EmptyException | DisabledException e) {
						outOfOrderLight(true);
					}
				}
				else if ((changeDue / typeCoin) < 1) {
					j--;
				}
			}
		}
		vend.getCoinReturn().unload(); // Simulates physical unloading
		exactChangeLight(exactChangePossible());
		eventLog.info(changeToReturn - changeDue + " returned to user");
	}

	/**
	 * Method to control "Exact Change Only" light, which turns on when exact
	 * change cannot be guaranteed for all possible transactions
	 * 
	 * @param status
	 *            - whether or not the exact change only light needs to be
	 *            turned on due to not being able to guarantee that exact change
	 *            can be returned.
	 */

	public void exactChangeLight(boolean status) {
		if (status == false) {
			eventLog.info("exact change light disabled");
			vend.getExactChangeLight().deactivate();
		}

		else if (status == true) {
			eventLog.info("exact change light enabled");
			vend.getExactChangeLight().activate();
		}

	}

	/**
	 * Method to check whether all coin racks are full or not
	 * 
	 * @return boolean (true or false) saying whether or not coin racks are full
	 */
	public boolean fullCoinRacks() {
		// Turning on outOfOrderLight for full coinRacks
		int i = 0;
		int fullRacks = 0;
		while (i < vend.getNumberOfCoinRacks()) {
			if (vend.getCoinRack(i).hasSpace() == false) {
				fullRacks++;
			}
		}

		if (fullRacks == (vend.getNumberOfCoinRacks() - 1)) {
			return true;
		}

		else {
			return false;
		}

	}

	/**
	 * Method to check if vending machine is completely empty
	 * 
	 * @return emptyStatus - true if empty, false if not empty
	 * 
	 */
	public boolean machineEmpty() {
		int emptyPopRacks = 0;
		boolean emptyStatus = false;
		for (int i = 0; i < vend.getNumberOfSelectionButtons(); i++) {
			if (vend.getPopCanRack(i).size() == 0) {
				emptyPopRacks++;
			}

		}

		if (emptyPopRacks == vend.getNumberOfPopCanRacks()) {
			emptyStatus = true;
		}

		return emptyStatus;
	}

	/**
	 * Method to control "Out of Order" light, which turns on when: - Machine
	 * cannot store additional coins - Machine becomes aware of problem that
	 * cannot be recovered from (including being out of pop) - Safety is enabled
	 * (already happens in hardware, don't need to add here)
	 * 
	 * @param status
	 *            - whether or not the Out of Order light needs to be turned
	 *            on, due to some sort of issue that renders the machine
	 *            unusable.
	 */

	public void outOfOrderLight(boolean status) {

		if (status == true) {
			eventLog.info("out of order light activated");
			vend.getOutOfOrderLight().activate();
		}

		else if (status == false) {
			eventLog.info("out of order light deactivated");
			vend.getOutOfOrderLight().deactivate();
		}

	}

	/**
	 * Method to listen to changes in display messages
	 * 
	 * @param display
	 *            - the device on which the event occurred
	 * @param oldMessage
	 *            - previous message displayed
	 * @param newMessage
	 *            - new message to display
	 */
	@Override
	public void messageChange(Display display, String oldMessage, String newMessage) {
		displayMessage = newMessage;
	}

	/**
	 * Method to listen for button presses and respond with appropriate logic
	 * (Is there enough pop in the rack, enough credits, etc.)
	 * 
	 * @param button
	 *            - the button which has been pressed by the user
	 */
	@Override
	public void pressed(PushButton button) {
		for (int i = 0; i < vend.getNumberOfSelectionButtons(); i++) {
			// Make sure the button matches one from the vending machine
			if (button == vend.getSelectionButton(i)) {
				// Ensure there is a pop in the rack
				if (vend.getPopCanRack(i).size() > 0) {
					// Ensure there is enough credit to purchase the pop
					if (vend.getPopKindCost(i) <= credit) {
						// Ensure there is enough space in the delivery chute
						if (vend.getDeliveryChute().hasSpace()) {
							try {
								// Dispense the pop can
								vend.getPopCanRack(i).dispensePopCan();
								credit -= vend.getPopKindCost(i);
								if (credit == 0)
									displayNoCredit();
								else 
									coordinateDisplay();
								provideChange(credit);
								eventLog.info("pop vended from:" + vend.getPopCanRack(i));
								break;
							} catch (CapacityExceededException e) {
								chuteFull(vend.getDeliveryChute());
							} catch (DisabledException e) {
								messageChange(vend.getDisplay(), displayMessage, "Device disabled.");
								outOfOrderLight(true);
							} catch (EmptyException e) {
								messageChange(vend.getDisplay(), displayMessage, "No pop in the rack.");
							}
						}
					} else
						messageChange(vend.getDisplay(), displayMessage, "Not enough credit.");
				} else if (vend.getPopCanRack(i).size() <= 0)
					messageChange(vend.getDisplay(), displayMessage, "No pops of this type to dispense.");
			} else if (i == vend.getNumberOfSelectionButtons() - 1)
				messageChange(vend.getDisplay(), displayMessage, "Invalid button selected.");
		}
		outOfOrderLight(machineEmpty()); 
		// Check after each press if machine is empty and If empty, turn on out of order light
	}

	/**
	 * Required method. Logs the event that an item has been delivered to
	 * the delivery chute
	 */
	@Override
	public void itemDelivered(DeliveryChute chute) {
		eventLog.info("Item delivered");
	}

	/**
	 * Required method. Logs the event that the user has opened the
	 * delivery chute door
	 */
	@Override
	public void doorOpened(DeliveryChute chute) {
		eventLog.info("Delivery chute door opened");
	}

	/**
	 * Required method. Logs the event that the delivery chute
	 * door has closed
	 */
	@Override
	public void doorClosed(DeliveryChute chute) {
		eventLog.info("Delivery chute door closed");
	}

	/**
	 * Required method. Logs the event that the delivery chute is full and displays the message to user
	 */
	@Override
	public void chuteFull(DeliveryChute chute) {
		eventLog.warning("Delivery chute full");
		messageChange(vend.getDisplay(), displayMessage, "Delivery chute full.");
	}

	/**
	 * This method implements the setup logic needed to be able to call the 
	 * event logger
	 */
	public void setupLogger() {
		try {
			// This block configures the logger with handler and formatter
			fh = new FileHandler("EventLogREW.txt", 20000, 1, true);
			eventLog.addHandler(fh);
			eventLog.setUseParentHandlers(false);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

} // end class
