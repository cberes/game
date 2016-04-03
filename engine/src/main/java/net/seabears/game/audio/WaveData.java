package net.seabears.game.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

public class WaveData implements AutoCloseable {
  public final int format;
  public final int samplerate;
  public final int totalBytes;
  public final int bytesPerFrame;
  public final ByteBuffer data;

  private final AudioInputStream audioStream;
  private final byte[] dataArray;

  private WaveData(AudioInputStream stream) throws IOException {
    this.audioStream = stream;
    final AudioFormat audioFormat = stream.getFormat();
    format = getOpenAlFormat(audioFormat.getChannels(), audioFormat.getSampleSizeInBits());
    this.samplerate = (int) audioFormat.getSampleRate();
    this.bytesPerFrame = audioFormat.getFrameSize();
    this.totalBytes = (int) (stream.getFrameLength() * bytesPerFrame);
    this.data = BufferUtils.createByteBuffer(totalBytes);
    this.dataArray = new byte[totalBytes];
    loadData();
  }

  @Override
  public void close() throws IOException {
    audioStream.close();
    data.clear();
  }

  private final ByteBuffer loadData() throws IOException {
    final int bytesRead = audioStream.read(dataArray, 0, totalBytes);
    data.clear();
    data.put(dataArray, 0, bytesRead);
    data.flip();
    return data;
  }

  public static WaveData create(String file) throws IOException {
    final String path = '/' + file;
    try (InputStream bufferedInput = new BufferedInputStream(WaveData.class.getResourceAsStream(path))) {
      final AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedInput);
      return new WaveData(audioStream);
    } catch (UnsupportedAudioFileException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  public static WaveData create(File file) throws IOException {
    try {
      final AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
      return new WaveData(audioStream);
    } catch (UnsupportedAudioFileException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  private static int getOpenAlFormat(int channels, int bitsPerSample) {
    if (channels == 1) {
      return bitsPerSample == 8 ? AL10.AL_FORMAT_MONO8 : AL10.AL_FORMAT_MONO16;
    } else {
      return bitsPerSample == 8 ? AL10.AL_FORMAT_STEREO8 : AL10.AL_FORMAT_STEREO16;
    }
  }
}
