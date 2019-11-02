package vlcj;

import java.awt.Component;
import java.awt.Dimension;
 
import javax.swing.JPanel;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayer;
import java.awt.BorderLayout;
 
public class AVPanel extends JPanel {
    private EmbeddedMediaPlayerComponent component;
    private EmbeddedMediaPlayer player;
    private int file_index = 0;
    private String[] av_list;
     
    public AVPanel()
    {
        boolean found = new NativeDiscovery().discover();
        System.out.println(found);
        System.out.println(LibVlc.INSTANCE.libvlc_get_version());
        component = new EmbeddedMediaPlayerComponent();
        player = component.getMediaPlayer();
 
        setLayout(new BorderLayout());
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(640, 480));
        setVisible(true);
        add(component, BorderLayout.CENTER);
        player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                endAV();
            }
        });
    }
     
    public void setAV(final String fnames[]) {
        av_list = fnames;
        file_index = 0;
        runAV();
    }
     
    private void runAV() {
        new Thread(new Runnable() {
            public void run() {
                player.playMedia(av_list[file_index]);
            }
        }).start();
    }
     
    private void endAV() {
        component.release();
        remove(component);
        component = new EmbeddedMediaPlayerComponent();
        player = component.getMediaPlayer();
        add(component, BorderLayout.CENTER);
        player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                endAV();
            }
        });
        file_index = ++ file_index % av_list.length;
        runAV();
    }
}