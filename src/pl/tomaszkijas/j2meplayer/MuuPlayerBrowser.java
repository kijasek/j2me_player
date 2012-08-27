package pl.tomaszkijas.j2meplayer;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;


public class MuuPlayerBrowser extends List implements CommandListener{

	private static final Command OK_COMMAND = new Command("Ok", Command.OK, 0);
	private static final Command CANCEL_COMMAND = new Command("Cancel", Command.CANCEL, 0);
	
	private static Image dirImage;
	private static Image fileImage;
	
	private final static String MP3_EXT = ".mp3";
	private final static String WAV_EXT = ".wav";
	
	private static final String UP_DIRECTORY = "..";
	private static final String ABSOLUTE_ROOT = "/";
	private final static char EXTENSION_DELIMETER = '.';
	private static final char SEP = '/';
	
	private MuuPlayer muuPlayer;
	private String currentDirectoryName = ABSOLUTE_ROOT;
	private Vector tracks;
		
	public MuuPlayerBrowser(MuuPlayer muuPlayer) {
		super("Browse...", List.IMPLICIT);
		this.muuPlayer = muuPlayer;
		
		addCommand(CANCEL_COMMAND);
		addCommand(OK_COMMAND);
		
		setCommandListener(this);
		setSelectCommand(OK_COMMAND);
		
		tracks = new Vector();
		
		showCurrentDir();
	}
	
	public void commandAction(Command command, Displayable d) {
		if (command == CANCEL_COMMAND) {
			muuPlayer.displayOptionsMenu();
		} else if (command == OK_COMMAND) {
			final String currFile = getString(getSelectedIndex());			
            
			new Thread(new Runnable() {
                    public void run() {
                        if (currFile.endsWith("/") || currFile.equals(UP_DIRECTORY)) {
                            traverseDirectory(currFile);
                        } else {
                            muuPlayer.getMuuPlayerCore().readNoise(currentDirectoryName + currFile);                            
                        }
                    }
                }).start();
		}
	}
		
	public Enumeration getRootDirs() {
		Enumeration drives = FileSystemRegistry.listRoots();
				
		return drives;
	}
	
	void showCurrentDir() {
		Enumeration e = null;
		FileConnection currentDir = null;
		deleteAll();
		
		try {
			if (ABSOLUTE_ROOT.equals(currentDirectoryName)) {
				e = getRootDirs();
			} else {
				currentDir = (FileConnection)Connector.open("file:///" + currentDirectoryName);
				e = currentDir.list();
				append(UP_DIRECTORY, dirImage);
			}		
			
			while (e.hasMoreElements()) {
			    String fileName = (String)e.nextElement();

			    if (fileName.charAt(fileName.length() - 1) == SEP) {
			    	append(fileName, dirImage);
			    } else if (isMusicFile(fileName)) {
			    	append(fileName, fileImage);
			    	tracks.addElement(currentDirectoryName + fileName);
			    }
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (currentDir != null) {
					currentDir.close();
				}
			} catch (IOException e1) { }				
		}
		
	}
	
	public void traverseDirectory(String fileName) {
		if (UP_DIRECTORY.equals(fileName)) {
			int i = currentDirectoryName.lastIndexOf(SEP, currentDirectoryName.length() - 2);

            if (i != -1) {
                currentDirectoryName = currentDirectoryName.substring(0, i + 1);
            } else {
                currentDirectoryName = ABSOLUTE_ROOT;
            }
		} else if (ABSOLUTE_ROOT.equals(currentDirectoryName)) {
			currentDirectoryName = fileName;
		} else {
			currentDirectoryName = currentDirectoryName + fileName;
		}
		
		showCurrentDir();
	}
	
	private boolean isMusicFile(String fileName) {
		boolean result = false;
		int extensionIndex = fileName.lastIndexOf(EXTENSION_DELIMETER);
		
		if (extensionIndex != -1) {
			String extension = fileName.substring(extensionIndex);
			if (MP3_EXT.equalsIgnoreCase(extension)) {
				result = true;
			} else if (WAV_EXT.equalsIgnoreCase(extension)) {
				result = true;
			}
		}
		
		return result;
	}
	
	public String getNextTrack(String fileName) {
		String result = null;
		
		if (tracks.contains(fileName)) {
			int index = tracks.indexOf(fileName);
			if ((index + 1) == tracks.size())
				result = null;
			else
				result = (String) tracks.elementAt(index + 1);
		} else if (!tracks.isEmpty()) {
			result = (String) tracks.firstElement();
		}
		
		return result;
	}
	
	public String getPreviousTrack(String fileName) {
		String result = null;
		
		if (tracks.contains(fileName)) {
			int index = tracks.indexOf(fileName);
			if ((index - 1) < 0)
				result = null;
			else
				result = (String) tracks.elementAt(index - 1);
		} else if (!tracks.isEmpty()) {
			result = (String) tracks.lastElement();
		}
		
		return result;
	}
	
	//STATIC BLOCK!
	static {
		try {
			dirImage = Image.createImage("/dir.png");
			fileImage = Image.createImage("/file.png");
		} catch (IOException e) {
			dirImage = null;
			fileImage = null;
		}
	}
}
