package vlcj;

import java.awt.*;

import javax.swing.*;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class Test {
	public static void main(String[] args){
		JFrame f = new JFrame();
		f.setLocation(100,100);
		f.setSize(1000,600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		AVPanel c = new AVPanel();
		f.add(c);
		
		String[] file = {"downloaded/BTS_Performs_Boys_With_Luv.mp4"};
		
		c.setAV(file);
	}
}