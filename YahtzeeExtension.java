/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.applet.AudioClip;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.graphics.GRoundRect;
import acm.io.*;
import acm.program.*;
import acm.util.*;
import acmx.export.java.io.FileReader;

public class YahtzeeExtension extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new YahtzeeExtension().start(args);
	}
	
	public void init() {
		
	}
	
	public void run() {
		
		extension();
		
		addPlayingBackground();
		
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
			
			//In arrayList all array of score must be full with special symbol. In arrayList length equals number of players.
			Integer[] scoreMassive = new Integer[N_CATEGORIES]; //players score array
			fillArrayWithSpecialSymbol(scoreMassive); //fill array which will be on i index
			playersScore.add(scoreMassive); //add array of score on i index
		}
		
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		
		playGame();
	}
	
	private void extension() {
		addMouseListeners();
		
		loading();
		
		menu();
		
		removeAll();
	}
	
	private void addPlayingBackground() {
		GImage background = new GImage("playing1.jpg");
		background.setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
		add(background);
	}
	
	private void fillArrayWithSpecialSymbol(Integer[] scoreMassive) {
		for(int i = 0; i < N_CATEGORIES; i++) {
			scoreMassive[i] = SPECIAL_NUM;
		}
	}

	private void playGame() {
		//first for depends on how many parts are in game for each player, second depends on how many players are in game 
		for(int i = 1; i <= N_SCORING_CATEGORIES; i++) {
			for(int j = 1; j <= nPlayers; j++) {//this loop repeat as many times as there are players in the game
				//this method starts when concrete player starts own turn and this method end when the player ends own turn
				//because of this this method repeat as many times as there are players in the game. j is index of player
				turnOnePlayer(j);
			}
		}
		//this method update categories upper score, bonus score and total score for all players. 
		lastUpdateScores();
		winner();
	}
	
	private void turnOnePlayer(int player) {
		int category; //when the player have already chosen category, this category are stored in this variable
		
		display.printMessage(playerNames[player - 1] + "'s turn! Click 'Roll Dice' button to roll the dice.");
		
		//first roll
		display.waitForPlayerToClickRoll(player);
		dices = rollDices(); //this method makes simulation of rolling and return array of value of dices
		display.displayDice(dices); 
		
		//roll again (NUM_ROLLS_ONE_TURN - 1)times
		for(int i = 0; i < NUM_ROLLS_ONE_TURN - 1; i++) {
			display.printMessage("Select the dice you wish to re-roll and click 'Roll Again'");
			rollAgain();
		}
		
		//choose category, check category and write relevant score
		display.printMessage("Select a category for this roll.");
		category = display.waitForPlayerToSelectCategory();
		//this method write score in selected category if category are not selected before and if category are selected before print relevant message and
		//and And the program allowed the player to re-select the category
		writeScoreInSelectedCategory(category, player);
	}
	
	private int[] rollDices() {
		int[] dices = new int[N_DICE];
		for(int i = 0; i < N_DICE; i++) {
			dices[i] = rgen.nextInt(1, 6);
		}
		return dices;
	}
	
	private void rollAgain() {
		display.waitForPlayerToSelectDice();
		//this method have one parameter, this parameter is array of dices value, this method makes simulation of roll again.
		//this method also returns new array of dices value. dices will roll again which is selected by player and in array value change new value of dice.
		dices = rollSelectedDices();
		display.displayDice(dices);
	}
	
	private int[] rollSelectedDices() {
		for(int i = 0; i < N_DICE; i++) {
			if(display.isDieSelected(i)) {
				dices[i] = rgen.nextInt(1, 6);
			}
		}
		return dices;
	}
	
	private void writeScoreInSelectedCategory(int category, int player) {
		//this while continues until player will choose category which category is not selected before
		while(true) {
			/*above the this code in playersScore(arrayList) program have already add array of scores of players
			 and also this array are filled with special symbols. when player choose any category and this category is empty, score write this category and 
			 also this score store in array of scores of player Instead of a special symbol.
			*/
			if(playersScore.get(player - 1)[category - 1] == SPECIAL_NUM) {//If the category is selected for the first time
				if(checkCategory(category)) {//if selected category is relevant
					int score = categoryRelevantScore(category);
					display.updateScorecard(category, player, score); //update selected category
					playersScore.get(player - 1)[category - 1] = score; //save score in array of scores of player Instead of a special symbol.
					/*countScore This method calculates scores from the first category to the corresponding category of the parameter 
					 * value specified in parentheses. in array of scores, on some index is stored special symbol but this method do not substract
					 * this value of special symbol*/
					int totalScore = countScore(playersScore.get(player - 1), playersScore.get(player - 1).length);
					display.updateScorecard(TOTAL, player, totalScore);
				}else {//if selected category is not relevant
					display.updateScorecard(category, player, 0);
					int totalScore = countScore(playersScore.get(player - 1), playersScore.get(player - 1).length);
					playersScore.get(player - 1)[category - 1] = 0; //save score in array of scores of player Instead of a special symbol.
					display.updateScorecard(TOTAL, player, totalScore);
				}
				break;
			}else {//if selected category is selected before and write score in this category 
				display.printMessage("This category has been selected once, please select another category again.");
				category = display.waitForPlayerToSelectCategory(); //players can select category again
			}
		}
	}
	
	//This method checks to see if the selected category is appropriate, if so then return true or false
	private boolean checkCategory(int category) {
		//for first six categories
		if(category <= SIXES) {
			for(int i = 0; i < N_DICE; i++) {
				if(dices[i] == category) {
					return true;
				}
			}
		}else if(category == THREE_OF_A_KIND) {
			int count = 0;
			int three = 3;
			for(int i = 1; i <= MAX_NUMBER; i++) {
				for(int j = 0; j < N_DICE; j++) {
					if(dices[j] == i) {
						count++;
					}
				}
				if(count >= three) {
					return true;
				}else {
					count = 0;
				}
			}
		}else if(category == FOUR_OF_A_KIND) {
			int count = 0;
			int four = 4;
			for(int i = 1; i <= MAX_NUMBER; i++) {
				for(int j = 0; j < N_DICE; j++) {
					if(dices[j] == i) {
						count++;
					}
				}
				if(count >= four) {
					return true;
				}else {
					count = 0;
				}
			}
		}else if(category == FULL_HOUSE) {
			int threeTimes = 0;
			int twoTimes = 0;
			
			for(int i = 1; i <= MAX_NUMBER; i++) {
				for(int j = 0; j < N_DICE; j++) {
					if(dices[j] == i) {
						threeTimes++;
					}
				}
				
				if(threeTimes == 3) {
					for(int m = 1; m <= MAX_NUMBER; m++) {
						if(m != i) {
							for(int n = 0; n < N_DICE; n++) {
								if(dices[n] == m) {
									twoTimes++;
								}
							}
							if(twoTimes == 2) {
								return true;
							}else {
								twoTimes = 0;
							}
						}
					}
				}else {
					threeTimes = 0;
				}
				
			}
		}else if(category == SMALL_STRAIGHT) {
			int count = 0;
			for(int i = 1; i <= 3; i++) {
				for(int j = i; j <= MAX_NUMBER; j++) {
					for(int k = 0; k < N_DICE; k++) {
						if(dices[k] == j) {
							count++;
							break;
						}
					}
				}
				
				if(count >= 4) {
					return true;
				}else {
					count = 0;
				}
			}
		}else if(category == LARGE_STRAIGHT) {
			int count = 0;
			for(int i = 1; i <= 3; i++) {
				for(int j = i; j <= MAX_NUMBER; j++) {
					for(int k = 0; k < N_DICE; k++) {
						if(dices[k] == j) {
							count++;
							break;
						}
					}
				}
				
				if(count == 5) {
					return true;
				}else {
					count = 0;
				}
			}
		}else if(category == YAHTZEE) {
			int count = 0;
			for(int i = 1; i <= MAX_NUMBER; i++) {
				for(int j = 0; j < N_DICE; j++) {
					if(dices[j] == i) {
						count++;
					}
				}
				
				if(count == 5) {
					return true;
				}else {
					count = 0;
				}
			}
		}else if(category == CHANCE) {
			return true;
		}
		
		return false;
	}
	
	private int categoryRelevantScore(int category) {
		int score = 0;
		if(category <= 6) {
			for(int i = 1; i <= category; i++) {
				if(i == category) {
					//this method returns how many relevant dice are in rolled dices
					score = countRelevantDices(category) * category;
					return score;
				}
			}
		}else if(category == 9) {
			for(int i = 0; i < N_DICE; i++) {
				score += dices[i];
			}
		}else if(category == 10) {
			for(int i = 0; i < N_DICE; i++) {
				score += dices[i];
			}
		}else if(category == 11) {
			score = 25;
		}else if(category == 12) {
			score = 30;
		}else if(category == 13) {
			score = 40;
		}else if(category == 14) {
			score = 50;
		}else if(category == 15) {
			for(int i = 0; i < N_DICE; i++) {
				score += dices[i];
			}
		}
		return score;
	}
	
	private int countRelevantDices(int category) {//this method returns how many relevant dice are in rolled dices
		int count = 0;
		for(int i = 0; i < N_DICE; i++) {
			if(dices[i] == category) {
				count++;
			}
		}
		return count;
	}
	
	/*countScore This method calculates scores from the first category to the corresponding category of the parameter 
	 * value specified in parentheses. in array of scores, on some index is stored special symbol but this method do not substruct
	 * this value of special symbol*/
	private int countScore(Integer[] scoreMassive, int numberOfCategories) {
		int totalScore = 0;
		for(int i = 0; i < numberOfCategories; i++) {
			if(scoreMassive[i] != SPECIAL_NUM) {
				totalScore += scoreMassive[i];
			}
		}
		return totalScore;
	}
	
	private void lastUpdateScores() {
		int totalScore = 0;
		for(int i = 1; i <= nPlayers; i++) {
			totalScore = updateUpperScoreAndBonus(playersScore.get(i - 1), i); //for update Upper Score And Bonus
			display.updateScorecard(TOTAL, i, totalScore);
		}
	}
	
	private int updateUpperScoreAndBonus(Integer[] scoreMassive, int player) {
		int upperScore = countScore(scoreMassive, SIXES);
		int lowerScore = countScore(scoreMassive, N_CATEGORIES) - upperScore;
		int totalScore = upperScore + lowerScore;
		display.updateScorecard(UPPER_SCORE, player, upperScore);
		
		//for bonus category
		if(upperScore >= IS_BONUS) {
			display.updateScorecard(UPPER_BONUS, player, FOR_BONUS);
			totalScore += FOR_BONUS;
		}else {
			display.updateScorecard(UPPER_BONUS, player, 0);
		}
		
		//for lower score
		display.updateScorecard(LOWER_SCORE, player, lowerScore);
		//save upper, lower and total score in array of score of player
		playersScore.get(player - 1)[UPPER_SCORE - 1] = upperScore;
		playersScore.get(player - 1)[UPPER_SCORE - 1] = lowerScore;
		playersScore.get(player - 1)[TOTAL - 1] = totalScore;
		return totalScore;
	}
	
	private void winner() {
		String winner = "";
		int winnerScore = 0;
		int winnerIndex = 0;
		//if number of players are nPlayers then program need (nPlayers - 1) compare to Identify the winner.
		for(int i = 1; i <= (nPlayers - 1); i++) {
			if(playersScore.get(winnerIndex)[TOTAL - 1] > playersScore.get(i)[TOTAL - 1]) {
				winner = playerNames[i - 1];
				winnerScore = playersScore.get(i - 1)[TOTAL - 1];
				winnerIndex = i - 1;
			}else {
				winner = playerNames[i];
				winnerScore = playersScore.get(i)[TOTAL - 1];
				winnerIndex = i;
			}
		}
		display.printMessage("Congratulations, " + winner + ", you're winner with a total score of " + winnerScore + "!");
	}
	
	private void loading() {
		backgroundLevel1(APPLICATION_WIDTH, APPLICATION_HEIGHT); //background of game
		addLoadingLabel(); //draw label "Loading" on canvas
		addLoadingBar(); //for animation of loading
	}

	//Background Implementation
	private void backgroundLevel1(double x, double y) {
		background = new GImage("loading.jpg");
		background.setSize(x, y);
		add(background);
	}
	//create loading label
	private void addLoadingLabel() {
		GLabel loading = new GLabel("L O A D I N G");
		loading.setFont("SenSerif-Bold-40");
		loading.setColor(Color.MAGENTA);
		add(loading, getWidth()/2 - loading.getWidth()/2, getHeight()/2 + BUTTON_SEP * 2/3);
	}
	
	//create loading bar and small animation of loading
	private void addLoadingBar() {
		GRoundRect loadingBar = new GRoundRect(getWidth()/2, getHeight()/22);
		loadingBar.setFilled(true);
		loadingBar.setColor(Color.BLACK);
		add(loadingBar, getWidth()/2 - loadingBar.getWidth()/2, getHeight()/2 + BUTTON_SEP);
		loadingAnimation(loadingBar); //loading animation
	}
	
	//implementation of loading animation
	private void loadingAnimation(GRoundRect rect) {
		double x = 1; //size of filler(size of recte)
		//animation
		AudioClip loading = MediaTools.loadAudioClip("loading.au");
		loading.play(); //sound of loading animation
		for(int i = 0; rect.getX() + (i + 1) * x <= rect.getX() + rect.getWidth(); i++) { 
			GRect fillRect = new GRect(x, rect.getHeight() + 2);
			fillRect.setFilled(true);
			fillRect.setColor(Color.GREEN);
			pause(4);
			add(fillRect, rect.getX() + i * x, rect.getY());
		} 
		loading.stop();
	}
	
	private void menu() {
		while(true) {
			addMenuBackground();
			addStartButton();
			addSettingsButton();
			addExitButton();
			
			while(startRect.getColor() != CR1 && historyRect.getColor() != CR1 && exitRect.getColor() != CR1) {
				
			}
			
			
			if(startRect.getColor() == CR1) {
				buttonClickSound();
				break;
			}else if(exitRect.getColor() == CR1){
				buttonClickSound();
				exit();
			}else {
				buttonClickSound();
				history();
			}
			buttonClickSound();
		}
	}
	
	private void addMenuBackground() {
		background = new GImage("loading.jpg");
		background.setLocation(0, 0);
		background.setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
		add(background);
	}
	
	private void addStartButton() {
		startRect = drawButtonRect(startRect, getWidth()/2 - BUTTON_X/2, BUTTON_POSY);
		startLabel = drawButtonLabel(startLabel, getWidth()/2 - BUTTON_X/2, BUTTON_POSY, "S T A R T");
	}
	
	private void addSettingsButton() {
		historyRect = drawButtonRect(historyRect, getWidth()/2 - BUTTON_X/2, BUTTON_POSY + BUTTON_SEP);
		historyLabel = drawButtonLabel(historyLabel, getWidth()/2 - BUTTON_X/2, BUTTON_POSY + BUTTON_SEP, "H I S T O R Y");
	}
	
	private void addExitButton() {
		exitRect = drawButtonRect(exitRect, getWidth()/2 - BUTTON_X/2, BUTTON_POSY + 2 * BUTTON_SEP);
		exitLabel = drawButtonLabel(exitLabel, getWidth()/2 - BUTTON_X/2, BUTTON_POSY + 2 * BUTTON_SEP, "E X I T");
	}
	
//with help of this method i can create button rect, this method have three parameter. first is created object for butoon rect, second an third parameter is for 
	private GRoundRect drawButtonRect(GRoundRect rect, double rectX, double rectY) {
		rect = new GRoundRect(rectX, rectY, BUTTON_X, BUTTON_Y, BUTTON_ARC_SIZE);
		rect.setFilled(true);
		rect.setColor(CR);
		add(rect);
		
		return rect;
	}
	
	//with help of this method i create button Label. first parameter is instance variable object for button label second and third is for size and last is for string
	//this and drawButtonRect create together full button
	private GLabel drawButtonLabel(GLabel label, double rectX, double rectY, String textOFButton) {
		label = new GLabel(textOFButton);
		label.setColor(CL);
		label.setFont("SenSerif-Bold-25");
		add(label, rectX + (BUTTON_X - label.getWidth())/2, rectY + BUTTON_Y/2 + label.getAscent()/2);
		
		return label;
	}
	
	private void history() {
		removeAll();
		addHistoryBackground();
		addCancelButton();
		readHighScore();
		
		while(cancelRect.getColor() != CR1) {
			
			
		}
		
	}
	
	private void addHistoryBackground() {
		GImage background = new GImage("history.jpg");
		background.setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
		add(background);
	}
	
	private void addCancelButton() {
		cancelRect = drawButtonRect(cancelRect, getWidth()/2 - BUTTON_X/2, BUTTON_POSY + 2 * BUTTON_SEP);
		cancelLabel = drawButtonLabel(cancelLabel, getWidth()/2 - BUTTON_X/2, BUTTON_POSY + 2 * BUTTON_SEP, "C A N C E L");
	}
	
	//implementation of button click sound
	private void buttonClickSound() {
		AudioClip buttonClick = MediaTools.loadAudioClip("buttonClick.au");
		buttonClick.play();
		pause(CLICK_PAUSE);
	}
	
	private void readHighScore() {
		String name = null;
		String readScore = null;
		int[] scoreMass = new int[10];
		String[] nameMass = new String[10];
		
		try {
			BufferedReader rd = new BufferedReader(new FileReader("HighScores"));
			waitForClick();
			removeAll();
			
			for(int i = 0; true; i++) {
				name = rd.readLine();
				readScore = rd.readLine();
				
				if(name != null) {
					nameMass[i] = name;
					scoreMass[i] = Integer.parseInt(readScore);
					
					GLabel nameLabel = new GLabel(nameMass[i]);
					nameLabel.setColor(Color.BLACK);
					add(nameLabel, getWidth()/2, i * PRINT_SEP);
					
				}else {
					rd.close();
					break;
				}
			}
			
		}catch(Exception ex){
			
		}
		
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(clickOnButton(startRect, startLabel, e)) {
			startRect.setColor(CR1);
		}else if(clickOnButton(historyRect, historyLabel, e)){
			historyRect.setColor(CR1);
		}else if(clickOnButton(exitRect, exitLabel, e)){
			exitRect.setColor(CR1);
		}else if(clickOnButton(cancelRect, cancelLabel, e)){
			cancelRect.setColor(CR1);
		}
	}
	
	private boolean clickOnButton(GRoundRect rect, GLabel label, MouseEvent e) {
		if(getElementAt(e.getX(), e.getY()) == rect || getElementAt(e.getX(), e.getY()) == label) {
			return true;
		}else {
			return false;
		}
	}
		
/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator().getInstance();
	
	
	private ArrayList<Integer[]> playersScore = new  ArrayList<Integer[]>(); //for players massive of score. length of list is number of players
	private int[] dices = new int[N_DICE]; //When the dice are already rolled, the values of these dice are stored in this array 
	
	private static final int MAX_NUMBER = 6;
	private static final int NUM_ROLLS_ONE_TURN = 3;
	private static final int SPECIAL_NUM = -1;
	private static final int IS_BONUS = 63;
	private static final int FOR_BONUS = 35;
	
	//for extension
	private final static double BUTTON_X = 200;
	private final static double BUTTON_Y = 40;
	private final static double BUTTON_SEP = 60;
	private final static double BUTTON_ARC_SIZE = 30;
	private final static double BUTTON_POSY = 350;
	
	private final static Color CR = Color.ORANGE;
	private final static Color CR1 = Color.GREEN;
	private final static Color CL = Color.BLUE;
	
	private static final int CLICK_PAUSE = 200;
	private static final double PRINT_SEP = 15;
	
	GImage background;
	
	GRoundRect startRect;
	GLabel startLabel;
	
	GRoundRect historyRect;
	GLabel historyLabel;
	
	GRoundRect exitRect;
	GLabel exitLabel;
	
	GRoundRect cancelRect;
	GLabel cancelLabel;
	
	
}
