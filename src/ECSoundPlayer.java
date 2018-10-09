import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

public class ECSoundPlayer extends JFrame {
		private static final long serialVersionUID = 1L;

		// Constructor
	   public ECSoundPlayer(String fname) {
	      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      this.setVisible(false);
	   
	      try {
	         // Open an audio input stream.
	         URL url = this.getClass().getClassLoader().getResource(fname);
	         AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
	         // Get a sound clip resource.
	         Clip clip = AudioSystem.getClip();
	         // Open audio clip and load samples from the audio input stream.
	         clip.open(audioIn);
	         clip.start();
	      } catch (UnsupportedAudioFileException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	      } catch (LineUnavailableException e) {
	         e.printStackTrace();
	      }
	   }
	   
		static void playLockFX(){new ECSoundPlayer("sounds/lock.wav");}
		static void playUnlockFX(){new ECSoundPlayer("sounds/unlock.wav");}
	}