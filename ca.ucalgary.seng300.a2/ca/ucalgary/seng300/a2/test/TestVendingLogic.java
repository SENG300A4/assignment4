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

import java.util.Arrays;
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
	private int coinRackCapacity = 15;
	
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
		
		VendingMachine vendingMachine = new VendingMachine(canadianCoins, 6, coinRackCapacity, 10, 200, 200, 15);
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
		
		int [] coinLoading = new int [vendingMachine.getNumberOfCoinRacks()];
		for (int i = 0; i < coinLoading.length; i++) {
			coinLoading[i] = coinRackCapacity;
		}
		this.vendingMachine.loadCoins(coinLoading);
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
		
		Timer timer = new Timer();
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
	
	/**
	 * 
	 * @throws DisabledException
	 */
	@Test
	public void testPressButtonWhenCoinsEnough() throws DisabledException {
		vendingMachine.getCoinSlot().addCoin(new Coin(200));
		vendingMachine.getCoinSlot().addCoin(new Coin(25));
		vendingMachine.getCoinSlot().addCoin(new Coin(25));
		vendingMachine.getSelectionButton(0).press();

		PopCan [] vendedItems = vendingMachine.getDeliveryChute().removeItems();
		
		//Product should have vended and value subtracted
		assertEquals(0,vendingLogic.getCredit()); 
		assertEquals(PopCan.class, vendedItems[0].getClass());
		assertEquals(9, vendingMachine.getPopCanRack(0).size());
	}

}
