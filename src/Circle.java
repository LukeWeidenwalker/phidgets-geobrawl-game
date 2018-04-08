package src;

import processing.core.PApplet;

class Circle extends PlayerUnit {

    // Constructor
    Circle(int playerID) {
        super(playerID);
        this.coords = determineSpawn();
    }

    @Override
    public int[] determineSpawn() {
        int x = PlayerUnit.playerSpawnCoords[this.playerID - 1][0];
        int y = PlayerUnit.playerSpawnCoords[this.playerID - 1][1];
        return new int[] {x, y};
    }


    @Override
    void drawUnit(PApplet p) {
        int[] color = PlayerUnit.playerColorsArray[this.playerID - 1];
        p.fill(color[0], color[1], color[2]);
        p.ellipse(this.coords[0], this.coords[1], (float)this.radius * 2, (float)this.radius * 2);
    }

    @Override
    public String toString() {
        return "circle ";
        // return "circle " + "(" + this.getCoords()[0] + ", " + this.getCoords()[0] + ")";
    }
}
