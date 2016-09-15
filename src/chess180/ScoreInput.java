package chess180;

import java.math.BigInteger;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ScoreInput implements Runnable {

	private ScoreInputEventListener sel;
	
	volatile boolean black = false;
	volatile boolean active = false;
	int trekk = 1;
	
	public ScoreInput(ScoreInputEventListener sel) {
		this.sel = sel;
		Thread scoreThread = new Thread(this);
		scoreThread.setDaemon(true);
		scoreThread.start();
	}
	
	void start() {
		if (black) {
			System.out.println("1. WTF! Sort spiller sine poeng: ");
		}
		else {
			System.out.println("1. Hvit spiller sine poeng: ");
		}
		active = true;
		trekk = 1;
	}
	
	void stop() {
		active = false;
	}
	
	void setPlayer(boolean black) {
		this.black = black;
	}
	
	public synchronized void run() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			BigInteger score;
			try {
				score = scanner.nextBigInteger();
			} catch (InputMismatchException e) {
				sel.stopit();
				scanner = new Scanner(System.in);
				continue;
			}
			sel.score(black, score.intValue());
			black = !black;
			if (black) {
				System.out.println(trekk + ". Sort spiller sine poeng: ");
				trekk++;
			}
			else {
				System.out.println(trekk + ". Hvit spiller sine poeng: ");
			}
		}
	}
	
}
