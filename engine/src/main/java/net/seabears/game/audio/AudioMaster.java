package net.seabears.game.audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALContext;
import org.lwjgl.openal.ALDevice;

public class AudioMaster implements AutoCloseable {
  private final ALDevice device;
  private final ALContext context;
  private final List<Integer> buffers;

  public AudioMaster() {
    this.device = ALDevice.create();
    this.context = ALContext.create(this.device);
    this.buffers = new ArrayList<>();
  }

  public int loadSound(String file) throws IOException {
    try (WaveData waveFile = WaveData.create(file)) {
      return loadSound(waveFile);
    }
  }

  public int loadSound(File file) throws IOException {
    try (WaveData waveFile = WaveData.create(file)) {
      return loadSound(waveFile);
    }
  }

  private int loadSound(WaveData waveFile) {
    final int buffer = AL10.alGenBuffers();
    buffers.add(buffer);
    AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
    return buffer;
  }

  public void setListenerData(Vector3f position) {
    this.setListenerData(position, new Vector3f());
  }

  public void setListenerData(Vector3f position, Vector3f velocity) {
    AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
    AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
  }

  @Override
  public void close() {
    buffers.forEach(AL10::alDeleteBuffers);
    context.destroy();
    device.destroy();
  }
}
