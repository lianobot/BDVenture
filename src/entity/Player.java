package entity;

import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Player extends Entity {

    KeyHandler keyH;
     public final int screenX;
     public final int screenY;
     private static final double DRAW_SCALE = 2.5;
     private static final int BODY_PIXELS_X = 18;
     private static final int BODY_PIXELS_Y = 30;
     private static final int BODY_PIXELS_WIDTH = 13;
     private static final int BODY_PIXELS_HEIGHT = 13;
     private static final int ATTACK_REACH = 24;
     private static final int ATTACK_PADDING = 4;
     private static final int MIN_KNOCKBACK_PIXELS = 20;
     private static final int MAX_KNOCKBACK_PIXELS = 30;
     private static final int KNOCKBACK_STEP_PIXELS = 4;
     private static final int BLOCK_DEFLECT_COOLDOWN = 20;
     private static final int DODGE_SPEED = 9;
     private static final int DODGE_DURATION = 14;
     private static final int DODGE_STAMINA_COST = 30;
     private static final int STAMINA_REGEN_DELAY = 18;
     private static final int BLOCK_STAMINA_DRAIN_INTERVAL = 12;
     private static final int BLOCK_STAMINA_DRAIN = 1;
     private static final int MONSTER_STAGGER_FRAMES = 60;
     private static final int SPRINT_STAMINA_DRAIN_INTERVAL = 8;
     private static final int SPRINT_STAMINA_DRAIN = 1;
     private static final int COMBO_WINDOW_FRAMES = 55;
     private static final int MAX_COMBO_STEP = 2;
     private static final int CONTACT_RECOIL_PIXELS = 18;

    // Player Stats
    public int attack = 1;
    public int defense = 0;
    public int maxStamina = 100;
    public int stamina = maxStamina;

    // Player State tracker
    public boolean isIdle = true;
    public volatile boolean isBlocking = false;
    public boolean isDodging = false;
    public int nearbyNpcIndex = 999;
    private volatile int blockDeflectCounter = 0;
    private int blockDrainCounter = 0;
    private int sprintDrainCounter = 0;
    private int dodgeCounter = 0;
    private int staminaRegenCounter = 0;
    private int comboStep = 0;
    private int comboTimer = 0;

    // Arrays to hold our 6-frame animations
    public BufferedImage[] idleUp = new BufferedImage[6];
    public BufferedImage[] idleDown = new BufferedImage[6];
    public BufferedImage[] idleLeft = new BufferedImage[6];
    public BufferedImage[] idleRight = new BufferedImage[6];

    public BufferedImage[] walkUp = new BufferedImage[6];
    public BufferedImage[] walkDown = new BufferedImage[6];
    public BufferedImage[] walkLeft = new BufferedImage[6];
    public BufferedImage[] walkRight = new BufferedImage[6];

    public BufferedImage[] attackUp = new BufferedImage[4];
    public BufferedImage[] attackDown = new BufferedImage[4];
    public BufferedImage[] attackLeft = new BufferedImage[4];
    public BufferedImage[] attackRight = new BufferedImage[4];


    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);
        this.keyH = keyH;

        screenX = gp.SCREEN_WIDTH / 2 - (gp.TILE_SIZE / 2);
        screenY = gp.SCREEN_HEIGHT / 2 - (gp.TILE_SIZE / 2);

        //Collision Box
        solidArea = createBodyCollisionBox();
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        //Attack Hitbox
        attackArea.width = ATTACK_REACH;
        attackArea.height = ATTACK_REACH;

        spriteNum = 0;

        setDefaultValues();
        getImage();
    }

    public void setDefaultValues() {

         worldX = gp.TILE_SIZE * 78;
         worldY = gp.TILE_SIZE * 63;

        speed = 4;
        direction = "down";

        //PLAYER STATUS
        maxLife = 6;
        life = maxLife;

        isIdle = true;
        isBlocking = false;
        isDodging = false;
        nearbyNpcIndex = 999;
        blockDeflectCounter = 0;
        blockDrainCounter = 0;
        sprintDrainCounter = 0;
        dodgeCounter = 0;
        stamina = maxStamina;
        staminaRegenCounter = 0;
        comboStep = 0;
        comboTimer = 0;
        attacking = false;
        invincible = false;
        invincibleCounter = 0;
        spriteNum = 0;
        spriteCounter = 0;
        collisionOn = false;
    }

    public void getImage() {
        try {
             // Read the main 48x48 sprite sheet
            BufferedImage spriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/player.png")));
            int size = 48;

            for (int i = 0; i < 6; i++) {


                // IDLE ANIMATIONS
                idleDown[i] = spriteSheet.getSubimage(i * size, 0, size, size);
                idleRight[i] = spriteSheet.getSubimage(i * size, size, size, size);
                idleLeft[i] = flipImage(idleRight[i]);
                idleUp[i] = spriteSheet.getSubimage(i * size, 2 * size, size, size);


                // MOVE ANIMATIONS
                walkDown[i] = spriteSheet.getSubimage(i * size, 3 * size, size, size);
                walkRight[i] = spriteSheet.getSubimage(i * size, 4 * size, size, size);
                walkLeft[i] = flipImage(walkRight[i]);
                walkUp[i] = spriteSheet.getSubimage(i * size, 5 * size, size, size);

                // ATTACK ANIMATIONS
                if (i < 4) {
                    attackDown[i] = spriteSheet.getSubimage(i * size, 6 * size, size, size);
                    attackRight[i] = spriteSheet.getSubimage(i * size, 7 * size, size, size);
                    attackLeft[i] = flipImage(attackRight[i]);
                    attackUp[i] = spriteSheet.getSubimage(i * size, 8 * size, size, size);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // HELPER FUNCTION : CONVERT RIGHT IMAGE TO LEFT
    public BufferedImage flipImage(BufferedImage img) {
        BufferedImage flipped = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flipped.createGraphics();
        g.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
        g.dispose();
        return flipped;
    }

    //Player HitBox
    private Rectangle createBodyCollisionBox() {
        int drawSize = (int) (gp.TILE_SIZE * DRAW_SCALE);
        int drawOffsetX = -(drawSize / 3);
        int drawOffsetY = -(drawSize / 2);

        int x = drawOffsetX + (int) Math.round(BODY_PIXELS_X * DRAW_SCALE);
        int y = drawOffsetY + (int) Math.round(BODY_PIXELS_Y * DRAW_SCALE);
        int width = (int) Math.round(BODY_PIXELS_WIDTH * DRAW_SCALE);
        int height = (int) Math.round(BODY_PIXELS_HEIGHT * DRAW_SCALE);

        return new Rectangle(x, y, width, height);
    }

    //Player AttackBox
    private Rectangle getAttackWorldBox() {
        Rectangle bodyBox = new Rectangle(
                worldX + solidAreaDefaultX,
                worldY + solidAreaDefaultY,
                solidArea.width,
                solidArea.height
        );

        return switch (direction) {
            case "up" -> new Rectangle(
                    bodyBox.x - ATTACK_PADDING,
                    bodyBox.y - ATTACK_REACH * 2,
                    bodyBox.width + ATTACK_PADDING * 2,
                    ATTACK_REACH * 2
            );
            case "down" -> new Rectangle(
                    bodyBox.x - ATTACK_PADDING,
                    bodyBox.y + bodyBox.height,
                    bodyBox.width + ATTACK_PADDING * 2,
                    ATTACK_REACH * 2
            );
            case "left" -> new Rectangle(
                    bodyBox.x - ATTACK_REACH * 2,
                    bodyBox.y,
                    ATTACK_REACH * 2,
                    bodyBox.height
            );
            case "right" -> new Rectangle(
                    bodyBox.x + bodyBox.width,
                    bodyBox.y,
                    ATTACK_REACH * 2,
                    bodyBox.height
            );
            default -> bodyBox;
        };
    }

    private int checkAttackMonster(Rectangle attackWorldBox) {
          synchronized (gp.monsterLock) {
            for (int i = 0; i < gp.monster.length; i++) {
                if (gp.monster[i] != null) {
                    Rectangle monsterBox = new Rectangle(
                            gp.monster[i].worldX + gp.monster[i].solidArea.x,
                            gp.monster[i].worldY + gp.monster[i].solidArea.y,
                            gp.monster[i].solidArea.width,
                            gp.monster[i].solidArea.height
                    );

                    if (attackWorldBox.intersects(monsterBox)) {
                        return i;
                    }
                }
            }
        }

        return 999;
    }

    public void update() {
        if (keyH.dodgePressed) {
            tryStartDodge();
            keyH.dodgePressed = false;
        }

        if (isDodging) {
            updateDodge();
            updateAnimationTicker();
            updateInvincibilityTimer();
            return;
        }

        isBlocking = keyH.blockPressed;
        if (isBlocking && attacking) {
            attacking = false;
            spriteCounter = 0;
            spriteNum = 0;
        }

        if (attacking) {
            isBlocking = false;
            attacking();
        } else {
            // ACTIVE SHIELD BLOCKING: Shift toggles the defensive stance while gameplay is active.
            isBlocking = keyH.blockPressed;

            // CHECK NPC COLLISION
            collisionOn = false;
            nearbyNpcIndex = gp.cChecker.checkEntity(this, gp.npc);
            interactNPC(nearbyNpcIndex);

            if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
                isIdle = false;

                if (keyH.upPressed) { direction = "up"; }
                if (keyH.downPressed) { direction = "down"; }
                if (keyH.leftPressed) { direction = "left"; }
                if (keyH.rightPressed) { direction = "right"; }

                // Normalize Speed
                double currentSpeed = speed;

                // ACTIVE SHIELD BLOCKING: blocking trades offense for defense by halving movement speed.
                if (isBlocking) {
                    currentSpeed *= 0.5;
                }

                if (isSprinting()) {
                    currentSpeed *= 1.5;
                    drainSprintStamina();
                } else {
                    sprintDrainCounter = 0;
                }

                // Check if moving diagonally
                boolean isDiagonal = (keyH.upPressed || keyH.downPressed) && (keyH.leftPressed || keyH.rightPressed);

                if (isDiagonal) {
                    currentSpeed *= 0.85;
                }

                int oldSpeed = speed;
                speed = Math.max(1, (int) Math.ceil(currentSpeed));

                // CHECK TILE COLLISION
                gp.cChecker.checkTile(this);

                // CHECK OBJECT COLLISION
                int objIndex = gp.cChecker.checkObject(this, true);
                pickUpObject(objIndex);

                // CHECK MONSTER COLLISION
                int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
                contactMonster(monsterIndex);

                // CHECK EVENT
                gp.eventHandler.checkEvent();

                speed = oldSpeed;

                // IF COLLISION FALSE, PLAYER MAY MOVE
                if (!collisionOn && !keyH.ePressed) {
                    if (keyH.upPressed) { worldY -= (int)currentSpeed; }
                    if (keyH.downPressed) { worldY += (int)currentSpeed; }
                    if (keyH.leftPressed) { worldX -= (int)currentSpeed; }
                    if (keyH.rightPressed) { worldX += (int)currentSpeed; }
                }
            } else {
                isIdle = true;
            }
        }

        updateAnimationTicker();
        updateInvincibilityTimer();
        updateStaminaRegen();
        updateComboTimer();

        if (blockDeflectCounter > 0) {
            blockDeflectCounter--;
        }
    }

    private void updateAnimationTicker() {
        spriteCounter++;
        if (!attacking) {
            if (spriteCounter >= 13) {
                spriteNum++;
                if (spriteNum >= 6) { // Loop back to 0 after the 6th frame
                    spriteNum = 0;
                }
                spriteCounter = 0;
            }
        }
    }

    private void updateInvincibilityTimer() {
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 60) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }

    private void updateStaminaRegen() {
        if (isBlocking) {
            blockDrainCounter++;
            if (blockDrainCounter >= BLOCK_STAMINA_DRAIN_INTERVAL) {
                stamina = Math.max(0, stamina - BLOCK_STAMINA_DRAIN);
                blockDrainCounter = 0;
            }

            if (stamina == 0) {
                isBlocking = false;
                keyH.blockPressed = false;
                gp.effects.onGuardBreak(worldX, worldY);
            }
            staminaRegenCounter = 0;
            return;
        }

        blockDrainCounter = 0;
        if (attacking || isDodging || isSprinting() || stamina >= maxStamina) {
            staminaRegenCounter = 0;
            return;
        }

        staminaRegenCounter++;
        if (staminaRegenCounter >= STAMINA_REGEN_DELAY) {
            stamina = Math.min(maxStamina, stamina + 2);
            staminaRegenCounter = 0;
        }
    }

    public boolean canDodge() {
        return stamina >= DODGE_STAMINA_COST && !attacking && !isBlocking && !isDodging;
    }

    public boolean isSprinting() {
        return keyH.sprintPressed && stamina > 0 && !attacking && !isBlocking && !isDodging;
    }

    private void drainSprintStamina() {
        sprintDrainCounter++;
        if (sprintDrainCounter >= SPRINT_STAMINA_DRAIN_INTERVAL) {
            stamina = Math.max(0, stamina - SPRINT_STAMINA_DRAIN);
            sprintDrainCounter = 0;
        }
    }

    private void updateComboTimer() {
        if (comboTimer > 0) {
            comboTimer--;
            if (comboTimer == 0) {
                comboStep = 0;
            }
        }
    }

    private void tryStartDodge() {
        if (!canDodge()) {
            return;
        }

        if (keyH.upPressed) { direction = "up"; }
        else if (keyH.downPressed) { direction = "down"; }
        else if (keyH.leftPressed) { direction = "left"; }
        else if (keyH.rightPressed) { direction = "right"; }

        stamina -= DODGE_STAMINA_COST;
        staminaRegenCounter = 0;
        isDodging = true;
        isBlocking = false;
        dodgeCounter = DODGE_DURATION;
        invincible = true;
        invincibleCounter = 0;
        isIdle = false;
    }

    private void updateDodge() {
        gp.effects.spawnDodgeDust(
                worldX + solidAreaDefaultX + solidArea.width / 2,
                worldY + solidAreaDefaultY + solidArea.height
        );
        moveInFacingDirection(DODGE_SPEED);
        dodgeCounter--;

        if (dodgeCounter <= 0) {
            isDodging = false;
            invincible = false;
            invincibleCounter = 0;
        }
    }

    private void moveInFacingDirection(int moveSpeed) {
        int oldSpeed = speed;
        speed = moveSpeed;
        collisionOn = false;

        gp.cChecker.checkTile(this);
        gp.cChecker.checkObject(this, true);
        gp.cChecker.checkEntity(this, gp.npc);
        gp.cChecker.checkEntity(this, gp.monster);

        if (!collisionOn) {
            switch (direction) {
                case "up" -> worldY -= moveSpeed;
                case "down" -> worldY += moveSpeed;
                case "left" -> worldX -= moveSpeed;
                case "right" -> worldX += moveSpeed;
            }
        }

        speed = oldSpeed;
    }

    public void attacking() {

        spriteCounter++;

        if (spriteCounter <= 10) {
            spriteNum = 0;
        }

        if (spriteCounter > 10 && spriteCounter <= 20) {
            spriteNum = 1;
        }

        if (spriteCounter > 20 && spriteCounter <= 30) {
            spriteNum = 2;

            int monsterIndex = checkAttackMonster(getAttackWorldBox());
            damageMonster(monsterIndex);

        }

        if (spriteCounter > 30 && spriteCounter <= 40) {
            spriteNum = 3;
        }

        if (spriteCounter > 40) {
            spriteNum = 0;
            spriteCounter = 0;
            attacking = false;
        }

    }

    public void pickUpObject(int i) {
        if (i != 999) {
            //add objects
        }
    }

    public void interactNPC(int i) {
        if (gp.keyH.ePressed) {
            if (i != 999) {
                gp.gameState = gp.dialogueState;
                gp.player.dialogueIndex = i;
                gp.npc[i].speak();
            } else {
                // Only start attack if we aren't already attacking
                // ACTIVE SHIELD BLOCKING: the player cannot begin an attack while holding block.
                if (!attacking && !keyH.blockPressed) {
                    if (comboTimer > 0) {
                        comboStep = Math.min(MAX_COMBO_STEP, comboStep + 1);
                    } else {
                        comboStep = 0;
                    }
                    comboTimer = COMBO_WINDOW_FRAMES;
                    gp.playSE(GamePanel.SE_SWING_WEAPON);
                    attacking = true;
                    spriteCounter = 0; // Reset counter for animation
                    spriteNum = 0;
                }
            }
        }
        gp.keyH.ePressed = false;
    }

    public void contactMonster(int i) {

        if (i != 999) {
            synchronized (gp.monsterLock) {
                if (gp.monster[i] != null && !invincible && !gp.monster[i].dying) {
                    receiveMonsterDamage(gp.monster[i]);
                }
            }
        }
    }

    public void receiveMonsterDamage(Entity monster) {
        if (monster == null || invincible || monster.dying) {
            return;
        }

        if (blockMonsterContact(monster)) {
            return;
        }

        gp.playSE(GamePanel.SE_PLAYER_HURT);
        life -= 1;
        applyContactRecoil(monster);

        if (life <= 0){
            gp.triggerGameOver();
        }
        invincible = true;
    }

    private void applyContactRecoil(Entity source) {
        if (source == null) {
            return;
        }

        int playerCenterX = worldX + solidAreaDefaultX + solidArea.width / 2;
        int playerCenterY = worldY + solidAreaDefaultY + solidArea.height / 2;
        int sourceCenterX = source.worldX + source.solidAreaDefaultX + source.solidArea.width / 2;
        int sourceCenterY = source.worldY + source.solidAreaDefaultY + source.solidArea.height / 2;
        int deltaX = playerCenterX - sourceCenterX;
        int deltaY = playerCenterY - sourceCenterY;

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            pushPlayerWithTileCollision(deltaX >= 0 ? CONTACT_RECOIL_PIXELS : -CONTACT_RECOIL_PIXELS, 0);
        } else {
            pushPlayerWithTileCollision(0, deltaY >= 0 ? CONTACT_RECOIL_PIXELS : -CONTACT_RECOIL_PIXELS);
        }
    }

    private void pushPlayerWithTileCollision(int deltaX, int deltaY) {
        int remaining = Math.max(Math.abs(deltaX), Math.abs(deltaY));
        int xSign = Integer.compare(deltaX, 0);
        int ySign = Integer.compare(deltaY, 0);
        String pushDirection = Math.abs(deltaX) > 0 ? (xSign > 0 ? "right" : "left") : (ySign > 0 ? "down" : "up");

        String oldDirection = direction;
        int oldSpeed = speed;
        boolean oldCollisionOn = collisionOn;

        while (remaining > 0) {
            int step = Math.min(KNOCKBACK_STEP_PIXELS, remaining);
            int stepX = xSign * step;
            int stepY = ySign * step;

            direction = pushDirection;
            speed = step;
            collisionOn = false;
            gp.cChecker.checkTile(this);
            if (collisionOn) {
                break;
            }

            worldX += stepX;
            worldY += stepY;
            remaining -= step;
        }

        direction = oldDirection;
        speed = oldSpeed;
        collisionOn = oldCollisionOn;
    }

    public boolean blockMonsterContact(Entity monster) {
        if (!isBlockingHitFromFront(monster)) {
            return false;
        }

        // ACTIVE SHIELD BLOCKING: a short cooldown prevents the parry sound from stacking every frame.
        if (blockDeflectCounter == 0) {
            gp.playSE(GamePanel.SE_BLOCK_DEFLECT);
            gp.effects.onBlockDeflect(monster.worldX, monster.worldY);
            blockDeflectCounter = BLOCK_DEFLECT_COOLDOWN;
        }
        return true;
    }

    public boolean isBlockingHitFromFront(Entity attacker) {
        if (!isBlocking || attacker == null || attacker.dying) {
            return false;
        }

        Rectangle playerBox = getEntityWorldBox(this);
        Rectangle attackerBox = getEntityWorldBox(attacker);

        int playerCenterX = playerBox.x + playerBox.width / 2;
        int playerCenterY = playerBox.y + playerBox.height / 2;
        int attackerCenterX = attackerBox.x + attackerBox.width / 2;
        int attackerCenterY = attackerBox.y + attackerBox.height / 2;

        // ACTIVE SHIELD BLOCKING: vector from player center to monster center determines the attack side.
        int deltaX = attackerCenterX - playerCenterX;
        int deltaY = attackerCenterY - playerCenterY;
        int sideToleranceX = playerBox.width / 2 + attackerBox.width;
        int sideToleranceY = playerBox.height / 2 + attackerBox.height;

        return switch (direction) {
            case "up" -> deltaY <= 0 && Math.abs(deltaX) <= sideToleranceX;
            case "down" -> deltaY >= 0 && Math.abs(deltaX) <= sideToleranceX;
            case "left" -> deltaX <= 0 && Math.abs(deltaY) <= sideToleranceY;
            case "right" -> deltaX >= 0 && Math.abs(deltaY) <= sideToleranceY;
            default -> false;
        };
    }

    private Rectangle getEntityWorldBox(Entity entity) {
        return new Rectangle(
                entity.worldX + entity.solidAreaDefaultX,
                entity.worldY + entity.solidAreaDefaultY,
                entity.solidArea.width,
                entity.solidArea.height
        );
    }

    private void damageMonster(int i) {

        if (i != 999) {
            synchronized (gp.monsterLock) {
                if (gp.monster[i] != null && !gp.monster[i].invincible) {

                    gp.playSE(GamePanel.SE_HIT_MONSTER);

                    int damage = attack + comboStep;
                    gp.monster[i].life -= damage;
                    gp.monster[i].invincible = true;
                    gp.monster[i].damaged = true;
                    gp.monster[i].staggerCounter = MONSTER_STAGGER_FRAMES;
                    gp.effects.onMonsterHit(gp.monster[i].worldX, gp.monster[i].worldY, damage);

                    applyAttackKnockback(gp.monster[i]);

                    if (gp.monster[i].life <= 0) {
                        gp.monstersDefeated++;
                        gp.effects.onMonsterDefeated(gp.monster[i].worldX, gp.monster[i].worldY);
                        gp.monster[i].dying = true;
                        gp.monster[i].spriteNum = 0;
                        gp.monster[i].spriteCounter = 0;

                    }
                }
            }
        }
    }

    public int getComboStep() {
        return comboStep;
    }

    private void applyAttackKnockback(Entity monster) {
        int knockbackDistance = Math.min(MAX_KNOCKBACK_PIXELS, MIN_KNOCKBACK_PIXELS + attack * 5);
        int deltaX = 0;
        int deltaY = 0;

        // ATTACK KNOCKBACK: direction vector is derived from the player's current facing.
        switch (direction) {
            case "up" -> deltaY = -knockbackDistance;
            case "down" -> deltaY = knockbackDistance;
            case "left" -> deltaX = -knockbackDistance;
            case "right" -> deltaX = knockbackDistance;
        }

        pushEntityWithTileCollision(monster, deltaX, deltaY);
    }

    private void pushEntityWithTileCollision(Entity entity, int deltaX, int deltaY) {
        if (deltaX != 0) {
            pushEntityAxis(entity, deltaX, 0, deltaX > 0 ? "right" : "left");
        }
        if (deltaY != 0) {
            pushEntityAxis(entity, 0, deltaY, deltaY > 0 ? "down" : "up");
        }
    }

    private void pushEntityAxis(Entity entity, int deltaX, int deltaY, String pushDirection) {
        int remaining = Math.max(Math.abs(deltaX), Math.abs(deltaY));
        int xSign = Integer.compare(deltaX, 0);
        int ySign = Integer.compare(deltaY, 0);

        String oldDirection = entity.direction;
        int oldSpeed = entity.speed;
        boolean oldCollisionOn = entity.collisionOn;

        while (remaining > 0) {
            int step = Math.min(KNOCKBACK_STEP_PIXELS, remaining);
            int stepX = xSign * step;
            int stepY = ySign * step;

            if (!canPushEntity(entity, pushDirection, step, stepX, stepY)) {
                break;
            }

            entity.worldX += stepX;
            entity.worldY += stepY;
            remaining -= step;
        }

        entity.direction = oldDirection;
        entity.speed = oldSpeed;
        entity.collisionOn = oldCollisionOn;
    }

    private boolean canPushEntity(Entity entity, String pushDirection, int step, int stepX, int stepY) {
        if (!isInsideWorldAfterStep(entity, stepX, stepY)) {
            return false;
        }

        String oldDirection = entity.direction;
        int oldSpeed = entity.speed;
        boolean oldCollisionOn = entity.collisionOn;

        // ATTACK KNOCKBACK: reuse tile collision by temporarily testing the push step as movement.
        entity.direction = pushDirection;
        entity.speed = step;
        entity.collisionOn = false;
        gp.cChecker.checkTile(entity);
        boolean canMove = !entity.collisionOn;

        entity.direction = oldDirection;
        entity.speed = oldSpeed;
        entity.collisionOn = oldCollisionOn;
        return canMove;
    }

    private boolean isInsideWorldAfterStep(Entity entity, int stepX, int stepY) {
        int left = entity.worldX + stepX + entity.solidAreaDefaultX;
        int right = left + entity.solidArea.width;
        int top = entity.worldY + stepY + entity.solidAreaDefaultY;
        int bottom = top + entity.solidArea.height;

        return left >= 0
                && top >= 0
                && right < gp.MAX_WORLD_COL * gp.TILE_SIZE
                && bottom < gp.MAX_WORLD_ROW * gp.TILE_SIZE;
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        if (attacking) {
            image = switch (direction) {
                case "up" -> attackUp[spriteNum];
                case "down" -> attackDown[spriteNum];
                case "left" -> attackLeft[spriteNum];
                case "right" -> attackRight[spriteNum];
                default -> image;

            };
        } else if (isIdle) {
            image = switch (direction) {
                case "up" -> idleUp[spriteNum];
                case "down" -> idleDown[spriteNum];
                case "left" -> idleLeft[spriteNum];
                case "right" -> idleRight[spriteNum];
                default -> image;
            };
        } else {
            image = switch (direction) {
                case "up" -> walkUp[spriteNum];
                case "down" -> walkDown[spriteNum];
                case "left" -> walkLeft[spriteNum];
                case "right" -> walkRight[spriteNum];
                default -> image;
            };
        }
        double multiplier = 2.5;
        int drawSize = (int) (gp.TILE_SIZE * multiplier);

        // Offset the X and Y so the character stays centered on their collision box
        int x = screenX - (drawSize / 3);
        int y = screenY - (drawSize / 2);

        // HURT BLINKING: skip only the sprite on alternating 4-frame windows for retro i-frame flicker.
        if (!invincible || (invincibleCounter / 4) % 2 != 0) {
            g2.drawImage(image, x, y, drawSize, drawSize, null);
        }

        //DEBUG: SHOWS HIT BOX AND ATTACK BOX
        if (gp.keyH.showCollisionBox) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.red);
            g2.drawRect(screenX + solidAreaDefaultX, screenY + solidAreaDefaultY, solidArea.width, solidArea.height);

            if (attacking) {
                Rectangle attackBox = getAttackWorldBox();
                int attackScreenX = attackBox.x - worldX + screenX;
                int attackScreenY = attackBox.y - worldY + screenY;

                g2.setColor(Color.green);
                g2.drawRect(attackScreenX, attackScreenY, attackBox.width, attackBox.height);
            }
            g2.setStroke(oldStroke);
        }
    }
}
