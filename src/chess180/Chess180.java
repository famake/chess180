package chess180;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;


public class Chess180 implements ScoreInputEventListener, ButtonInputEventListener {

	private static final String DART_IP = "192.168.1.35";
	private static final String CHESS_TABLE_IP = "192.168.1.204";
	final static int CHESS_NDOT = 111;
	final static int DART_NDOT = 127;
	
	ArtNetSender fArt;
	ScoreInput scoreInput;
	ButtonInput buttonInput;
	
	long totalTime = 10*60000;
	long startTime = totalTime/2;//3*60000;
	long black = startTime, white = startTime;
	boolean isBlack = false;
	volatile boolean started = false;
	
	long lastUpdateTime;
	
	public Chess180() throws SocketException {
		
		scoreInput = new ScoreInput(this);
		buttonInput = new ButtonInput(this);
		// får first click
		lastUpdateTime = System.currentTimeMillis();
		fArt = new ArtNetSender();

		byte[] darta = new byte [DART_NDOT * 3];
		byte[] sjak = new byte [CHESS_NDOT * 3];
		int i;
		for (i=0; i<DART_NDOT*3; ++i) darta[i] = 20;
		for (i=0; i<CHESS_NDOT*3; ++i) sjak[i] = 20; 
		sendArtNet(darta, sjak);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			return;
		}
		for (i=0; i<DART_NDOT*3; ++i) darta[i] = 0;
		for (i=0; i<CHESS_NDOT*3; ++i) sjak[i] = 0;
		sendArtNet(darta, sjak);
		update();
	}
	
	
	public void game() {
		try {
			while(true) {
				Thread.sleep(1000);
				update();
			}
		} catch (InterruptedException e) {
		}
	}
	
	
	void gameOver() {
		scoreInput.stop();
		System.out.println("Spillet er over");
		started = false;
	}
	
	void update() {

		byte [] darta;
		byte [] sjak;
		
		if (started) {
			long now = System.currentTimeMillis();
			long elapsed = now - lastUpdateTime;
			lastUpdateTime = now;
			
			if (isBlack) {
				black -= elapsed;
			}
			else {
				white -= elapsed;
			}
			
			if (white - (totalTime/2) > black || black < 0) {
				terror(true);
				darta = new byte [DART_NDOT * 3];
				sjak = new byte [CHESS_NDOT * 3];
				gameOver();
				System.out.println("Sort tapte på tid.");
			}
			else if (black - (totalTime/2) > white || white < 0) {
				terror(false);
				darta = new byte [DART_NDOT * 3];
				sjak = new byte [CHESS_NDOT * 3];
				gameOver();
				System.out.println("Hvit tapte på tid.");
			}
			else {
				darta = showTime(DART_NDOT);
				sjak = showTime(CHESS_NDOT);
			}
			sendArtNet(darta, sjak);
		}
	}


	private void sendArtNet(byte[] darta, byte[] sjak) {
		try {
			fArt.sendData(CHESS_TABLE_IP, 10, sjak);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fArt.sendData(DART_IP, 0, darta);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void terror(boolean black) {
		byte[] darta = new byte [DART_NDOT * 3];
		byte[] sjak = new byte [CHESS_NDOT * 3];
		
		int chess_off = 0, dart_off = 0;
		if (black) {
			chess_off = CHESS_NDOT / 2;
			dart_off = DART_NDOT / 2;
		}
		
		for (int j=0; j<20; ++j) {; 
			for (int i=dart_off; i<(DART_NDOT/2)+dart_off; ++i) {
				darta[i*3+1] = 80;
				if ((j & 1) == 1) 
					darta[i*3+2] = 80;
				else
					darta[i*3+2] = 0;
			}
			for (int i=chess_off; i<(CHESS_NDOT/2)+chess_off; ++i) {
				sjak[i*3+1] = 80;
				if ((j & 1) == 1) 
					sjak[i*3+2] = 80;
				else
					sjak[i*3+2] = 0;
			}
			sendArtNet(darta, sjak);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				return;
			}
		}
	}


	private byte[] showTime(int ndot) {
		byte[] buffer = new byte[ndot*3];
		
		double whitePlayer = (white % totalTime) * 1.0 / totalTime;
		double blackPlayer = (black % totalTime) * 1.0 / totalTime;
		
		int w1, w2, b1, b2;
		if (isBlack) {
			w1 = 2;
			w2 = 2;
			b1 = 0;
			b2 = 1;
		}
		else {
			w1 = 0;
			w2 = 1;
			b1 = 2;
			b2 = 2;
			
		}
		
		int whiteDots = (int)(whitePlayer*ndot); 
		for (int i=0; i<whiteDots; ++i) {
			buffer[(i*3+w1)%(ndot*3)] = 80;
			buffer[(i*3+w2)%(ndot*3)] = 80;
		}
		int blackDots = (int)(blackPlayer*ndot); 
		for (int i=ndot/2; i<(ndot/2+blackDots); ++i) {
			buffer[(i*3+b1)%(ndot*3)] = 80;
			buffer[(i*3+b2)%(ndot*3)] = 80;
		}
		return buffer;
	}
	
	public static void main(String[] args) {
		Chess180 c;
		try {
			c = new Chess180();
			c.game();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void score(boolean blaque, int intValue) {
		if (blaque) {
			black += intValue*1000;
		}
		else {
			white += intValue*1000;
		}
		isBlack = !isBlack;
		/*if (isBlack) System.out.println("Sort sin tur.");
		else System.out.println("Hvit sin tur.");*/
		update();
	}


	@Override
	public void click() {
		if (started) {
			int i;
			byte[] darta = new byte [DART_NDOT * 3];
			byte[] sjak = new byte [CHESS_NDOT * 3];
			for (i=0; i<DART_NDOT; ++i) darta[i*3] = -1;
			for (i=0; i<CHESS_NDOT; ++i) sjak[i*3] = -1; 
			sendArtNet(darta, sjak);
		}
		else {
			System.out.println("Spillet er i gang!");
			started = true;
			isBlack = false;
			white = black = startTime;
			lastUpdateTime = System.currentTimeMillis();
			scoreInput.setPlayer(false);
			scoreInput.start();
			update();
		}
	}

	void stopIt() {
		gameOver();
		byte[] darta = new byte [DART_NDOT * 3];
		byte[] sjak = new byte [CHESS_NDOT * 3];
		int i;
		for (i=0; i<DART_NDOT*3; ++i) darta[i] = 0;
		for (i=0; i<CHESS_NDOT*3; ++i) sjak[i] = 0;
		sendArtNet(darta, sjak);
	}

	@Override
	public void allClick() {
		stopIt();
	}


	@Override
	public void stopit() {
		stopIt();
	}

}
