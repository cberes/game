package net.seabears.game.util;

import static org.junit.Assert.*;

import org.joml.Vector3f;
import org.junit.Test;

import net.seabears.game.entities.Camera;
import net.seabears.game.entities.Player;

public class FrustumTest {
    private static final float FOV = 70.0f;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 1000.0f;
    private static final float WIDTH = 800;
    private static final float HEIGHT = 600;

    @Test
    public void test() {
        final Vector3f playerPosition = new Vector3f(800, 5, 0);
        final Player player = new Player(null, playerPosition, new Vector3f(), 0.5f, null, new Volume(5, 4), 0, 0, 0, 0);
        final Camera camera = new Camera(player);
        camera.move();
        final Frustum frustum = new Frustum(camera, FOV, NEAR_PLANE, FAR_PLANE, WIDTH / HEIGHT);
        System.out.println("Normals:");
        frustum.printNormals();
        System.out.println("Points:");
        frustum.printPoints();
        System.out.println("Camera:");
        frustum.print(camera.getPosition());
        System.out.println("Player:");
        frustum.print(player.getPosition());
        System.out.println(new ViewMatrix(camera).toMatrix());
        assertEquals(frustum.distanceRight(playerPosition), frustum.distanceLeft(playerPosition), 1E-5);
        assertTrue(frustum.contains(playerPosition, 0.0f));
        assertFalse(frustum.contains(camera.getPosition(), 0.0f));
    }
}
