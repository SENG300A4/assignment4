/**
 * JUnit Test Suite for VendingLogic class
 * SENG 300 Group Assignment 2
 * 
 * Stephen Pollo
 * Simran Rai
 * Roman Sklyar
 * Shawn Sangha
 * Anthony Coulthard
 * Emilie Guidos
 */

package ca.ucalgary.seng300.a2.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lsmr.vending.*;
import org.lsmr.vending.hardware.*;
import ca.ucalgary.seng300.a2.VendingLogic;

public class TestVendingLogic {

	private int[] canadianCoins = { 5, 10, 25, 100, 200 };
	private VendingLogic vendingLogic;
	private VendingMachine vendingMachine;
	
	

	@Before
	public void setUp() throws Exception {
		
		//Canadian coins, 6 types of pop, capacity of coinRack=15, 10 pops per rack, 200 coins in receptacle, 
		//200 coins in delivery chute, 15 coins in coin return slot
		
		VendingMachine vendingMachine = new VendingMachine(canadianCoins, 6, 15, 10, 200, 200, 15);
		VendingLogic vendingLogic = new VendingLogic(vendingMachine);
		
		this.vendingMachine = vendingMachine;
		this.vendingLogic = vendingLogic;
		
		//Configure different pop cans up to the number in the vending machine
		//and load one pop into each rack
		PopCan[] popCans = new PopCan[vendingMachine.getNumberOfPopCanRacks()];
		for (int i = 0; i < vendingMachine.getNumberOfPopCanRacks(); i++) {
			popCans[i] = new PopCan("coke " + i);
			vendingMachine.getPopCanRack(i).load(popCans[i]);
		}
		
		vendingMachine.loadCoins(10,10,10,10,10);
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Tests if display displays credit correctly after inserting a coin.
	 */
	@Test
	public void testDisplayOnInsert() throws InterruptedException {
		
		Coin fiveCents = new Coin(5);
		
		try {
			vendingMachine.getCoinSlot().addCoin(fiveCents);
			assertEquals("Credit: 5", vendingLogic.getDisplayMessage());
			
		} catch (DisabledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Tests if pop is dispensed correctly with valid coin insertions and button presses
	 */
/*	
	@Test
	public void testDispense() {
		Coin tenCents = new Coin(10);
		int currentCredit = 10;
		try {
			vendingMachine.getCoinSlot().addCoin(tenCents);
			//Test once for each button
			for (int i = 0; i < vendingMachine.getNumberOfSelectionButtons(); i++) {
				vendingLogic.pressed(vendingMachine.getSelectionButton(i));
				assertEquals(currentCredit - vendingMachine.getPopKindCost(i), vendingLogic.getCredit());
				assertEquals(0, vendingMachine.getPopCanRack(i).size());
				currentCredit -= vendingMachine.getPopKindCost(i);
			}
		} catch (DisabledException e) {
			System.out.println("Coin Slot disabled.");
		}
	}
*/
	
	//***EMILIE'S NOTES (Remove later)*********************
	/*Test: Machine returns change: -when exact change is provided (credit == 0)
	 *  							-when inexact change is provided (remove from credit)
	 *      Exact change light is on or off at proper state
	 *      
	 *      Out of order light is on or off at proper state
	 *      
	 *Unit test methods: -fullCoinRacks
	 *					-MachineEmpty
	
	 */
	
	/*
	 * Tests that exact change is returned from machine when all coin racks have enough coins
	 */
	@Test
	public void testReturnExactChange(){
		Coin toonie = new Coin(200);
		Coin loonie = new Coin(100);
		Coin dime = new Coin(10);
		
		//initial number of toonies, loonies, dimes in respective coin racks
		int num_toonies = vendingMachine.getCoinRackForCoinKind(200).size();
		int num_loonies = vendingMachine.getCoinRackForCoinKind(100).size(); 
		int num_dimes = vendingMachine.getCoinRackForCoinKind(10).size(); 
		System.out.println(num_toonies);
		System.out.println(num_loonies);
		System.out.println(num_dimes);
		
		try {
			vendingMachine.getCoinSlot().addCoin(toonie);
			vendingMachine.getCoinSlot().addCoin(toonie);
			vendingMachine.getCoinSlot().addCoin(loonie);
			vendingMachine.getCoinSlot().addCoin(dime);
		} catch (DisabledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		vendingLogic.provideChange(vendingLogic.getCredit());
		assertEquals(0,vendingLogic.getCredit());
		assertEquals(num_toonies - 2, vendingMachine.getCoinRackForCoinKind(200).size());
		assertEquals(num_loonies - 1, vendingMachine.getCoinRackForCoinKind(100).size());
		assertEquals(num_dimes - 1, vendingMachine.getCoinRackForCoinKind(10).size());
		
	}

	/*
	 * Tests that exact change is returned from machine when some, but not all, of the coin racks
	 * are out of coins
	 */
	
	/*
	 * Tests that inexact change is returned from machine when not enough coins. Change should be as close as
	 * possible to exact without going over remaining credit
	 */
	
	
	
	
	
}
