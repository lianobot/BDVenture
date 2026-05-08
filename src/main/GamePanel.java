package main;

import entity.Entity;
import entity.Player;
import tile.TileManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class GamePanel extends JPanel implements Runnable{

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
    TileManager tileM = new TileManager(this);
    public KeyHandler keyH = new KeyHandler(this);
    Sound sound = new Sound() ;
    public CollisionChecker cChecker= new CollisionChecker(this) ;
    public AssetSetter assetSetter = new AssetSetter(this);
    public UI ui = new UI(this);
    public EventHandler eventHandler = new EventHandler(this);
    Thread gameThread;

    //ENTITY AND OBJECT
    public Player player = new Player(this,keyH);
    public Entity[] obj = new Entity[10];
    public Entity[] npc = new Entity[10];
    public Entity[] monster = new Entity[10];
    ArrayList<Entity> entityArrayList = new ArrayList<>();

    //GAME STATE
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;

    public GamePanel(){

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
    public void startGameThread(){

        gameThread = new Thread(this);
        gameThread.start();
    }


    //delta method for 60 frame game loop.
    @Override
    public void run() {

        double drawInterval = (double) 1000000000 /FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null){

            currentTime = System.nanoTime();
            delta += (currentTime-lastTime)/drawInterval;
            lastTime = currentTime;

            if (delta >= 1){
                //1. UPDATE: update information such as character position.
                update();
                //2. DRAW: draw the screen.
                repaint();
                delta--;
            }


        }

    }
    public void update(){

        if (gameState == playState) {
            //PLAYER
            player.update();

            //NPC
            for (Entity entity : npc) {
                if (entity != null) {
                    entity.update();
                }
            }

            //MONSTER
            for (Entity entity : monster){
                if (entity != null){
                    entity.update();
                }
            }
        }
        if (gameState == pauseState){
            //nothing atm
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        //DEBUG
        long drawStart = 0;
        if (keyH.checkDrawTime) {
            drawStart = System.nanoTime();
        }


        //TITLE SCREEN
        if (gameState == titleState){
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

            for (Entity entity : monster) {
                if (entity != null) {
                    entityArrayList.add(entity);
                }
            }


            //SORT
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
            g2.drawString("Draw Time: " + passed, 10, 400);
        }

        g2.dispose();

    }
public void playMusic (int i){
        sound.setFile(i) ;
        sound.play() ;
        sound.loop() ;

}
public void stopMusic(){
        sound.stop();
}
public void playSE(int i){
        sound.setFile(i);
        sound.play();
}
}
