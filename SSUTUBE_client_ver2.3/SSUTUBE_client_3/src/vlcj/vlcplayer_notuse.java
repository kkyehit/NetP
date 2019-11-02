package vlcj;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.*;
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class vlcplayer_notuse extends JPanel {
	private EmbeddedMediaPlayerComponent component;
	private EmbeddedMediaPlayer player;
	
	private final AudioMediaPlayerComponent mediaPlayerComponent;
	
	public vlcplayer_notuse() {
		mediaPlayerComponent = new AudioMediaPlayerComponent();
        boolean found = new NativeDiscovery().discover();
        System.out.println(found);        
        component = new EmbeddedMediaPlayerComponent();
        player = component.getMediaPlayer();
 
        setLayout(new BorderLayout());
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(640, 480));
        setVisible(true);
        add(component, BorderLayout.CENTER);   
	}
	
}
