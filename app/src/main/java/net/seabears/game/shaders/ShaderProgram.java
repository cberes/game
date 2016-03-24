package net.seabears.game.shaders;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class ShaderProgram implements AutoCloseable {
  private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

  private static int loadShader(String file, int type) throws IOException {
    int shaderId = GL20.glCreateShader(type);
    GL20.glShaderSource(shaderId, readFile(file));
    GL20.glCompileShader(shaderId);
    if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
      throw new IllegalStateException("Could not compile shader. " + GL20.glGetShaderInfoLog(shaderId, 500));
    }
    return shaderId;
  }

  private static String readFile(String file) throws IOException {
    return new String(Files.readAllBytes(Paths.get(file)));
  }

  private final int programId;
  private final int vertexShaderId;
  private final int fragmentShaderId;

  public ShaderProgram(String vertexFile, String fragmentFile) throws IOException {
    this.vertexShaderId = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
    this.fragmentShaderId = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
    this.programId = GL20.glCreateProgram();
    GL20.glAttachShader(this.programId, this.vertexShaderId);
    GL20.glAttachShader(this.programId, this.fragmentShaderId);
    this.bindAttributes();
    GL20.glLinkProgram(this.programId);
    GL20.glValidateProgram(this.programId);
    this.getAllUniformLocations();
  }

  protected abstract void getAllUniformLocations();

  protected int getUniformLocation(String uniformName) {
    return GL20.glGetUniformLocation(programId, uniformName);
  }

  public void start() {
    GL20.glUseProgram(programId);
  }

  public void stop() {
    GL20.glUseProgram(0);
  }

  @Override
  public void close() {
    stop();
    GL20.glDetachShader(programId, vertexShaderId);
    GL20.glDetachShader(programId, fragmentShaderId);
    GL20.glDeleteShader(vertexShaderId);
    GL20.glDeleteShader(fragmentShaderId);
    GL20.glDeleteProgram(programId);
  }

  protected abstract void bindAttributes();

  protected void bindAttribute(int attribute, String variableName) {
    GL20.glBindAttribLocation(programId, attribute, variableName);
  }

  protected void loadInt(int location, int value) {
    GL20.glUniform1i(location, value);
  }

  protected void loadFloat(int location, float value) {
    GL20.glUniform1f(location, value);
  }

  protected void loadFloat(int location, Vector3f value) {
    GL20.glUniform3f(location, value.x, value.y, value.z);
  }

  protected void loadFloat(int location, boolean value) {
    loadFloat(location, value ? 1.0f : 0.0f);
  }

  protected void loadMatrix(int location, Matrix4f value) {
    GL20.glUniformMatrix4fv(location, false, value.get(matrixBuffer));
  }
}
