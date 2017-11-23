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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
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
	private int tenderType = 1; // Defaults to cash. 1 = Cash, 2 = Debit, 3 = Credit

	/**
	 * The main constructor. Will register itself as listener for all relevant
	 * components of the vending machine it is passed.
	 * 
	 * @param vend
	 *            The VendingMachine object to control
	 */
	public VendingLogic(VendingMachine vending) {
		setupLogger();
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
		exactChangeLight(true);
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
		coordinateDisplay();
		eventLog.info("valid: " + coin.getValue() + " coin entered");
	}

	/**
	 * Method to handle insertion of invalid coins
	 */
	@Override
	public void coinRejected(CoinSlot slot, Coin coin) {
		// TODO Auto-generated method stub
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

	/**
	 * Setter for the credit
	 */
	private void setCredit(int newCredit) {

		credit = newCredit;
	}
	
	/**
	 * Getter for the tender type
	 */
	public int getTenderType() {
		return tenderType;
	}



	/**
	 * Setter for the credit
	 */
	public void setTenderType(int newTender) {
		tenderType = newTender;
	}

	/**
	 * Method to check if exact change is possible
	 * Assumes that Canadian coins are used for the vending machine
	 * 
	 * @return - boolean saying whether or not exact change is possible
	 */
	public boolean exactChangePossible(){
		//Determine how many total coins are in the racks
		int totalCoins = 0;
		for (int i = 0; i < vend.getNumberOfCoinRacks(); i++)
			totalCoins += vend.getCoinRack(i).size();
		//Need a boolean to see if each type of pop can be purchased
		boolean possible = true;
		int remainingCredit = 0;
		outerloop: for (int i = vend.getNumberOfPopCanRacks() - 1; i >= 0; i--) {
			int counter = 0;
			//Set up dummy coin racks to make calculations without affecting real racks
			CoinRack[] fakeRacks = new CoinRack[vend.getNumberOfCoinRacks()];
			Map<Integer, CoinChannel> coinRackChannels = new HashMap<Integer, CoinChannel>();
			int [] coinTypes = new int[] {5, 10, 25, 100, 200};
			for(int k = 0; k < vend.getNumberOfCoinRacks(); k++) {
			    fakeRacks[k] = new CoinRack(15);
			    fakeRacks[k].connect(new CoinChannel(vend.getCoinReturn()));
			    coinRackChannels.put(new Integer(coinTypes[k]), new CoinChannel(fakeRacks[k]));
			}
			//Need the remaining credit after a purchase of each kind
			remainingCredit = credit - vend.getPopKindCost(i);
			int totalCoinsCounter = 0;
			loop: while (remainingCredit > 0) {
				for (int j = vend.getNumberOfCoinRacks() - 1; j >= 0; j--) {
					if (remainingCredit >= vend.getCoinKindForCoinRack(j) && fakeRacks[j].size() != 0) {
						remainingCredit -= vend.getCoinKindForCoinRack(j);
						try {
							fakeRacks[j].releaseCoin();
						} catch (CapacityExceededException e) {
						} catch (EmptyException e) {
						} catch (DisabledException e) {
						}
						if (counter == fakeRacks[j].size()) {
							break loop;
						}
					}
					counter++;
					totalCoinsCounter++;
					if (totalCoinsCounter == totalCoins)
						break outerloop;
				}
			}
		}
		
		if (remainingCredit != 0) {
			possible = false;
		}
		
		if (possible == false) {
			eventLog.warning("Exact change light turned on. Credit = " + credit);
			vend.getExactChangeLight().activate();
		}
		else if (possible == true) {
			eventLog.info("exact change light turned off. Credit = " + credit);
			vend.getExactChangeLight().deactivate();
		}
		return possible;
	}

  
	/**
	 * Method to provide change after pop has been vended
	 * 
	 * @param changeDue
	 *            - the customer's remaining credit after the pop has been
	 *            purchased, and the amount which should be returned to the
	 *            customer. If exact change is not possible, as much of this
	 *            credit as possible is returned without going over.
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
						// Reduces credit by amount released
						
						if ((changeDue / typeCoin) < 1) {
							j--;
						}
					} catch (CapacityExceededException | EmptyException | DisabledException e) {
						outOfOrderLight(true);
					}
					;

				}
        
				else  {
					j--;
				}

			}

		}

		vend.getCoinReturn().unload(); // Simulates physical unloading
		exactChangeLight(exactChangePossible());

		eventLog.info(changeToReturn - changeDue + " returned to user");

		setCredit(changeDue);
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
			eventLog.info("exact change light enabled");

			vend.getExactChangeLight().activate();
		}

		else if (status == true) {
			eventLog.info("exact change light disabled");

			vend.getExactChangeLight().deactivate();
		}

	}

	
	/**
	 * Method to check whether all coin racks are full or not
	 * 
	 * @return boolean (true or false) saying whether or not coin racks are full
	 */
	public boolean fullCoinRacks() {
		// Turning on outOfOrderLight for full coinRacks

		int fullRacks = 0;
		for (int i = 0; i< vend.getNumberOfCoinRacks(); i++) {
			if (vend.getCoinRack(i).hasSpace() == false) {
				fullRacks++;
			}
		}

		if (fullRacks == (vend.getNumberOfCoinRacks())) {
			
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
		//Try to dispense a pop with cash payment
		if (tenderType == 1) {
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
									//Take payment in the form of cash
									takePayment(1, vend.getPopKindCost(i));
									// Dispense the pop can
									vend.getPopCanRack(i).dispensePopCan();
									eventLog.info("pop vended from: " + vend.getPopCanRack(i));
									break;
								} catch (CapacityExceededException e) {
									chuteFull(vend.getDeliveryChute());
								} catch (DisabledException e) {
									vend.getDisplay().display("Device disabled.");
									outOfOrderLight(true);
	
								} catch (EmptyException e) {
									vend.getDisplay().display("No pop in the rack.");
									eventLog.info("No pop to vend from: " + vend.getPopCanRack(i));
								}
							}
						} else
							vend.getDisplay().display("Not enough credit");
							eventLog.info("Not enough credit was entered for selection");
					} else if (vend.getPopCanRack(i).size() <= 0)
						vend.getDisplay().display("No pops of this type to dispense");
						eventLog.info("No pops of type: " + vend.getPopCanRack(i));
				} else if (i == vend.getNumberOfSelectionButtons() - 1)
					eventLog.warning("Invalid button selected");
			}
		}
		//Try to dispense a pop with a debit/credit card payment
		else if (tenderType == 2 || tenderType == 3) {
			//Take input for a manually entered card (for now)
			System.out.println("Enter a debit or credit card number");
			Scanner scan = new Scanner(System.in);
			int cardNumber = Integer.parseInt(scan.nextLine());
			//Verify the card number is valid
			if (verifyCardNumber(cardNumber)) {
				//Take input for a manually entered PIN
				System.out.println("Enter 4-digit PIN");
				int pin = Integer.parseInt(scan.nextLine());
				//Verify the PIN is valid for the given card
				//Only one attempt at entering PIN (for now)
				if (verifyPIN(cardNumber, pin)) {
					//Find the appropriate selection button
					for (int i = 0; i < vend.getNumberOfSelectionButtons(); i++) {
						// Make sure the button matches one from the vending machine
						if (button == vend.getSelectionButton(i)) {
							// Ensure there is a pop in the rack
							if (vend.getPopCanRack(i).size() > 0) {
								// Ensure there is enough space in the delivery chute
								if (vend.getDeliveryChute().hasSpace()) {
									//Verify there is enough balance/credit on the card to make a purchase
									//Payment by card needs to make the full purchase amount (for now)
									if(verifyBalance(cardNumber, vend.getPopKindCost(i))) {
										try {
											takePayment(tenderType, vend.getPopKindCost(i));
											// Dispense the pop can
											vend.getPopCanRack(i).dispensePopCan();
											eventLog.info("pop vended from: " + vend.getPopCanRack(i));
											break;
										} catch (CapacityExceededException e) {
											chuteFull(vend.getDeliveryChute());
										} catch (DisabledException e) {
											vend.getDisplay().display("Device disabled.");
											outOfOrderLight(true);
			
										} catch (EmptyException e) {
											vend.getDisplay().display("No pop in the rack.");
											eventLog.info("No pop to vend from: " + vend.getPopCanRack(i));
										}
									}
								}
							}
						}
					}
				}
				else {
					vend.getDisplay().display("Invalid PIN.");
					eventLog.warning("PIN" + pin + " for card " + cardNumber + " is invalid.");
				}
			}
			else {
				vend.getDisplay().display("Card Number not able to be verified.");
				eventLog.warning("Card number " + cardNumber + " not recognized.");
			}
			//scan.close();
		}
		outOfOrderLight(machineEmpty()); 
		// Check after each press if machine is empty and If empty, turn on out of order light
	}

	/**
	 * Method to verify if a credit/debit card is valid. Only checks the card number
	 * 
	 * @param cardNumber
	 * 				- The credit/debit card number to be verified
	 *              - Must match a known card number
	 **/
	private boolean verifyCardNumber(int cardNumber) {
		//Need to change this for real functionality once we know how to obtain card numbers
		if (cardNumber == 123) {
			return true;
		}
		else if (cardNumber == 321) {
			return true;
		}
		else return false;
	}

	/**
	 * Method to verify if a PIN is valid given a card number and PIN
	 * 
	 * @param cardNumber
	 * 				- The credit/debit card number to be verified
	 * 
	 * @param pin
	 * 				- The PIN entered by the user
	 *              - Must match the PIN associated with the card       
	 **/
	private boolean verifyPIN(int cardNumber, int pin) {
		//Need to change this for real functionality once we know how to obtain card numbers and PINs
				if (cardNumber == 123 && pin == 1234) {
					return true;
				}
				else if (cardNumber == 321 && pin == 4321) {
					return true;
				}
				else return false;	
	}

	/**
	 * Method to verify if there is enough balance on a card to pay for a pop.
	 * 
	 * @param cardNumber
	 * 				- The credit/debit card number to be verified
	 *              - Must match a known card number
	 *              
	 * @param cost
	 *              - The cost of the pop trying to be purchased
	 **/
	private boolean verifyBalance(int cardNumber, int cost) {
		//Need to change functionality for this method as well
		//Need to associate card balances/available credit with specific cards,
		//then check the cost against the balance we grabbed from the card.
		if (cardNumber == 123 && cost <= 100000) {
			//This card has a constant $1000 balance for now
			return true;
		}
		else if (cardNumber == 321 && cost <= 500) {
			//This card has a $5 balance
			return true;
		}
		else return false;			
	}

	/**
	 * Required method. Logs the event that an item has been delivered to
	 * the delivery chute
	 */
	@Override
	public void itemDelivered(DeliveryChute chute) {
		// TODO Auto-generated method stub
		eventLog.info("Item delivered");
	}

	/**
	 * Required method. Logs the event that the user has opened the
	 * delivery chute door
	 */
	@Override
	public void doorOpened(DeliveryChute chute) {
		// TODO Auto-generated method stub
		eventLog.info("Delivery chute door opened");
	}

	/**
	 * Required method. Logs the event that the delivery chute
	 * door has closed
	 */
	@Override
	public void doorClosed(DeliveryChute chute) {
		// TODO Auto-generated method stub
		eventLog.info("Delivery chute door closed");
	}

	/**
	 * Required method. Logs the event that the delivery chute is full
	 */
	@Override
	public void chuteFull(DeliveryChute chute) {
		eventLog.warning("Delivery chute full");
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
	
	/**
	 * Method to take payment. Can add more functionality to this (Bitcoin) if required
	 * 
	 * @param paymentType
	 *            - Corresponds to the available payment options.
	 *            - 1 = Cash
	 *            - 2 = Debit
	 *            - 3 = Credit
	 *            
	 * @param cost
	 *            - The cost of the pop
	 * 
	 */
	public void takePayment(int paymentType, int cost) {
		if (paymentType == 1) {
			credit -= cost;
			provideChange(credit);
			coordinateDisplay();
		}
		else if (paymentType == 2) {
			//Need to decrease the remaining balance of the debit card here (and display it)
			vend.getDisplay().display("Payment taken successfully. Remaining balance: ");
			eventLog.info("Debit purchase made for $" + cost);
		}
		else if (paymentType == 3) {
			//Need to decrease the available credit of the credit card here (and display it)
			vend.getDisplay().display("Payment taken successfully. Remaining balance: ");
			eventLog.info("Credit purchase made for $" + cost);
		}
	}

} // end class
