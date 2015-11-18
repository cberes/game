package net.seabears.game;

public class Player {
    private final int size;
    private int x, y;

    public Player(int size) {
        this(size, 0, 0);
    }

    public Player(int size, int x, int y) {
        this.size = size;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSize() {
        return size;
    }
}
