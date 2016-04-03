package net.seabears.game.audio;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.joml.Vector3f;
import org.junit.Test;

public class AudioTest {
  @Test
  public void test() throws Exception {
    final AudioMaster am = new AudioMaster();
    am.setListenerData();
    final int buffer = am.loadSound(new File("src/test/resources/audio/bounce.wav"));
    final Source source = new Source(1, 1, new Vector3f());
    for (int i = 0; i < 5; ++i) {
      source.play(buffer);
      TimeUnit.SECONDS.sleep(1);
    }
    source.close();
    am.close();
  }
}
