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
    private Clip MusicClip;
    URL[] soundURL = new URL[30] ;
    private final BlockingQueue<Runnable> soundQueue = new LinkedBlockingQueue<>();
    private volatile int musicVolume = UI.SLIDER_TICK_COUNT;
    private volatile int soundEffectVolume = UI.SLIDER_TICK_COUNT;

    public Sound(){
        soundURL[0]=getClass().getResource("/sound/menuet.wav") ;
        soundURL[1]=getClass().getResource("/sound/hitmonster.wav") ;
        soundURL[2]=getClass().getResource("/sound/parry.wav") ;
        soundURL[3]=getClass().getResource("/sound/swingweapon.wav") ;
        soundURL[4]=getClass().getResource("/sound/fanfare.wav") ;
        soundURL[5]=getClass().getResource("/sound/gameover.wav") ;

        Thread soundThread = new Thread( this,  "Sound Thread");
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
            stopClip(MusicClip );
            MusicClip = loadClip(i, musicVolume);
            if (MusicClip != null) {
                MusicClip.start();
                MusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        });
    }

    public void stopMusic() {
        soundQueue.offer(() -> stopClip(MusicClip));
    }

    public void playSoundEffect(int i) {
        soundQueue.offer(() -> {
            Clip soundEffect = loadClip(i, soundEffectVolume);
            if (soundEffect != null) {
                soundEffect.start();
            }
        });
    }


    public void setFile(int i){
        soundQueue.offer(() -> clip = loadClip(i, soundEffectVolume));
    }

    public void setMusicVolume(int volume) {
        musicVolume = clampVolume(volume);
        soundQueue.offer(() -> applyVolume(MusicClip, musicVolume));
    }

    public void setSoundEffectVolume(int volume) {
        soundEffectVolume = clampVolume(volume);
        soundQueue.offer(() -> applyVolume(clip, soundEffectVolume));
    }

    private Clip loadClip(int i, int volume) {
        try{

            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            Clip newClip = AudioSystem.getClip();
            newClip.open(ais);
            applyVolume(newClip, volume);
            return newClip;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void applyVolume(Clip targetClip, int volume) {
        if (targetClip == null || !targetClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gainControl = (FloatControl) targetClip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(volumeToDecibels(volume));
    }

    private int clampVolume(int volume) {
        return Math.max(0, Math.min(UI.SLIDER_TICK_COUNT, volume));
    }

    private float volumeToDecibels(int volume) {
        if (volume <= 0) {
            return -80.0f;
        }

        // UI Settings Buttons.png exposes nine green slider ticks; full volume preserves the old +2 dB mix.
        float normalized = (float) volume / UI.SLIDER_TICK_COUNT;
        return -30.0f + normalized * 32.0f;
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
