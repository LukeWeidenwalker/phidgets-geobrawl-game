package src;

import processing.core.PApplet;

abstract public class SpawnedUnit {
    String type;
    int[] coords;
    int radius;

    SpawnedUnit() {
        this.type = this.toString();
    }


    public String getType() {
        return this.type;
    }

    public int[] getCoords() {
        return this.coords;
    }


    abstract int[] determineSpawn();

    abstract void drawUnit(PApplet p);






}
