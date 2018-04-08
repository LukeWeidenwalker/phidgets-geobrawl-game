package src;

import processing.core.PApplet;

class Coin extends SpawnedUnit {
    // Variables
    int coinValue;
    private static int SPAWN_MARGIN = 20;

    // Constructor
    Coin() {
        super();
        this.coords = determineSpawn();
        double valueDecider = Math.random();

        // 1 out of 4 coins is worth 5 points
        if (valueDecider <= 0.25) {
            this.coinValue = 5;
            this.radius = 30;
        }
        else {
            this.coinValue = 1;
            this.radius = 15;
        }
    }

    Coin(int score) {
        super();
        this.coords = determineSpawn();
        this.coinValue = score;
        this.radius = 15;
    }

    // Methods
    @Override
    int[] determineSpawn() {
        int x = (int) (Math.random()*(GeoBrawl.mapSize[0] - SPAWN_MARGIN)) + SPAWN_MARGIN;
        int y = (int) (Math.random()*GeoBrawl.mapSize[1] - SPAWN_MARGIN) + SPAWN_MARGIN;
        return new int[] {x, y};
    }

    @Override
    void drawUnit(PApplet p) {
        p.strokeWeight(1);
        p.stroke(200);
        p.fill(255, 212, 0);
        p.ellipse(this.coords[0], this.coords[1], this.radius, this.radius);
    }
}
