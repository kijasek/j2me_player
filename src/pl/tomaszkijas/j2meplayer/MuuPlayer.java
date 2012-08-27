package pl.tomaszkijas.j2meplayer;
import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


public class MuuPlayer extends MIDlet implements CommandListener, ItemStateListener{

	private static final Command EXIT_COMMAND = new Command("Exit", Command.EXIT, 0);
	private static final Command OPTIONS_COMMAND = new Command("Options", Command.SCREEN, 0);
	private static final Command BACK_COMMAND = new Command("Back", Command.BACK, 0);
	private static final Command OK_COMMAND = new Command("Ok", Command.OK, 0);
	
	private static final String NO_TRACK = "no track";
	private static final String PLAY_NAME = "play";
	private static final String STOP_NAME = "stop";
	private static final String PAUSE_NAME = "pause";
	private static final String SEEK_FWD_NAME = "seek forward";
	private static final String SEEK_BACK_NAME = "seek backward";
	private static final String SKIP_FWD_NAME = "skip forward";
	private static final String SKIP_BACK_NAME = "skip backward";
	
	private static final int VOLUME_MAX = 10;
	private static final int VOLUME_INITIAL = 5;
	private int currentVolume = VOLUME_INITIAL;
	
	private static final String PLAYLIST_NAME = "playlist";
	private static final String VOLUME_NAME = "volume";
	
	private static final String PLAYER_NAME = "MuuPlayer";
	
	private List mainMenu;
	private List optionsMenu;
	private Form volumeForm;
	private Form pleaseWaitForm;
	private Gauge volumeGauge;
	private MuuPlayerBrowser muuPlayerBrowser;
	private MuuPlayerCore muuPlayerCore;
	
	private Display display;
	
	public MuuPlayer() {
		display = Display.getDisplay(this);
		
		initializeMainMenu();
		initializeOptionsMenu();
		initializeVolumeForm();
		initializePleaseWaitForm();
		
		muuPlayerBrowser = new MuuPlayerBrowser(this);
		muuPlayerCore = new MuuPlayerCore(this);
	}

	protected void destroyApp(boolean arg0) {
		notifyDestroyed();
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		displayMainMenu();
	}
	
	private void initializeMainMenu() {
		mainMenu = new List(PLAYER_NAME, List.IMPLICIT);
		
		try {
			Image trackImage = Image.createImage("/audio-x-generic.png");
			Image playImage = Image.createImage("/media-playback-start.png");
			Image stopImage = Image.createImage("/media-playback-stop.png");
			Image pauseImage = Image.createImage("/media-playback-pause.png");
			Image fwdImage = Image.createImage("/media-seek-forward.png");
			Image backImage = Image.createImage("/media-seek-backward.png");		
			Image skipBackImage = Image.createImage("/media-skip-backward.png");
			Image skipFwdImage = Image.createImage("/media-skip-forward.png");
			
			mainMenu.append(NO_TRACK, trackImage);
			mainMenu.append(PLAY_NAME, playImage);
			mainMenu.append(STOP_NAME, stopImage);
			mainMenu.append(PAUSE_NAME, pauseImage);
			mainMenu.append(SEEK_BACK_NAME, backImage);
			mainMenu.append(SEEK_FWD_NAME, fwdImage);
			mainMenu.append(SKIP_BACK_NAME, skipBackImage);
			mainMenu.append(SKIP_FWD_NAME, skipFwdImage);
		} catch (IOException e) {
			String[] labels = {NO_TRACK, PLAY_NAME, STOP_NAME, PAUSE_NAME, SEEK_BACK_NAME, SEEK_FWD_NAME, SKIP_BACK_NAME, SKIP_FWD_NAME};
			mainMenu = new List(PLAYER_NAME, List.IMPLICIT, labels, null);
		}
		
		mainMenu.addCommand(EXIT_COMMAND);
		mainMenu.addCommand(OPTIONS_COMMAND);		
		
		mainMenu.setCommandListener(this);
	}
	
	private void initializeOptionsMenu() {
		optionsMenu = new List(PLAYER_NAME, List.IMPLICIT);
		
		optionsMenu.append(PLAYLIST_NAME, null);
		optionsMenu.append(VOLUME_NAME, null);
		
		optionsMenu.addCommand(BACK_COMMAND);
		optionsMenu.addCommand(OK_COMMAND);
		
		optionsMenu.setCommandListener(this);
	}
	
	private void initializePleaseWaitForm() {
		pleaseWaitForm = new Form("MuuPlayer");
		pleaseWaitForm.append("Please wait...");
	}
	
	private void initializeVolumeForm() {
		volumeForm = new Form("Set Volume");
		volumeGauge = new Gauge("volume", true, VOLUME_MAX, VOLUME_INITIAL);
		
		volumeForm.append(volumeGauge);
		
		volumeForm.addCommand(BACK_COMMAND);		
		
		volumeForm.setCommandListener(this);
		volumeForm.setItemStateListener(this);
	}
	
	public void commandAction(Command command, Displayable d) {
		if (mainMenu.isShown()) {		
			handleMainMenu(command, d);
		} else if (optionsMenu.isShown()) {
			handleOptionsMenu(command, d);
		} else if (volumeForm.isShown()) {
			handleVolumeForm(command);
		}
	}
	
	private void handleMainMenu(Command command, Displayable d) {
		if (command == EXIT_COMMAND) {
			destroyApp(true);
		}else if (command == OPTIONS_COMMAND) {
			displayOptionsMenu();
		}else if (command == List.SELECT_COMMAND) {
			handlePlaybackMenu(command, d);
		}
	}
	
	private void handlePlaybackMenu(Command command, Displayable d) {
		List l = (List)d;		
		
		switch (l.getSelectedIndex() - 1) {
		case 0:
			//play
			muuPlayerCore.playNoise();
			break;
		case 1:
			//stop
			muuPlayerCore.stopNoise();
			break;
		case 2:
			//pause
			muuPlayerCore.pauseNoise();
			break;
		case 3:
			//seek back
			muuPlayerCore.seek(false);
			break;
		case 4:
			//seek forward
			muuPlayerCore.seek(true);
			break;
		case 5:
			//skip back
			skipBack();			
			break;
		case 6:
			//skip forward
			skipForward();
			break;
		}
	}
	
	private void handleOptionsMenu(Command command, Displayable d) {
				
		if (command == BACK_COMMAND) {
			displayMainMenu();
		} else if ((command == List.SELECT_COMMAND) || (command == OK_COMMAND)) {
			List l = (List)d;
			
			switch (l.getSelectedIndex()) {
			case 0:
				//playlist was selected
				displayMuuPlayerBrowser();
				break;

			case 1:
				//volume was selected
				displayVolumeForm();
				break;
			}
		}
	}
	
	private void handleVolumeForm(Command command) {
		
		if (command == BACK_COMMAND) {
			displayOptionsMenu();
		}
	}
	
	public void itemStateChanged(Item item) {
		
		if (item instanceof Gauge) {
			currentVolume = ((Gauge)item).getValue();			
			muuPlayerCore.setVolumeLevel(currentVolume);
		}
	}
	
	private void skipBack() {
		String currentTrack = muuPlayerCore.getCurrentTrack();
		
		if (currentTrack != null) {
			final String prevTrack = muuPlayerBrowser.getPreviousTrack(currentTrack);
			if (prevTrack != null) {
				new Thread(new Runnable() {
					public void run() { muuPlayerCore.readNoise(prevTrack); }
				}).start();
			}
		}
	}
	
	private void skipForward() {
		String currentTrack = muuPlayerCore.getCurrentTrack();
		
		if (currentTrack != null) {
			final String nextTrack = muuPlayerBrowser.getNextTrack(currentTrack);
			if (nextTrack != null) {
				new Thread(new Runnable() {
					public void run() { muuPlayerCore.readNoise(nextTrack); }
				}).start();
			}
		}
	}
	
	public void setTrackName(String track) {
		Image img = mainMenu.getImage(0);
		
		if (track == null) {
			mainMenu.set(0, NO_TRACK, img);
		} else {
			mainMenu.set(0, track, img);
		}
	}
	
	public MuuPlayerCore getMuuPlayerCore() {
		return muuPlayerCore;
	}
	
	public int getCurrentVolumeLevel() {
		return currentVolume;
	}
	
	public void displayMainMenu() {
		display.setCurrent(mainMenu);
	}
	
	public void displayOptionsMenu() {
		display.setCurrent(optionsMenu);
	}
	
	public void displayVolumeForm() {
		display.setCurrent(volumeForm);
	}
	
	public void displayMuuPlayerBrowser() {		
		display.setCurrent(muuPlayerBrowser);
	}
	
	public void displayPleaseWaitForm() {
		display.setCurrent(pleaseWaitForm);
	}
	
	public void displayErrorMessage(String message) {
		Alert alert = new Alert("Error", message, null, AlertType.INFO);
		alert.setTimeout(Alert.FOREVER);		
		display.setCurrent(alert, mainMenu);		
	}

}
