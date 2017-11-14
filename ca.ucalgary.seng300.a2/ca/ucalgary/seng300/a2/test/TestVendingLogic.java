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


	@Before
	public void setUp() throws Exception {

		//Canadian coins, 6 types of pop, capacity of coinRack=15, 10 pops per rack, 200 coins in receptacle, 
		//200 coins in delivery chute, 15 coins in coin return slot

		VendingMachine vendingMachine = new VendingMachine(canadianCoins, 6, coinRackCapacity, 10, 200, 200, 15);
		VendingLogic vendingLogic = new VendingLogic(vendingMachine);

		this.vendingMachine = vendingMachine;
		this.vendingLogic = vendingLogic;

		// Customize the pop kinds and pop costs in the vending machine
		/*		java.util.List<String> popCanNames = Arrays.asList("Cola","Sprite","Fonda","Diet","GingerAle","DrPepper");
		java.util.List<Integer> popCanCosts = Arrays.asList(250,250,250,250,250,250);
		vendingMachine.configure(popCanNames, popCanCosts);		
		 */
		//Configure different pop cans up to the number in the vending machine
		//and load one pop into each rack
		PopCan[] popCans = new PopCan[vendingMachine.getNumberOfPopCanRacks()];
		System.out.println(vendingMachine.getNumberOfPopCanRacks());
		for (int i = 0; i < vendingMachine.getNumberOfPopCanRacks(); i++) {
			popCans[i] = new PopCan("coke " + i);
			vendingMachine.getPopCanRack(i).load(popCans[i]);
		}
		
		int [] coinLoading = new int [vendingMachine.getNumberOfCoinRacks()];
		for (int i = 0; i < coinLoading.length; i++) {
			coinLoading[i] = coinRackCapacity - 5;
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
		assertEquals(250-vendingMachine.getPopKindCost(0),vendingLogic.getCredit()); 
		assertEquals(PopCan.class, vendedItems[0].getClass());
		assertEquals(9, vendingMachine.getPopCanRack(0).size());
	}

	/**
	 * Tests the insertion of an invalid coin
	 * Input - invalid coin (7 cents)
	 * Expected output - 0
	 * 
	 * @throws DisabledException
	 */
	@Test
	public void testInvalidCoin() throws DisabledException {
		vendingMachine.getCoinSlot().addCoin(new Coin(7));
		assertEquals(0,vendingLogic.getCredit());

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
