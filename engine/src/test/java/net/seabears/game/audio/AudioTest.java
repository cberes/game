package net.seabears.game.audio;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.joml.Vector3f;
import org.junit.Test;

public class AudioTest {
  @Test
  public void test() throws Exception {
    final AudioMaster am = new AudioMaster();
    am.setListenerData(new Vector3f());
    final int buffer = am.loadSound(new File("src/test/resources/audio/bounce.wav"));
    final Source source = new Source();
    source.setGain(1);
    source.setPitch(1);
    source.setPosition(new Vector3f(2, 0, 2));
    source.setLoop(true);
    source.start(buffer);
    for (int i = 0; i < 4; ++i) {
      TimeUnit.SECONDS.sleep(1);
      source.setPosition(new Vector3f(-i * 2, 0, 2));
    }
    source.pause();
    final Source source2 = new Source();
    source2.setPitch(2);
    source2.setLoop(true);
    source2.start(buffer);
    for (int i = 0; i < 2; ++i) {
      TimeUnit.SECONDS.sleep(1);
    }
    source.play();
    for (int i = 0; i < 2; ++i) {
      TimeUnit.SECONDS.sleep(1);
    }
    source2.close();
    source.close();
    am.close();
  }
}
