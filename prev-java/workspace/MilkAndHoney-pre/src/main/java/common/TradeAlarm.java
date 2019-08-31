package common;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TestJavaClient.Main;

public class TradeAlarm {
	private transient final static Logger log = LoggerFactory.getLogger(TradeAlarm.class);
	
//	public static void playSound(String filename) {
//	    try {
//	    	URL url = new URL("file:" + "target\\classes\\common\\"+filename);
//	    	AudioClip ac = Applet.newAudioClip(url);
//	    	ac.play();
//	    	//ac.stop();// stop
//	    	
//	    	// second method
//			//	InputStream in = new FileInputStream("target\\siren.wav");
//			// 	AudioStream as = new AudioStream(in);
//			// 	AudioPlayer.player.start(as);
//		} catch (Exception e) {
//			log.warn("{}",e);
//		}
//	}
	
	  public static synchronized void playSound(final String url) {     
		  new Thread(new Runnable() { // the wrapper thread is unnecessary, unless it blocks on the Clip finishing, see comments       
			  public void run() {         
				  try {           
					  Clip clip = AudioSystem.getClip();        
					  InputStream in = new FileInputStream(url);
					  AudioInputStream inputStream = AudioSystem.getAudioInputStream(in);           
					  clip.open(inputStream);           
					  clip.start();          
					  } catch (Exception e) {           
						  System.err.println(e.getMessage());        
						  }       
					  }     
			  }).start();  
		  } 

}
