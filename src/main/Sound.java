package main;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Sound implements Runnable{
    private Clip clip;
    private Clip musicClip;
    URL[] soundURL = new URL[30] ;
    private final BlockingQueue<Runnable> soundQueue = new LinkedBlockingQueue<>();

    public Sound(){
        soundURL[0]=getClass().getResource("/sound/menuet.wav") ;
        soundURL[1]=getClass().getResource("/sound/hitmonster.wav") ;
        soundURL[2]=getClass().getResource("/sound/parry.wav") ;
        soundURL[3]=getClass().getResource("/sound/swingweapon.wav") ;
        soundURL[4]=getClass().getResource("/sound/fanfare.wav") ;
        soundURL[5]=getClass().getResource("/sound/gameover.wav") ;

        Thread soundThread = new Thread(this, "Sound Thread");
        soundThread.setDaemon(true);
        soundThread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                soundQueue.take().run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void playMusic(int i) {
        soundQueue.offer(() -> {
            stopClip(musicClip);
            musicClip = loadClip(i);
            if (musicClip != null) {
                musicClip.start();
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        });
    }

    public void stopMusic() {
        soundQueue.offer(() -> stopClip(musicClip));
    }

    public void playSoundEffect(int i) {
        soundQueue.offer(() -> {
            Clip soundEffect = loadClip(i);
            if (soundEffect != null) {
                soundEffect.start();
            }
        });
    }


    public void setFile(int i){
        soundQueue.offer(() -> clip = loadClip(i));
    }

    private Clip loadClip(int i) {
        try{

            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            Clip newClip = AudioSystem.getClip();
            newClip.open(ais);

            FloatControl gainControl = (FloatControl) newClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-13.0f);
            return newClip;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void stopClip(Clip clipToStop) {
        if (clipToStop != null) {
            clipToStop.stop();
            clipToStop.close();
        }
    }

    public void play(){
        soundQueue.offer(() -> {
            if (clip != null) {
                clip.start();
            }
        });
    }

    public void loop(){
        soundQueue.offer(() -> {
            if (clip != null) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        });
    }

    public void stop(){
        soundQueue.offer(() -> stopClip(clip));
    }
}