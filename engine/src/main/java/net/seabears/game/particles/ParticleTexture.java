package net.seabears.game.particles;

import org.lwjgl.opengl.GL11;

public class ParticleTexture {
    private final int textureId;
    private final int rows;
    private final int blendFunc;

    public ParticleTexture(int textureId, int rows) {
        this(textureId, rows, false);
    }

    public ParticleTexture(int textureId, int rows, boolean additive) {
        this.textureId = textureId;
        this.rows = rows;
        this.blendFunc = additive ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA;
    }

    public int getTextureId() {
        return textureId;
    }

    public int getRows() {
        return rows;
    }

    public int getBlendFunc() {
        return blendFunc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + rows;
        result = prime * result + textureId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final ParticleTexture other = (ParticleTexture) obj;
        return rows == other.rows && textureId == other.textureId;
    }
}
