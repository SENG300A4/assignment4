package groupAssignment4;

import java.lang.Math;
import java.util.*;
import org.lsmr.vending.hardware.*;

public class ConfigPanelLogic {
	private int mode;
	private int input;		// input value from the user
	private VendingMachine vend;
	private int enteredPrice;
	private int selectedPop;
	
	public ConfigPanelLogic(VendingMachine vending) {
		mode = 0;
		input = -1;
		vend = vending;
		enteredPrice = 0;
		selectedPop = -1;
	}
	
	/**
	 * A method to branch into the specific display message depending on the current mode
	 */
	public void displayConfigMessage() {
		if (mode == 0) {
			displayPanelOptions();
		}
		else if (mode == 1) {
			displayPopConfigOptions();
		}
		else if (mode == 2) {
			displayConfig$Change();
		}
		else {
			throw new SimulationException("Unable to determine display mode"); 
		}
	}
	
	/**
	 * a method to display the options available for configurations 
	 */
	public void displayPanelOptions() {
		String message = "Select which aspect to configure: \n";
		message += "0 - Set Pop Price \n";
		message += "1 - "; // TODO more messages to display options 
		if (input < 0)
			message += "Selection: ";
		else 
			message += "Selection: " + input;
		vend.getConfigurationPanel().getDisplay().display(message);
	}
	
	/**
	 * A method to display to the user the question of which pop option that should have it's price changed
	 */
	public void displayPopConfigOptions() {
		String message = "Choose a Pop type that you would like to change the price of: \n";
		for (int i = 0; i < vend.getNumberOfPopCanRacks(); i++) {
			message += vend.getPopKindName(i) + " : $" + (double) ((double) vend.getPopKindCost(i) / 100) + "\n";
		}
		if (selectedPop < 0)
			message += "Selection: ";
		else 
			message += "Selection: " + vend.getPopKindName(selectedPop);
		vend.getConfigurationPanel().getDisplay().display(message);
	}
	
	/**
	 * A method display to the user what price is being proposed 
	 */
	public void displayConfig$Change() {
		String message = "Use 0 - 9 (10 for backspace) to enter new price for " 
						+ vend.getPopKindName(selectedPop) + ": \n $";
		message += ((double) enteredPrice / (double) 100);
		vend.getConfigurationPanel().getDisplay().display(message);
	}
	
	
	/**
	 * A method to determine what action should be taken when a button on the configuration panel is pressed
	 * @param button - the reference to the button that was pressed 
	 */
	public void configButtonAction(PushButton button) {
		if (button == vend.getConfigurationPanel().getEnterButton()) {
			if (mode == 0) {
				if (input == 0) {		// change mode to pop price change mode
					mode = 1;
					input = -1;
				}
				else if (input == 1) {	// change mode to ... TODO
					mode = 3;
					input = -1;
				}
				else {
					input = -1;
				}
			}
			else if (mode == 1) {
				mode = 2;
			}
			else if (mode == 2) {
				this.reconfigureVend();
				selectedPop = -1;
				mode = 0;
			}
			else
				throw new SimulationException("Invalid Configuration panel mode");
			
			this.displayConfigMessage();
		}
		
		else {
			for (int i = 0; i < 37; i++) {
				if (button == vend.getConfigurationPanel().getButton(i)) {  
					if (mode == 0) {
						input = i;
					}
					else if (mode == 1) {
						if (i >= vend.getNumberOfSelectionButtons()) {
							vend.getConfigurationPanel().getDisplay().display("Invalid Pop Selection");
							try {
								this.wait(5000); // wait 5 seconds
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
						}
						else {
							selectedPop = i;
						}
					}
					else if (mode == 2) {
						if (i == 10) {
							enteredPrice = (int) Math.floor((double) enteredPrice / 10); 
						}
						else if (i > 10) {
							// nothing  only use buttons 0 - 10
						}
						else {
							enteredPrice = (enteredPrice * 10) + i;
						}
					}
					else
						throw new SimulationException("Invalid Configuration panel mode");
					
					this.displayConfigMessage();		// re-display the current state of the panel
				}
			}
		}
	}
	
	/**
	 * The method that applies the inputs made by the user to the configuration of the vending machine.
	 */
	public void reconfigureVend() {
		List<String> popCanNames = new ArrayList<String>(vend.getNumberOfSelectionButtons());
		List<Integer> popCanCosts = new ArrayList<Integer>(vend.getNumberOfSelectionButtons());
		
		for (int i = 0; i < vend.getNumberOfSelectionButtons(); i++) {
			popCanNames.add(i, vend.getPopKindName(i));
		}
		
		for (int i = 0; i < vend.getNumberOfSelectionButtons(); i++) {
			if (i == selectedPop)
				popCanCosts.add(i, enteredPrice);
			else 
				popCanCosts.add(i, vend.getPopKindCost(i));
		}
	}
}
