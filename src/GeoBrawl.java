package src;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.AdvancedServoPhidget;
import com.phidgets.PhidgetException;
import processing.core.PApplet;
import processing.core.PFont;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class GeoBrawl extends PApplet {
    static int[] mapSize = {1440, 900};
    static int fontColor = 230;

    static public ArrayList<PlayerUnit> playerUnits;
    static public ArrayList<Coin> coins;

    static InterfaceKitPhidget ik;
    static AdvancedServoPhidget servo;

    // Allow for playing without attaching the servo.
    static boolean servoOn;

    long startTime;
    int lastTime = 0;
    int duration = 25;

    String winner;

    public void setup() {
        background(255);
        frameRate(60);
        // Coins and players will be stored in these ArrayLists.
        coins = new ArrayList<Coin>();
        playerUnits = new ArrayList<PlayerUnit>();

        // Spawn both players and a starting number of coins.
        spawnCoins(5);
        spawnPlayer(1);
        spawnPlayer(2);

        setupPhidgets();

        startTime = System.nanoTime();
    }

    void spawnPlayer(int playerID) {
        playerUnits.add(new Circle(playerID));
    }

    void spawnCoins(int n) {
        for (int i = 0; i < n; i++) {
            coins.add(new Coin());
        }
    }

    void spawnCoinsEnd(int n, int value) {
        for (int i = 0; i < n; i++) {
            coins.add(new Coin(value));
        }
    }

    public void settings() {
        size(mapSize[0], mapSize[1]);
    }

    public void mainGameLoop() {
        // Put new background over previous frame.
        background(51);
        lastTime = (int) TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
        lastTime = duration - lastTime;
        displayTime();
        // Collide with other players.
        if (playerUnits.get(0).checkUnitCollision(playerUnits.get(1))) {
            playerUnits.get(0).collide();
        }

        // Deal with both collisions after each other, to avoid the shapes getting stuck in each other.
        if (playerUnits.get(1).checkUnitCollision(playerUnits.get(0))) {
            playerUnits.get(1).collide();
        }

        // Go through list of players and execute update functions
        for (PlayerUnit player : playerUnits) {
            player.displayScore(this);
            // Make sure the player does not leave the playing field.
            player.checkWallCollision();

            for (int i = coins.size() - 1; i >= 0; i--) {
                coins.get(i).drawUnit(this);

                // Let players pickup coins.
                if (player.checkUnitCollision(coins.get(i))) {
                    player.score += coins.get(i).coinValue;
                    coins.remove(i);
                    spawnCoins(1);
                }
            }

            player.updatePosition();
            player.drawUnit(this);
        }
    }

    public void draw() {

        // Execute the main game loop while time has not run out.
        if (lastTime >= 0) {
            mainGameLoop();
        }

        else if (lastTime < 0  && lastTime >= -5){
            wrapUpGame();
        }

        else {

            exit();
        }
    }

    @Override
    public void exit() {
        if ((servoOn) && !(winner.equals("It's a draw!"))) {
            controlServo();
            super.exit();
        }
    }

    public void wrapUpGame() {

        lastTime = (int) TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
        lastTime = duration - lastTime;

        if (playerUnits.get(0).score > playerUnits.get(1).score) {
            this.winner = "1";
        }
        else if (playerUnits.get(0).score < playerUnits.get(1).score) {
            this.winner = "2";
        }
        else {
            this.winner = "It's a draw!";
        }

        PFont f;
        f = createFont("Helvetica",100,true);
        textFont(f,100);
        fill(fontColor);

        if (this.winner.equals("It's a draw!")) {
            text(winner, mapSize[0] / 3, mapSize[1] / 2);
        }

        else {
            text("Player " + winner + " wins!", mapSize[0] / 3, mapSize[1] / 2);
        }

        spawnCoinsEnd(1, 0);
        for (int i = coins.size() - 1; i >= 0; i--) {
            coins.get(i).drawUnit(this);
        }


    }

    void controlServo() {
        try {
            System.out.println("Attempting to move servo");
            servo.setEngaged(0, true);
            if (this.winner.equals("1")) {
                servo.setPosition(0, 160);
            }
            else if (this.winner.equals("2")) {
                servo.setPosition(0, 50);
            }

        }
        catch (PhidgetException e) {
            System.out.println("Servo error.");
        }
    }

    void displayTime() {
        PFont f;
        f = createFont("Helvetica",36,true);
        textFont(f,36);
        fill(fontColor);
        if (lastTime <= 9) {
            text("00:0" + lastTime, mapSize[0] / 2, 40);
        }
        else {
            text("00:" + lastTime, mapSize[0] / 2, 40);
        }
    }

    void setupPhidgets() {
        // Wait for attachments of phidgets and calibrate their readings.
        // Only setup servo if this mode has been chosen.

        try {
            if (servoOn) {
                servo = new AdvancedServoPhidget();
                servo.openAny();
                System.out.println("Waiting for Servo.");
                servo.waitForAttachment();
                servo.setEngaged(0, true);
                servo.setVelocityLimit(0, 50);
            }

            ik = new InterfaceKitPhidget();
            ik.openAny();
            System.out.println("Waiting for Phidget.");
            ik.waitForAttachment();
            System.out.println("Ready to go!");

            for (PlayerUnit player : playerUnits) {
                player.calibrateSensors();
            }
        }

        catch (Exception e){
            System.out.println("Phidgets setup error. Please try again and make sure all inputs are correctly attached.");
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                servoOn = (Integer.parseInt(args[0]) == 1);
            }
            else {
                servoOn = false;
            }
        }

        catch (Exception e) {
            System.out.println("usage: GeoBrawl <servoMode: On = 1>");
        }

        PApplet.main(new String[] { "--present", "src.GeoBrawl"});
    }
}