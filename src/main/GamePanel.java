package main;

import entity.Entity;
import entity.Player;
import tile.TileManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GamePanel extends JPanel implements Runnable {

    //SCREEN SETTINGS
     final int ORIGINAL_TILE_SIZE = 16; // 16x16 tile
     final int SCALE = 3;

     public final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE; // 48x48 tile

     public final double MAX_SCREEN_COLUMN = 27;
     public final double MAX_SCREEN_ROW = 15;
     public final int SCREEN_WIDTH = (int) (TILE_SIZE * MAX_SCREEN_COLUMN); // 1440 pixels
     public final int SCREEN_HEIGHT = (int) (TILE_SIZE * MAX_SCREEN_ROW); // 810 pixels

    //WORLD SETTINGS
    public final int MAX_WORLD_COL = 128;
    public final int MAX_WORLD_ROW = 128;

    //FPS
    int FPS = 60;

    //SYSTEM
    Font arial_40 = new Font("Arial", Font.PLAIN, 40);
    TileManager tileM = new TileManager(this);
    public KeyHandler keyH = new KeyHandler(this);
    Sound sound = new Sound();
    public CollisionChecker cChecker = new CollisionChecker(this);
    public AssetSetter assetSetter = new AssetSetter(this);
    public UI ui = new UI(this);
    public EventHandler eventHandler = new EventHandler(this);
    volatile Thread gameThread;
    Thread enemyLogicThread;
    Thread playtimeMonitorThread;
    private volatile boolean workerThreadsRunning = false;

    //ENTITY AND OBJECT
    public Player player = new Player(this, keyH);
    public Entity[] obj = new Entity[10];
    public Entity[] npc = new Entity[10];
    public Entity[] monster = new Entity[10];
    public final Object monsterLock = new Object();
    ArrayList<Entity> entityArrayList = new ArrayList<>();
    public int playTimeSeconds = 0;

    //GAME STATE
    public volatile int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;
    public final int gameOverState = 4;
    private long gameOverStartTime;
    private boolean gameOverSoundPlayed = false;

    public GamePanel() {

        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
    }

    public void setupGame() {

        assetSetter.setObject();
        assetSetter.setNPC();
        assetSetter.setMonster();
        gameState = titleState;
    }

    public void startGameThread() {

        workerThreadsRunning = true;
        enemyLogicThread = new Thread(new EnemyLogicWorker(), "Enemy Logic Thread");
        enemyLogicThread.start();

        playtimeMonitorThread = new Thread(new PlaytimeMonitor(), "Playtime Monitor Thread");
        playtimeMonitorThread.setDaemon(true);
        playtimeMonitorThread.start();

        gameThread = new Thread(this);
        gameThread.start();
    }


    //delta method for 60 frame game loop.
    @Override
    public void run() {

        double drawInterval = (double) 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {

            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                //1. UPDATE: update information such as character position.
                update();
                //2. DRAW: draw the screen.
                repaint();
                delta--;
            }


        }

    }

    public void update() {

        if (gameState == playState) {
            //PLAYER
            player.update();

            //NPC
            for (Entity entity : npc) {
                if (entity != null) {
                    entity.update();
                }
            }

            //Check if player died
            if (player.life <= 0){
                triggerGameOver();
            }
        }
        if (gameState == pauseState) {
            //nothing atm
        }
    }

    public void triggerGameOver(){
        if (gameState != gameOverState){
            gameState = gameOverState;
            gameOverStartTime = System.currentTimeMillis();

            if (!gameOverSoundPlayed){
                stopMusic();
                playSE(5);
                gameOverSoundPlayed = true;
            }
        }
    }

    //After 3 seconds of death, Show prompt to return to title
    public boolean isGameOverPromptReady(){
        return gameState == gameOverState && System.currentTimeMillis() - gameOverStartTime >= 3000;
    }

    public void returnToTileScreen(){
        stopMusic();
        keyH.resetMovementKeys();
        player.setDefaultValues();
        synchronized (monsterLock){
            Arrays.fill(monster, null);
        }
        assetSetter.setMonster();
        gameOverSoundPlayed = false;
        gameState = titleState;
    }

    //MONSTER
    private void updateMonsters() {
         synchronized (monsterLock) {
            for (int i = 0; i < monster.length; i++) {
                if (monster[i] != null) {
                    if (monster[i].alive) {
                        monster[i].update();
                    }
                    // If the monster has finished its dying animation
                    if (!monster[i].alive) {
                        monster[i] = null;
                    }
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        //DEBUG
        long drawStart = 0;
        if (keyH.checkDrawTime) {
            drawStart = System.nanoTime();
        }


        //TITLE SCREEN
        if (gameState == titleState) {
            ui.draw(g2);
        }
        //OTHERS
        else {

            //TILE
            tileM.draw(g2);

            //ADD all ENTITIES TO LIST
            entityArrayList.add(player);
            for (Entity entity : npc) {
                if (entity != null) {
                    entityArrayList.add(entity);
                }
            }
            for (Entity entity : obj) {
                if (entity != null) {
                    entityArrayList.add(entity);
                }
            }
            synchronized (monsterLock) {
                for (Entity entity : monster) {
                    if (entity != null) {
                        entityArrayList.add(entity);
                    }
                }
            }


            //SORT by Y level of entities
            entityArrayList.sort((e1, e2) -> {

                int result = Integer.compare(e1.worldY, e2.worldY);
                return result;
            });

            //DRAW ENTITIES
            for (Entity entity : entityArrayList) {
                entity.draw(g2);
            }

            //Empty the list
            entityArrayList.clear();

            //UI
            ui.draw(g2);

        }

        //DEBUG
        if (keyH.checkDrawTime) {
            long drawEnd = System.nanoTime();
            long passed = drawEnd - drawStart;
            g2.setColor(Color.white);
            g2.setFont(arial_40.deriveFont(Font.BOLD, 28F));
            g2.drawString("Draw Time: " + passed, 10, 400);
        }

        g2.dispose();

    }

    public void playMusic(int i) {
        sound.playMusic(i);
    }

    public void stopMusic() {
        sound.stopMusic();
    }

    public void playSE(int i) {
        sound.playSoundEffect(i);
    }

    public int getPlayTimeSeconds() {
        return playTimeSeconds;
    }

    private class EnemyLogicWorker implements Runnable {
        @Override
        public void run() {
            long sleepTime = 1000 / FPS;

            while (workerThreadsRunning) {
                if (gameState == playState) {
                    updateMonsters();
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private class PlaytimeMonitor implements Runnable {
        @Override
        public void run() {
            while (workerThreadsRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                playTimeSeconds++;

            }
        }
    }
}
