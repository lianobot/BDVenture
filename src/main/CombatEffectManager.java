package main;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CombatEffectManager {

    private static final int FLOAT_TEXT_LIFE = 45;
    private static final int PARTICLE_LIFE = 24;
    private static final int HIT_PAUSE_FRAMES = 5;
    private final GamePanel gp;
    private final Random random = new Random();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private int screenShakeCounter = 0;
    private int screenShakeStrength = 0;
    private int hitPauseCounter = 0;

    public CombatEffectManager(GamePanel gp) {
        this.gp = gp;
    }

    public void update() {
        if (hitPauseCounter > 0) {
            hitPauseCounter--;
        }
        if (screenShakeCounter > 0) {
            screenShakeCounter--;
        }

        Iterator<FloatingText> textIterator = floatingTexts.iterator();
        while (textIterator.hasNext()) {
            FloatingText text = textIterator.next();
            text.life--;
            text.worldY -= 1;
            if (text.life <= 0) {
                textIterator.remove();
            }
        }

        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle particle = particleIterator.next();
            particle.life--;
            particle.worldX += particle.velocityX;
            particle.worldY += particle.velocityY;
            particle.velocityY += 0.08;
            if (particle.life <= 0) {
                particleIterator.remove();
            }
        }
    }

    public void onMonsterHit(int worldX, int worldY, int damage) {
        addFloatingText(worldX, worldY, "-" + damage, new Color(235, 42, 50));
        spawnHitSparks(worldX, worldY);
        triggerHitPause();
        triggerScreenShake(8, 3);
    }

    public void onBlockDeflect(int worldX, int worldY) {
        addFloatingText(worldX, worldY, "BLOCK", new Color(183, 255, 132));
        triggerScreenShake(5, 2);
    }

    public void onGuardBreak(int worldX, int worldY) {
        addFloatingText(worldX, worldY, "TIRED", new Color(235, 42, 50));
        triggerScreenShake(6, 2);
    }

    public void onMonsterDefeated(int worldX, int worldY) {
        addFloatingText(worldX, worldY, "DEFEATED", new Color(255, 236, 150));
        for (int i = 0; i < 18; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 1.8 + random.nextDouble() * 2.8;
            particles.add(new Particle(
                    worldX,
                    worldY,
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed,
                    5 + random.nextInt(5),
                    new Color(183 + random.nextInt(50), 255, 132)
            ));
        }
        triggerScreenShake(12, 4);
    }

    public void spawnDodgeDust(int worldX, int worldY) {
        for (int i = 0; i < 3; i++) {
            particles.add(new Particle(
                    worldX + random.nextInt(18) - 9,
                    worldY + random.nextInt(10) - 5,
                    random.nextDouble() * 1.4 - 0.7,
                    -0.6 - random.nextDouble(),
                    5 + random.nextInt(4),
                    new Color(196, 174, 129)
            ));
        }
    }

    public void triggerScreenShake(int frames, int strength) {
        screenShakeCounter = Math.max(screenShakeCounter, frames);
        screenShakeStrength = Math.max(screenShakeStrength, strength);
    }

    public void triggerHitPause() {
        hitPauseCounter = Math.max(hitPauseCounter, HIT_PAUSE_FRAMES);
    }

    public boolean isHitPaused() {
        return hitPauseCounter > 0;
    }

    public int getShakeOffsetX() {
        if (screenShakeCounter <= 0) {
            return 0;
        }
        return random.nextInt(screenShakeStrength * 2 + 1) - screenShakeStrength;
    }

    public int getShakeOffsetY() {
        if (screenShakeCounter <= 0) {
            return 0;
        }
        return random.nextInt(screenShakeStrength * 2 + 1) - screenShakeStrength;
    }

    public void draw(Graphics2D g2) {
        for (Particle particle : particles) {
            int screenX = particle.worldX - gp.player.worldX + gp.player.screenX;
            int screenY = particle.worldY - gp.player.worldY + gp.player.screenY;
            float alpha = Math.max(0.15f, particle.life / (float) PARTICLE_LIFE);
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(particle.color);
            g2.fillRect(screenX, screenY, particle.size, particle.size);
            g2.setComposite(oldComposite);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        for (FloatingText text : floatingTexts) {
            int screenX = text.worldX - gp.player.worldX + gp.player.screenX;
            int screenY = text.worldY - gp.player.worldY + gp.player.screenY;
            g2.setColor(Color.black);
            g2.drawString(text.text, screenX + 2, screenY + 2);
            g2.setColor(text.color);
            g2.drawString(text.text, screenX, screenY);
        }
    }

    private void addFloatingText(int worldX, int worldY, String text, Color color) {
        floatingTexts.add(new FloatingText(worldX, worldY, text, color));
    }

    private void spawnHitSparks(int worldX, int worldY) {
        for (int i = 0; i < 10; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 1.4 + random.nextDouble() * 2.2;
            int size = 4 + random.nextInt(4);
            particles.add(new Particle(
                    worldX,
                    worldY,
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed,
                    size,
                    new Color(255, 220 + random.nextInt(30), 110)
            ));
        }
    }

    private static class FloatingText {
        int worldX;
        int worldY;
        int life = FLOAT_TEXT_LIFE;
        String text;
        Color color;

        FloatingText(int worldX, int worldY, String text, Color color) {
            this.worldX = worldX;
            this.worldY = worldY;
            this.text = text;
            this.color = color;
        }
    }

    private static class Particle {
        int worldX;
        int worldY;
        double velocityX;
        double velocityY;
        int size;
        int life = PARTICLE_LIFE;
        Color color;

        Particle(int worldX, int worldY, double velocityX, double velocityY, int size, Color color) {
            this.worldX = worldX;
            this.worldY = worldY;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.size = size;
            this.color = color;
        }
    }
}
