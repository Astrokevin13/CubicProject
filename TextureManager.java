package com.CubicIdea.game;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TextureManager {
    private static int textureID;
    private static int atlasWidth, atlasHeight;
    private static final int TEXTURE_ATLAS_TILES = 16; // Nombre de textures par ligne/colonne
    public static void loadTexture() {
        try {
            // Chemin du fichier atlas, à ajuster selon votre structure de projet
            BufferedImage image = ImageIO.read(new File("C:/Users/kevin/IdeaProjects/CubicIdea/com/CubicIdea/game/ressource/Atlas.png"));
            atlasWidth = image.getWidth();
            atlasHeight = image.getHeight();

            ByteBuffer buffer = ByteBuffer.allocateDirect(4 * atlasWidth * atlasHeight);
            for (int y = 0; y < atlasHeight; y++) {
                for (int x = 0; x < atlasWidth; x++) {
                    int pixel = image.getRGB(x, y);
                    buffer.put((byte)((pixel >> 16) & 0xFF)); // R
                    buffer.put((byte)((pixel >> 8) & 0xFF));  // G
                    buffer.put((byte)((pixel >> 0) & 0xFF));  // B
                    buffer.put((byte)((pixel >> 24) & 0xFF)); // A
                }
            }
            buffer.flip();

            // Génération de la texture OpenGL
            textureID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

            // Paramètres de texture pour une texture nette (sans flou)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, atlasWidth, atlasHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getTextureID() {
        return textureID;
    }

    /**
     * Obtenir les coordonnées de texture pour une position X, Y dans l'atlas
     * @param x Position X de la texture (commence à 1)
     * @param y Position Y de la texture (commence à 1)
     * @return Tableau de coordonnées de texture pour les 4 coins
     */
    public static float[] getTexCoords(int x, int y) {
        // Ajustement pour commencer à 1 au lieu de 0
        x = x - 1;
        y = y - 1;

        // Calcul des coordonnées normalisées
        float texX = (float) x / TEXTURE_ATLAS_TILES;
        float texY = (float) y / TEXTURE_ATLAS_TILES;
        float tileSize = 1.0f / TEXTURE_ATLAS_TILES;

        // Retourne les coordonnées pour les 4 coins de la texture
        return new float[]{
                texX, texY + tileSize,      // Coin inférieur gauche
                texX + tileSize, texY + tileSize, // Coin inférieur droit
                texX + tileSize, texY,      // Coin supérieur droit
                texX, texY                  // Coin supérieur gauche
        };
    }

    /**
     * Méthode de compatibilité avec l'ancien système
     * @param textureIndex Index de texture dans le système précédent
     * @return Coordonnées de texture
     */
    public static float[] getCustomTexCoords(int textureIndex) {
        int x = (textureIndex % TEXTURE_ATLAS_TILES) + 1;
        int y = (textureIndex / TEXTURE_ATLAS_TILES) + 1;
        return getTexCoords(x, y);
    }
}