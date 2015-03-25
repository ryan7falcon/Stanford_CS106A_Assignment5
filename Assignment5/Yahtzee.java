/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.*;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players (1-4)");
		//If user enters invalid number try again
		while ((nPlayers > 4)||(nPlayers < 1))
			nPlayers = dialog.readInt("Enter number of players (1-4)");
		playerNames = new String[nPlayers];
		list = new ArrayList[nPlayers];
		
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
			list[i-1] = new ArrayList<Integer>();
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		dice = new int[N_DICE];
		scoreTable = new int [nPlayers][18];	
		playGame();
	}

	private void playGame() {
		//Game is 13 turns long
		for (int turn = 1; turn <= 13; turn++)
		{
			//Each player plays a turn
			for (int i = 1; i <= nPlayers; i++) {
				PlayTurn(i);
			}
		}
		
		//when the game is over
		ComputeWinner();
	}
		
/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[] dice;//dice scores
	private int[][] scoreTable;//score table for each player
	ArrayList[] list;
	
	private void RollDice ()//rolls all dice
	{
		for (int i = 0; i < N_DICE; i++)
			dice[i] = rgen.nextInt(1,6);
	}
	private void RollSelectedDice()//rolls only selected by user dice
	{
		for (int i = 0; i < N_DICE; i++)
		{
			if (display.isDieSelected(i))
				dice[i] = rgen.nextInt(1, 6);
		}
	}
	
	/**compute score for chosen category invoking appropriate methods
	 * @param cat Category for which the score is calculated
	 * @return Score
	 */
	private int ComputeScore(int cat)//
	{
		int result = 0;
		if(CheckCategory(dice, cat))
			if (cat < 7)
				result = SumS(cat);
			else
				switch (cat)
				{
					case THREE_OF_A_KIND:
						result = SumAll();
					break;
					case FOUR_OF_A_KIND:
						result = SumAll();
					break;
					case FULL_HOUSE:
						result = 25;
					break;
					case SMALL_STRAIGHT:
						result = 30;
					break;
					case LARGE_STRAIGHT:
						result = 40;
					break;
					case YAHTZEE:
						result = 50;
					break;
					case CHANCE:
						result = SumAll();
					break;
				}
		return result;
	}
	/**
	 * compute score for categories 1-6 (Sum of all Ones's or Two's or...)
	 * @param cat Category for which the score is calculated
	 * @return Score
	 */
	private int SumS (int cat)
	{
		int result = 0;
		for (int i = 0; i < N_DICE; i++)
		{
			if (dice[i] == cat)
				result+=cat; 
		}
		return result;
	}
	/**
	 * compute score by sum of all dice
	 *
	 * @return Score
	 */
	private int SumAll()//sums all dice
	{
		int result = 0;
		for (int i = 0; i < N_DICE; i++)
		{
			result+=dice[i]; 
		}
		return result;
	}
	/**
	 * compute total score using upper score, lower score and bonus
	 * @param player Player
	 * 
	 */
	private void ComputeTotalScore(int player)
	{
		scoreTable[player-1][TOTAL] = scoreTable[player-1][LOWER_SCORE] + scoreTable[player-1][UPPER_SCORE] + scoreTable[player-1][UPPER_BONUS];
	}
	private void ComputeUpperScore(int player)
	{
		int upperScore = 0;
		int bonus = 0;
		
		for (int i = 1; i<7; i++)
		{
			upperScore+=scoreTable[player-1][i];
		}
		
		if(upperScore>=63)
			bonus = 35;
		
		scoreTable[player-1][UPPER_SCORE]=upperScore;
		scoreTable[player-1][UPPER_BONUS]=bonus;
	}
	private void ComputeLowerScore(int player)
	{
		int lowerScore = 0;
		for (int i = 9; i <= 15; i++)
		{
			lowerScore+=scoreTable[player-1][i];
		}
		scoreTable[player-1][LOWER_SCORE]=lowerScore;
	}
	/**
	 * plays one turn
	 *
	 * @param player Player
	 */
	private void PlayTurn(int player)
	{
		display.printMessage(playerNames[player-1] + "'s Turn. Click \"Roll Dice\" button to roll the dice.");
		//first roll
		display.waitForPlayerToClickRoll(player);
		RollDice();
		display.displayDice(dice);
		
		display.printMessage("Select the dice you want to re-roll and click \"Roll again\".");
		//second roll
		display.waitForPlayerToSelectDice();
		RollSelectedDice();
		display.displayDice(dice);
		
		display.printMessage("Select the dice you want to re-roll and click \"Roll again\".");
		//third roll
		display.waitForPlayerToSelectDice();
		RollSelectedDice();
		display.displayDice(dice);
		
		display.printMessage("Select a category for this roll.");
		
		int category = display.waitForPlayerToSelectCategory();
		while (list[player-1].contains(category))
			{
				display.printMessage("This category has already been played. Choose another one.");
				category = display.waitForPlayerToSelectCategory();
			}
		//list of used categories (each of them should be used only once)
		list[player-1].add(category);
	
		//Update score table
		int score = ComputeScore(category);
		display.updateScorecard(category, player, score);
		scoreTable[player-1][category] = score;
		ComputeUpperScore(player); 
		display.updateScorecard(UPPER_SCORE, player, scoreTable[player-1][UPPER_SCORE]);
		display.updateScorecard(UPPER_BONUS, player, scoreTable[player-1][UPPER_BONUS]);
		ComputeLowerScore(player);
		display.updateScorecard(LOWER_SCORE, player, scoreTable[player-1][LOWER_SCORE]);
		ComputeTotalScore(player);
		display.updateScorecard(TOTAL, player, scoreTable[player-1][TOTAL]);
	}
	private void ComputeWinner()
	{
		int maxIndex = 0;
		int maxScore = 0;
		
		for (int i = 0; i < nPlayers; i++)
		{
			if (scoreTable[i][TOTAL] > maxScore)
			{
				maxScore = scoreTable[i][TOTAL];
				maxIndex = i;
			}
			display.printMessage(playerNames[maxIndex] + " win Your score is " + maxScore);
		}
	}
	/**
	 * Checks validity of category for this dice
	 *@param d category Dice and category for checking
	 * @return bool true or false depending on validity of category for this dice
	 */
	private boolean CheckCategory (int[] d, int category)
	{
		boolean flag[] = new boolean[6];
		switch (category)
		{
		case THREE_OF_A_KIND:
			return CountFlagsMoreThan(3,d);
			
		case FOUR_OF_A_KIND:
			return CountFlagsMoreThan(4,d);
			
		case FULL_HOUSE:
			if (CountFlags(3,d))
			{
				return CountFlags(2,d);
			}
			else return false;
			
		case SMALL_STRAIGHT:
			CheckFlags(flag, d);
			if(flag[2]&&flag[3]&&((flag[1]&&flag[0])||(flag[1]&&flag[4])||(flag[4]&&flag[5])))
				return true;
			return false;

		case LARGE_STRAIGHT:
			CheckFlags(flag, d);
			if(flag[2]&&flag[3]&&flag[1]&&flag[4]&&((flag[5]||flag[0])))
				return true;
			return false;
			
		case YAHTZEE:
			return CountFlags(5,d);
			
		case CHANCE:
			return true;
		}
		return true;
	}
	/**
	 * Checks if there are certain numbers in this roll
	 *@param flag d Flag array and Dice
	 *
	 */
	private void CheckFlags (boolean[] flag, int[] d)
	{
		
		for (int i = 6; i<6; i++)
			flag[i] = false;
		for (int i = 1; i<=N_DICE; i++)//for each die
		{
			if (d[i-1]==3)
				flag[2] = true;
			if (d[i-1]==4)
				flag[3] = true;
			if (d[i-1]==5)
				flag[4] = true;
			if (d[i-1]==6)
				flag[5] = true;
			if (d[i-1]==1)
				flag[0] = true;
			if (d[i-1]==2)
				flag[1] = true;
		}
	}
	/**
	 * Counts if there any number repeats certain amount of times in this roll
	 *@param numberOfFlags d amount of times for repeating and dice
	 *@return bool 
	 */
	private boolean CountFlags(int numberOfFlags, int[] d)
	{
		for (int score = 1; score<=6; score++)//for each number that can be on dice
		{
			int counter = 0;
			for (int i = 1; i<=N_DICE; i++)//for each die
			{
				if (d[i-1]==score)//if it is a score, plus a counter
					counter++;
			}
			if(counter==numberOfFlags) //if counts more than 3
				return true;
		}
		return false;
	}
	/**
	 * Counts if there any number repeats certain amount of times or more in this roll
	 *@param numberOfFlags d amount of times for repeating and dice
	 *@return bool 
	 */
	private boolean CountFlagsMoreThan(int numberOfFlags, int[] d)
	{
		for (int score = 1; score<=6; score++)//for each number that can be on dice
		{
			int counter = 0;
			for (int i = 1; i<=N_DICE; i++)//for each die
			{
				if (d[i-1]==score)//if it is a score, plus a counter
					counter++;
			}
			if(counter>=numberOfFlags) //if counts more than 3
				return true;
		}
		return false;
	}
}
