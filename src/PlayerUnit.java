package src;

import com.phidgets.*;

import processing.core.PApplet;
import processing.core.PFont;

import java.util.concurrent.TimeUnit;

abstract class PlayerUnit extends SpawnedUnit {

    // Variables
    private String type;
    int radius, score, playerID;

    static int[][] playerColorsArray = new int[][] {{66, 134, 244}, {165, 33, 39}, {69, 175, 59}, {138, 13, 188}};
    static int[][] playerScoreCoordsArray = new int[][] {{10, 30}, {GeoBrawl.mapSize[0] - 200, 30}};
    static int[][] playerSpawnCoords = new int[][] {{GeoBrawl.mapSize[0] / 4, GeoBrawl.mapSize[1] / 2}, {3 * (GeoBrawl.mapSize[0] / 4), GeoBrawl.mapSize[1] / 2}};
    // Make sure the joystick sensors are attached so that left/right is before up/down on the phidgets board.
    static int[][] playerSensorIDs = new int[][] {{0, 1}, {2, 3}};

    private float[][] sensorCalibrationAverages = new float[2][2];

    private static double frict = 0.955;
    private double xvel = 0;
    private double yvel = 0;


    // Constructor
    PlayerUnit(int playerID) {
        super();
        this.playerID = playerID;
        this.radius = 25;
    }

    // Methods

    abstract void drawUnit(PApplet p);

    void calibrateSensors() {
        try {
            System.out.println("Joysticks are now being calibrated.");
            // Gathering calibration values for Joystick X, Y

            // Joystick calibration using 5000 sample values.
            for (int sensorNumber = 0; sensorNumber <= 1; sensorNumber++) {
                float sumMeasurements = 0;
                float[] measurementArray = new float[5000];
                for (int i = 0; i < 5000; i++) {
                    float sensorValue = GeoBrawl.ik.getSensorValue(playerSensorIDs[this.playerID - 1][sensorNumber]);
                    measurementArray[i] = sensorValue;
                    sumMeasurements += sensorValue;
                }

                this.sensorCalibrationAverages[sensorNumber][0] = sumMeasurements / 5000;
                this.sensorCalibrationAverages[sensorNumber][1] = calculateSD(measurementArray);
            }

            System.out.println("Calibration averages: " + this.sensorCalibrationAverages[0][0] + ", " + this.sensorCalibrationAverages[1][0]);
            System.out.println("Calibration SDs: " + this.sensorCalibrationAverages[0][1] + ", " + this.sensorCalibrationAverages[1][1]);

        }


        catch (PhidgetException e) {
            System.out.println("Calibration error.");
            System.out.println("Please do not move the sensors for the next 3 seconds.");
            System.out.println(e.getMessage());

        }
    }

    void updatePosition() {
        // Is called every drawing cycle, moves player units according to joystick input.
        this.coords[0] = this.coords[0] + (int)this.xvel;
        // y-axis is inverted from joystick input
        this.coords[1] = this.coords[1] - (int)this.yvel;

        float posX, posY;
        try {
            posX = GeoBrawl.ik.getSensorValue(playerSensorIDs[this.playerID - 1][0]) - this.sensorCalibrationAverages[this.playerID - 1][0];//
            posY = GeoBrawl.ik.getSensorValue(playerSensorIDs[this.playerID - 1][1]) - this.sensorCalibrationAverages[this.playerID - 1][0];//

            // Make sure the input is only registered when the input is noticeably high
            if (Math.abs(posX) >= this.sensorCalibrationAverages[0][0] * 0.05) {
                posX /= 1150;
                this.xvel = (this.xvel + posX) * frict;
            }

            else {
                this.xvel = this.xvel * frict;
            }

            if (Math.abs(posY) >= this.sensorCalibrationAverages[1][0] * 0.05) {
                posY /= 1150;
                this.yvel = (this.yvel - posY) * frict;
            }

            else {
                this.yvel = this.yvel * frict;
            }
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    boolean checkUnitCollision(SpawnedUnit otherUnit) {
        // Detect collision based on distance between circle centres.
        return Math.sqrt(Math.pow(Math.abs(this.coords[0] + 1  - otherUnit.coords[0] + 1), 2) + Math.pow(Math.abs(this.coords[1] + 1 - otherUnit.coords[1] + 1), 2)) - this.radius - otherUnit.radius < 3;
    }

    void checkWallCollision() {
        if (this.coords[0] > GeoBrawl.mapSize[0] - this.radius) {
            this.coords[0] = GeoBrawl.mapSize[0] - this.radius;
            this.xvel *= -1;
        } else if (this.coords[0] < radius) {
            this.coords[0] = this.radius;
            this.xvel *= -1;
        } else if (this.coords[1] > GeoBrawl.mapSize[1] - this.radius) {
            this.coords[1] = GeoBrawl.mapSize[1] - this.radius;
            this.yvel *= -1;
        } else if (this.coords[1] < radius) {
            this.coords[1] = this.radius;
            this.yvel *= -1;
        }
    }

    void collide() {
        this.xvel *= -1;
        this.yvel *= -1;
        this.coords[0] += this.xvel * 2;
        this.coords[1] += this.yvel * 2;

    }

    void timeout(int duration) {
        try {
            TimeUnit.SECONDS.sleep(duration);
        } catch (InterruptedException e) {
            System.out.println("Interrupted.");
        }
    }

    void displayScore(PApplet p) {
        PFont f;
        f = p.createFont("Helvetica",16,true);
        p.textFont(f,24);
        p.fill(GeoBrawl.fontColor);
        p.text("Player " + this.playerID + " - Score: " + this.score, playerScoreCoordsArray[this.playerID - 1][0], playerScoreCoordsArray[this.playerID - 1][1]);
    }

    public static float calculateSD(float numArray[]) {
        // Adapted slightly from https://www.programiz.com/java-programming/examples/standard-deviation.

        float sum = 0.0f, standardDeviation = 0.0f;

        for(float num : numArray) {
            sum += num;
        }

        float mean = sum/numArray.length;

        for(float num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return (float)Math.sqrt(standardDeviation/numArray.length);
    }
}
