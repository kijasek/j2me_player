package pl.tomaszkijas.j2meplayer;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;


public class MuuPlayerCore implements PlayerListener {

	private final static String WAVE_TYPE = "audio/x-wav";
	private final static String MP3_TYPE = "audio/mpeg";
	
	private final static String MP3_EXT = ".mp3";
	private final static String WAV_EXT = ".wav";
	
	private final static String FILE_PREFIX = "file:///";	
	private final static char EXTENSION_DELIMETER = '.';
	
	private final static long SEEK_RATIO = 2000000; //2 seconds
	
	private MuuPlayer muuPlayer;
	private Player player;
	private VolumeControl volumeControl;
	private String currentTrack;
	
	public MuuPlayerCore(MuuPlayer muuPlayer) {
		this.muuPlayer = muuPlayer;		
	}
	
	public void playNoise() {
		try {
			if ((player != null) && (player.getState() == Player.PREFETCHED)) {
				player.start();
			}
		} catch (MediaException e) {			
			e.printStackTrace();
			muuPlayer.displayErrorMessage("Player cannot be started");
		}
	}
	
	public void stopNoise() {
		try {
			if ((player != null) && (player.getState() == Player.STARTED)) {
				player.stop();
				player.setMediaTime(0);
			}
		} catch (MediaException e) {			
			e.printStackTrace();
		}
	}
	
	public void pauseNoise() {
		try {
			if ((player != null) && (player.getState() == Player.STARTED)) {
				player.stop();				
			}
		} catch (MediaException e) {			
			e.printStackTrace();
		}
	}
	
	public synchronized void readNoise(String fileName) {
		muuPlayer.displayPleaseWaitForm();
		stopNoise();		
		String content = getFileContent(fileName);
		currentTrack = fileName;		
				
		if (MP3_TYPE.equals(content) || WAVE_TYPE.equals(content)) {
			
			try {
				InputStream is = Connector.openInputStream(FILE_PREFIX + fileName);
				
				player = Manager.createPlayer(is, content);
				player.addPlayerListener(this);
				player.realize();
				volumeControl = (VolumeControl) player.getControl("VolumeControl");
				setVolumeLevel(muuPlayer.getCurrentVolumeLevel());
				player.prefetch();
				
				muuPlayer.setTrackName(fileName);
				muuPlayer.displayMainMenu();
			} catch (IOException e) {				
				e.printStackTrace();
				muuPlayer.displayErrorMessage("Unable to read file");
			} catch (MediaException e) {
				e.printStackTrace();
				muuPlayer.displayErrorMessage("Unsupported file type");
			} catch (SecurityException e) {
				e.printStackTrace();
				muuPlayer.displayErrorMessage("Permission denied");
			}
			
		} else {
			muuPlayer.displayErrorMessage("Unsupported file type");
		}
	}
	
	private String getFileContent(String fileName) {
		String result = null;
		int extensionIndex = fileName.lastIndexOf(EXTENSION_DELIMETER);
		String extension = fileName.substring(extensionIndex);
		
		if (MP3_EXT.equalsIgnoreCase(extension)) {
			result = MP3_TYPE;
		} else if (WAV_EXT.equalsIgnoreCase(extension)) {
			result = WAVE_TYPE;
		}
		
		return result;
	}
	
	public void setVolumeLevel(int level) {
		if (volumeControl != null) {
			volumeControl.setLevel(level * 10);
		}
	}
	
	public void seek(boolean isForwardSeek) {
		if ((player != null) && (player.getState() == Player.STARTED)) {
			
			try {
				long currentTime = player.getMediaTime();
				
				if (currentTime != Player.TIME_UNKNOWN) {
					if (isForwardSeek) 
						currentTime += SEEK_RATIO;
					else 
						currentTime -= SEEK_RATIO;
					player.setMediaTime(currentTime);
				}
			} catch (MediaException e) {				
				//unable to change current time in a song
				e.printStackTrace();
			}
		}
	}
	
	public String getCurrentTrack() {
		return currentTrack;
	}
	
	public void playerUpdate(Player player, String event, Object eventData) {
				
	}
}
