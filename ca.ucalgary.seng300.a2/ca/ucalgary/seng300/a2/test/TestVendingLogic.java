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

import java.util.Timer;
import java.util.TimerTask;

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
	private long elapsedTime;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

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
	 * Tests if display shows "Hi there!" message for the first five seconds, followed by an empty message for 10 seconds
	 */
	@Test
	public void testDisplayCoordination() throws InterruptedException {
		long startTime = System.currentTimeMillis();
		elapsedTime = 0L;

		//System.out.println(vendingLogic.getDisplayMessage());
		//Throw your exception
		
		Timer timer = new Timer();
		int temp = 0;
		timer.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
				  if(elapsedTime < 5000 && vendingLogic.getDisplayMessage() != "Hi there!") {
					  fail();
				  }
				  if(elapsedTime > 5000 && elapsedTime < 10000 && vendingLogic.getDisplayMessage() != "") {
					  fail();
				  }
			  }
			}, 500, 5000);
		
		while (elapsedTime < 10000) {
		    //perform db poll/check

		    elapsedTime = System.currentTimeMillis() - startTime;
		}
	}
	
	/**
	 * Tests if pop is dispensed correctly with valid coin insertions and button presses
	 */
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

}
