package net.seabears.game.audio;

import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

public class Source implements AutoCloseable {
  private final int sourceId;

  public Source() {
    this.sourceId = AL10.alGenSources();
  }

  public void setGain(int gain) {
    AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
  }

  public void setPitch(int pitch) {
    AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
  }

  public void setPosition(Vector3f v) {
    AL10.alSource3f(sourceId, AL10.AL_POSITION, v.x, v.y, v.z);
  }

  public void setVelocity(Vector3f v) {
    AL10.alSource3f(sourceId, AL10.AL_VELOCITY, v.x, v.y, v.z);
  }

  public void setLoop(boolean loop) {
    AL10.alSourcei(sourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
  }

  public void start(final int buffer) {
    stop();
    AL10.alSourcei(sourceId, AL10.AL_BUFFER, buffer);
    play();
  }

  public void play() {
    AL10.alSourcePlay(sourceId);
  }

  public void pause() {
    AL10.alSourcePause(sourceId);
  }

  public void stop() {
    AL10.alSourceStop(sourceId);
  }

  public boolean isPlaying() {
    return AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
  }

  @Override
  public void close() {
    stop();
    AL10.alDeleteSources(sourceId);
  }
}
