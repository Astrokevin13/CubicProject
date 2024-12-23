package com.CubicIdea.game;

import org.lwjgl.opengl.GL11;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.CubicIdea.game.Main.chunkRadius;

public class Cube {
    private float x, y, z;
    private BlockTextureRegistry.BlockTexture blockTexture;
    private int blockType;
    private static final int CHUNK_SIZE = 16;
    private static final float FOV_ANGLE = 90.0f;
    private static final float MAX_VIEW_DISTANCE = 100.0f;
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public Cube(float x, float y, float z, BlockTextureRegistry.BlockTexture blockTexture, int blockType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockTexture = blockTexture;
        this.blockType = blockType;
    }

    public int getBlockType() {
        return blockType;
    }

    public void render(Chunk currentChunk) {
        if (!isInViewDistance() || !isInFieldOfView()) {
            return;
        }

        // Si c'est un bloc d'eau, on ne le rend pas ici
        if (blockType == 2) {
            return;
        }

        renderBlock(currentChunk, false);
    }

    // Nouvelle méthode pour rendre spécifiquement l'eau
    public void renderWater(Chunk currentChunk) {
        if (blockType != 2) return;
        if (!isInViewDistance() || !isInFieldOfView()) {
            return;
        }

        GL11.glEnable(GL11.GL_CULL_FACE);  // Ajoutez ceci
        GL11.glCullFace(GL11.GL_BACK);     // Ajoutez ceci
        renderBlock(currentChunk, true);
        GL11.glDisable(GL11.GL_CULL_FACE); // Ajoutez ceci
    }
    private boolean isInViewDistance() {
        RenderDistanceManager renderManager = new RenderDistanceManager();
        renderManager.applyOperation(chunkRadius, RenderDistanceManager.Operation.MULTIPLY, 900);
        int RenderCulling = renderManager.getResult(); // Utiliser getResult() au lieu du retour de applyOperation

        float camX = Main.getCameraX();
        float camY = Main.getCameraY();
        float camZ = Main.getCameraZ();

        float toCubeX = x - camX;
        float toCubeY = y - camY;
        float toCubeZ = z - camZ;

        float distanceSquared = toCubeX * toCubeX + toCubeY * toCubeY + toCubeZ * toCubeZ;
        return distanceSquared <= RenderCulling;
    }

    private boolean isInFieldOfView() {
        float camX = Main.getCameraX();
        float camY = Main.getCameraY();
        float camZ = Main.getCameraZ();
        float rotY = (float) Math.toRadians(Main.getRotationY());
        float rotX = (float) Math.toRadians(Main.getRotationX());

        float toCubeX = x - camX;
        float toCubeY = y - camY;
        float toCubeZ = z - camZ;

        float viewDirX = (float) Math.sin(rotY);
        float viewDirY = (float) -Math.sin(rotX);
        float viewDirZ = (float) -Math.cos(rotY);

        float distance = (float) Math.sqrt(toCubeX * toCubeX + toCubeY * toCubeY + toCubeZ * toCubeZ);
        float normalizedToCubeX = toCubeX / distance;
        float normalizedToCubeY = toCubeY / distance;
        float normalizedToCubeZ = toCubeZ / distance;

        float dotProduct = normalizedToCubeX * viewDirX + normalizedToCubeY * viewDirY + normalizedToCubeZ * viewDirZ;
        return dotProduct >= 0.5f;
    }

    private void renderBlock(Chunk currentChunk, boolean isWater) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);

        if (isWater) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);
        }

        float[] sunDirection = {0f, 0.9f, 0.5f};
        GL11.glBegin(GL11.GL_QUADS);

        FaceInfo[] faces = getFaces();
        if (isWater) {
            // Pour l'eau, on trie les faces de la plus éloignée à la plus proche
            sortFacesByDistance(faces);
        }

        for (FaceInfo face : faces) {
            if (shouldRenderFace(face, Main.getCameraX(), Main.getCameraY(), Main.getCameraZ())) {
                checkAndRenderFace(currentChunk, face.dx, face.dy, face.dz,
                        face.type, face.normal, sunDirection);
            }
        }

        GL11.glEnd();

        if (isWater) {
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);
        }

        GL11.glPopMatrix();
    }

    private FaceInfo[] getFaces() {
        return new FaceInfo[]{
                new FaceInfo(0, 0, 1, BlockTextureRegistry.FaceType.FRONT,
                        new float[]{0.0f, 0.0f, 1.0f}, 0.5f, 0.5f, 1.0f),
                new FaceInfo(0, 0, -1, BlockTextureRegistry.FaceType.BACK,
                        new float[]{0.0f, 0.0f, -1.0f}, 0.5f, 0.5f, 0.0f),
                new FaceInfo(-1, 0, 0, BlockTextureRegistry.FaceType.LEFT,
                        new float[]{-1.0f, 0.0f, 0.0f}, 0.0f, 0.5f, 0.5f),
                new FaceInfo(1, 0, 0, BlockTextureRegistry.FaceType.RIGHT,
                        new float[]{1.0f, 0.0f, 0.0f}, 1.0f, 0.5f, 0.5f),
                new FaceInfo(0, 1, 0, BlockTextureRegistry.FaceType.TOP,
                        new float[]{0.0f, 1.0f, 0.0f}, 0.5f, 1.0f, 0.5f),
                new FaceInfo(0, -1, 0, BlockTextureRegistry.FaceType.BOTTOM,
                        new float[]{0.0f, -1.0f, 0.0f}, 0.5f, 0.0f, 0.5f)
        };
    }

    private void sortFacesByDistance(FaceInfo[] faces) {
        float camX = Main.getCameraX();
        float camY = Main.getCameraY();
        float camZ = Main.getCameraZ();

        for (FaceInfo face : faces) {
            float faceCenterX = x + face.centerX;
            float faceCenterY = y + face.centerY;
            float faceCenterZ = z + face.centerZ;

            face.distToCamera = (faceCenterX - camX) * (faceCenterX - camX) +
                    (faceCenterY - camY) * (faceCenterY - camY) +
                    (faceCenterZ - camZ) * (faceCenterZ - camZ);
        }

        Arrays.sort(faces, (a, b) -> Double.compare(b.distToCamera, a.distToCamera));
    }

    private boolean shouldRenderFace(FaceInfo face, float camX, float camY, float camZ) {
        float faceCenterX = x + face.centerX;
        float faceCenterY = y + face.centerY;
        float faceCenterZ = z + face.centerZ;

        float viewVectorX = camX - faceCenterX;
        float viewVectorY = camY - faceCenterY;
        float viewVectorZ = camZ - faceCenterZ;

        float length = (float) Math.sqrt(viewVectorX * viewVectorX +
                viewVectorY * viewVectorY + viewVectorZ * viewVectorZ);
        viewVectorX /= length;
        viewVectorY /= length;
        viewVectorZ /= length;

        float faceDotProduct = viewVectorX * face.normal[0] +
                viewVectorY * face.normal[1] +
                viewVectorZ * face.normal[2];

        return faceDotProduct > 0;
    }

    private static class FaceInfo {
        int dx, dy, dz;
        BlockTextureRegistry.FaceType type;
        float[] normal;
        float centerX, centerY, centerZ;
        double distToCamera; // Nouvelle propriété pour le tri

        FaceInfo(int dx, int dy, int dz, BlockTextureRegistry.FaceType type,
                 float[] normal, float centerX, float centerY, float centerZ) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.type = type;
            this.normal = normal;
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
        }
    }

    private void checkAndRenderFace(Chunk currentChunk, int dx, int dy, int dz,
                                    BlockTextureRegistry.FaceType faceType, float[] normal, float[] sunDirection) {
        int checkX = (int)x + dx;
        int checkY = (int)y + dy;
        int checkZ = (int)z + dz;

        Cube neighborCube = Main.getChunkManager().getNeighborCube(checkX, checkY, checkZ);

        if (blockType == 2) { // Eau
            // Si on est en bordure de chunk
            if (currentChunk.isOnChunkBorder(x, y, z)) {
                if (neighborCube == null) {
                    // Rendre la face si pas de voisin connu
                    renderFace(faceType, normal, sunDirection);
                    return;
                }
                // Ne pas rendre si le voisin est de l'eau
                if (neighborCube.getBlockType() == 2) {
                    return;
                }
            }
            // Comportement normal pour les blocs non frontaliers
            else if (neighborCube != null && neighborCube.getBlockType() == 2) {
                return;
            }
            renderFace(faceType, normal, sunDirection);
        } else {
            if (neighborCube != null && neighborCube.getBlockType() != 2) {
                return;
            }
            renderFace(faceType, normal, sunDirection);
        }
    }

    private void renderFace(BlockTextureRegistry.FaceType faceType, float[] normal, float[] sunDirection) {
        float shade = calculateShade(normal, sunDirection);
        BlockTextureRegistry.TextureCoord textureCoord = blockTexture.getFaceTexture(faceType);
        float[] texCoords = textureCoord.getTexCoords();

        if (blockType == 2) {
            GL11.glColor4f(shade, shade, shade, 0.6f);
        } else {
            GL11.glColor3f(shade, shade, shade);
        }

        switch(faceType) {
            case FRONT:
                GL11.glTexCoord2f(texCoords[0], texCoords[1]); GL11.glVertex3f(0, 0, 1);
                GL11.glTexCoord2f(texCoords[2], texCoords[3]); GL11.glVertex3f(1, 0, 1);
                GL11.glTexCoord2f(texCoords[4], texCoords[5]); GL11.glVertex3f(1, 1, 1);
                GL11.glTexCoord2f(texCoords[6], texCoords[7]); GL11.glVertex3f(0, 1, 1);
                break;
            case BACK:
                GL11.glTexCoord2f(texCoords[0], texCoords[1]); GL11.glVertex3f(1, 0, 0);
                GL11.glTexCoord2f(texCoords[2], texCoords[3]); GL11.glVertex3f(0, 0, 0);
                GL11.glTexCoord2f(texCoords[4], texCoords[5]); GL11.glVertex3f(0, 1, 0);
                GL11.glTexCoord2f(texCoords[6], texCoords[7]); GL11.glVertex3f(1, 1, 0);
                break;
            case LEFT:
                GL11.glTexCoord2f(texCoords[0], texCoords[1]); GL11.glVertex3f(0, 0, 0);
                GL11.glTexCoord2f(texCoords[2], texCoords[3]); GL11.glVertex3f(0, 0, 1);
                GL11.glTexCoord2f(texCoords[4], texCoords[5]); GL11.glVertex3f(0, 1, 1);
                GL11.glTexCoord2f(texCoords[6], texCoords[7]); GL11.glVertex3f(0, 1, 0);
                break;
            case RIGHT:
                GL11.glTexCoord2f(texCoords[0], texCoords[1]); GL11.glVertex3f(1, 0, 1);
                GL11.glTexCoord2f(texCoords[2], texCoords[3]); GL11.glVertex3f(1, 0, 0);
                GL11.glTexCoord2f(texCoords[4], texCoords[5]); GL11.glVertex3f(1, 1, 0);
                GL11.glTexCoord2f(texCoords[6], texCoords[7]); GL11.glVertex3f(1, 1, 1);
                break;
            case TOP:
                GL11.glTexCoord2f(texCoords[0], texCoords[1]); GL11.glVertex3f(0, 1, 0);
                GL11.glTexCoord2f(texCoords[2], texCoords[3]); GL11.glVertex3f(0, 1, 1);
                GL11.glTexCoord2f(texCoords[4], texCoords[5]); GL11.glVertex3f(1, 1, 1);
                GL11.glTexCoord2f(texCoords[6], texCoords[7]); GL11.glVertex3f(1, 1, 0);
                break;
            case BOTTOM:
                GL11.glTexCoord2f(texCoords[0], texCoords[1]); GL11.glVertex3f(0, 0, 0);
                GL11.glTexCoord2f(texCoords[2], texCoords[3]); GL11.glVertex3f(1, 0, 0);
                GL11.glTexCoord2f(texCoords[4], texCoords[5]); GL11.glVertex3f(1, 0, 1);
                GL11.glTexCoord2f(texCoords[6], texCoords[7]); GL11.glVertex3f(0, 0, 1);
                break;
        }
    }

    private float calculateShade(float[] normal, float[] sunDirection) {
        float dotProduct = normal[0] * sunDirection[0] +
                normal[1] * sunDirection[1] +
                normal[2] * sunDirection[2];
        return Math.max(0.3f, Math.min(1.0f, 0.5f + dotProduct * 0.5f));
    }
}