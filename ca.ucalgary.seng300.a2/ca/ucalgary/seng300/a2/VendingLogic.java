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

public class VendingLogic implements CoinSlotListener, DisplayListener, PushButtonListener, DeliveryChuteListener{

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
		vend.getDeliveryChute().register(this);
		for (int i = 0; i < vend.getNumberOfSelectionButtons(); i++) {
			vend.getSelectionButton(i).register(this);
		}
		credit = 0;
		coordinateDisplay();
	}

	
	/**
	 * Method to handle what the display should be displaying.
	 * Should be called whenever the value of credit is updated.
	 */
	public void coordinateDisplay() {
		timer = new Timer();
		if (credit == 0) {
			timerCycles = 0;
			try {
				timer.schedule(timerResponder, 0, 5000);
			}
			catch (IllegalStateException e) {
				
			};
		}
		else {
			if (timer != null) {
				timer.cancel();
			}
			timer = null;
			displayWithCredit();
		}
		
	}
	
	/**
	 * Method to handle the display message when the user has no credit in the machine
	 */
	private void displayNoCredit() {
		vend.getDisplay().display("Hi there!");
	}
	
	/**
	 * Method to handle the display message when the user has credit in the machine.
	 */
	private void displayWithCredit() {
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
	 * Getter for the credit
	 */
	public int getCredit() {
		return credit;
	}
	
	/**
	 * Setter for the credit
	 */
	
	public void setCredit(int newCredit)
	{
		credit = newCredit;
	}
	

	/**
	 * Method to check if exact change may not be possible
	 * @return possible -- boolean saying whether or not exact change is possible
	 */
	public boolean exactChangePossible()
	{
		boolean possible = true;
		//Checking for coin levels of each rack or at least one empty rack. 
		//If any is below threshold of 5, exact change may not be possible 
		
		boolean emptyRack = false;
		boolean underFive = false;
		
		for (int i = 0; i < vend.getNumberOfCoinRacks(); i++) {
			if (vend.getCoinRack(i).size() == 0)
			{
				emptyRack = true;
			}
			else if(vend.getCoinRack(i).size() < 5)
			{
				underFive = true;
			}
		}	
		
		if (emptyRack == true || underFive == true)
		{
			possible = false;
		}
		
		return possible;
	}
	
	/**
	 * Method to provide change after pop has been vended
	 * @param changeDue -- the customer's remaining credit after the pop has been purchased, and the amount which should be returned to the customer. If exact change is not possible, as much of this credit as possible is returned without going over.
	 * @return Remaining credit (should be zero if exact change is returned)
	 * 
	 */
	public void provideChange(int changeDue)
	{
		int numCoins = vend.getNumberOfCoinRacks();
		int typeCoin;
		Coin returnCoin;
		int j = numCoins - 1;
		
		if (changeDue != 0)
		{
			while (j > -1)
			{
				typeCoin = vend.getCoinKindForCoinRack(j); //Coin Kinds are initialized when machine is set up, assuming they are standard Canadian denominations here
				if (((changeDue/typeCoin) >= 1) && vend.getCoinRackForCoinKind(typeCoin).size() != 0)
				{
					try
					{
						vend.getCoinRackForCoinKind(typeCoin).releaseCoin(); //Releases specific coin from coin rack
						returnCoin = new Coin(typeCoin); //Coin of the type released
						vend.getCoinReturn().acceptCoin(returnCoin); //Adds coin to coin return
						changeDue = changeDue - typeCoin; //Reduces credit by amount released
						if((changeDue/typeCoin) < 1)
						{
							j--;
						}
					}
					catch (CapacityExceededException|EmptyException|DisabledException e)
					{
						outOfOrderLight(true);
					};

				}
				
				else if ((changeDue/typeCoin) < 1)
				{
					j--;
				}
				
			}
			
		}

		vend.getCoinReturn().unload(); //Simulates physical unloading 
		exactChangeLight(exactChangePossible());
		setCredit(changeDue);
	}
	
	
	
	/**
	 * Method to control "Exact Change Only" light, which turns on when exact change cannot be guaranteed 
	 * for all possible transactions
	 * @param status -- whether or not the exact change only light needs to be turned on due to not being able to guarantee that exact change can be returned.
	 */
	
	public void exactChangeLight(boolean status)
	{
		if(status == false)
		{
			vend.getExactChangeLight().activate();
		}
		
		else if(status == true)
		{
			vend.getExactChangeLight().deactivate();
		}

	}
	
	
	/**
	 * Method to check whether coin racks are full or not
	 * @return boolean (true or false) saying whether or not coin racks are full
	 */
	public boolean fullCoinRacks()
	{
		//Turning on outOfOrderLight for full coinRacks
		int i = 0;
		int fullRacks = 0;
		while (i < vend.getNumberOfCoinRacks())
		{
			if (vend.getCoinRack(i).hasSpace() == false)
			{
				fullRacks++;
			}
		}
			
		if (fullRacks == (vend.getNumberOfCoinRacks() -1))
		{
			return true;
		}
			
		
		else
		{
			return false;
		}
				
	}
	
	
	/**
	 * Method to check if vending machine is completely empty
	 * @return emptyStatus -- true if empty, false if not empty
	 * 
	 */
	
	public boolean machineEmpty()
	{
		int emptyPopRacks = 0;
		boolean emptyStatus = false;
		for (int i = 0; i < vend.getNumberOfSelectionButtons(); i++) 
		{
			if (vend.getPopCanRack(i).size() == 0) {
				emptyPopRacks++;
			}
			
		}
		
		if(emptyPopRacks == vend.getNumberOfPopCanRacks())
		{
			emptyStatus = true;
		}
		
		return emptyStatus;
	}
	
	/**
	 * Method to control "Out of Order" light, which turns on when:
	 * - Machine cannot store additional coins
	 * - Machine becomes aware of problem that cannot be recovered from (including being out of pop)
	 * - Safety is enabled (already happens in hardware, don't need to add here)
	 * 
	 * @param status -- whether or not the Out of Order light needs to be turned on, due to some sort of issue that renders the machine unusable.
	 */
	
	public void outOfOrderLight(boolean status)
	{
		
		if (status == true)
		{
			vend.getOutOfOrderLight().activate();
		}
		
	
		
		else if (status == false)
		{
			vend.getOutOfOrderLight().deactivate();
		}
		

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


	/**
	 * Method to listen for button presses and respond with appropriate logic
	 * (Is there enough pop in the rack, enough credits, etc.)
	 * @param button - the button which has been pressed by the user
	 */
	@Override
	public void pressed(PushButton button) {
		for (int i = 0; i < vend.getNumberOfSelectionButtons(); i++) {
			//Make sure the button matches one from the vending machine
			if (button == vend.getSelectionButton(i)) {
				//Ensure there is a pop in the rack
				if (vend.getPopCanRack(i).size() > 0) {
					//Ensure there is enough credit to purchase the pop
					if (vend.getPopKindCost(i) <= credit) {
						//Ensure there is enough space in the delivery chute
						if (vend.getDeliveryChute().hasSpace()) {
							try {
								//Dispense the pop can
								vend.getPopCanRack(i).dispensePopCan();
								credit -= vend.getPopKindCost(i);
								provideChange(credit);
								coordinateDisplay();
								break;
							} catch (CapacityExceededException e) {
								chuteFull(vend.getDeliveryChute());
							} catch (DisabledException e) {
								System.out.println("Device disabled.");
								outOfOrderLight(true);

							} catch (EmptyException e) {
								System.out.println("No pop in the rack.");
							}
						}
					}
					else
						System.out.println("Not enough credit.");
				}
				else if (vend.getPopCanRack(i).size() <= 0)
					System.out.println("No pops of this type to dispense");
			}
			else if (i == vend.getNumberOfSelectionButtons() - 1)
				System.out.println("Invalid button selected.");
		}
		
		outOfOrderLight(machineEmpty()); //Check after each press if machine is empty and If empty, turn on out of order light
	}


	@Override
	public void itemDelivered(DeliveryChute chute) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void doorOpened(DeliveryChute chute) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void doorClosed(DeliveryChute chute) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void chuteFull(DeliveryChute chute) {
		System.out.println("Delivery chute capacity exceeded.");
	}
	
} // end class
