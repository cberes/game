package net.seabears.game.audio;

import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

public class Source implements AutoCloseable {
  private final int sourceId;

  public Source(int gain, int pitch, Vector3f position) {
    this.sourceId = AL10.alGenSources();
    AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
    AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
    AL10.alSource3f(sourceId, AL10.AL_POSITION, position.x, position.y, position.z);
  }

  public void play (final int buffer) {
    AL10.alSourcei(sourceId, AL10.AL_BUFFER, buffer);
    AL10.alSourcePlay(sourceId);
  }

  @Override
  public void close() {
    AL10.alDeleteSources(sourceId);
  }
}
