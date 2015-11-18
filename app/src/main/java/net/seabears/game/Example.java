package net.seabears.game;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

/**
 * Copied from http://wiki.lwjgl.org/wiki/LWJGL_Basics_3_(The_Quad)
 */
public class Example {
    private final Player player = new Player(200, 100, 100);
    private final int speed = 1;

    public void start() {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        // init OpenGL
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, 800, 0, 600, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        while (!Display.isCloseRequested()) {
            // Clear the screen and depth buffer
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // set the color of the quad (R,G,B,A)
            if (Mouse.isButtonDown(0)) {
                GL11.glColor3f(0.5f, 0.5f, 0.0f);
            } else {
                GL11.glColor3f(0.5f, 0.5f, 1.0f);
            }

            // get x/y movement
            int x = 0;
            int y = 0;
            if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
                y += 1;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
                y += -1;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
                x += 1;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
                x += -1;
            }

            // update player position
            player.setX(player.getX() + x * speed);
            player.setY(player.getY() + y * speed);

            // draw quad
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(player.getX(), player.getY());
            GL11.glVertex2f(player.getX() + player.getSize(), player.getY());
            GL11.glVertex2f(player.getX() + player.getSize(), player.getY() + player.getSize());
            GL11.glVertex2f(player.getX(), player.getY() + player.getSize());
            GL11.glEnd();

            Display.update();
        }

        Display.destroy();
    }

    public static void main(String[] argv) {
        Example quadExample = new Example();
        quadExample.start();
    }
}

