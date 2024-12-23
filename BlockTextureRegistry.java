package com.CubicIdea.game;

import java.util.HashMap;
import java.util.Map;

public class BlockTextureRegistry {
    // Structure pour définir les textures d'un bloc
    public static class BlockTexture {
        private Map<FaceType, TextureCoord> faceTextures;

        public BlockTexture() {
            faceTextures = new HashMap<>();
        }

        // Définir la texture pour une face spécifique
        public void setFaceTexture(FaceType face, TextureCoord coord) {
            faceTextures.put(face, coord);
        }

        // Obtenir la texture pour une face
        public TextureCoord getFaceTexture(FaceType face) {
            // Si aucune texture n'est définie pour une face, utiliser une texture par défaut
            return faceTextures.getOrDefault(face, new TextureCoord(1, 1));
        }

        // Méthode pour s'assurer que toutes les faces ont une texture
        public void fillMissingTextures(TextureCoord defaultTexture) {
            for (FaceType face : FaceType.values()) {
                if (!faceTextures.containsKey(face)) {
                    faceTextures.put(face, defaultTexture);
                }
            }
        }
    }

    // Représentation des coordonnées de texture
    public static class TextureCoord {
        private int x, y;

        public TextureCoord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        // Convertir les coordonnées en index de texture (compatible avec l'ancien système)
        public int toTextureIndex() {
            return (y - 1) * 16 + (x - 1);
        }

        public float[] getTexCoords() {
            return TextureManager.getTexCoords(x, y);
        }

        // Nouveaux getters pour permettre des ajustements précis
        public int getX() { return x; }
        public int getY() { return y; }
    }

    // Énumération des types de faces (ordonné logiquement)
    public enum FaceType {
        FRONT(0),
        BACK(1),
        LEFT(2),
        RIGHT(3),
        TOP(4),
        BOTTOM(5);

        private final int index;
        FaceType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    // Registre global des textures de blocs
    private static Map<Integer, BlockTexture> blockTextureMap = new HashMap<>();

    // Enregistrer un nouveau type de bloc avec ses textures
    public static void registerBlockTexture(int blockId, BlockTexture texture) {
        // Remplir les textures manquantes avec une texture par défaut
        texture.fillMissingTextures(new TextureCoord(1, 1));
        blockTextureMap.put(blockId, texture);
    }

    // Obtenir la texture d'un bloc spécifique
    public static BlockTexture getBlockTexture(int blockId) {
        return blockTextureMap.getOrDefault(blockId, createDefaultTexture());
    }

    // Texture par défaut si non spécifiée
    private static BlockTexture createDefaultTexture() {
        BlockTexture defaultTexture = new BlockTexture();
        for (FaceType face : FaceType.values()) {
            defaultTexture.setFaceTexture(face, new TextureCoord(1, 1));
        }
        return defaultTexture;
    }

    // Exemple d'utilisation amélioré
    public static void initializeTextures() {
        // Bloc d'herbe avec textures différentes
        BlockTexture grassBlock = new BlockTexture();
        grassBlock.setFaceTexture(FaceType.TOP, new TextureCoord(1, 1));     // Herbe dessus
        grassBlock.setFaceTexture(FaceType.BOTTOM, new TextureCoord(3, 1)); // Terre dessous
        grassBlock.setFaceTexture(FaceType.FRONT, new TextureCoord(4, 1));  // Côté herbe
        grassBlock.setFaceTexture(FaceType.BACK, new TextureCoord(4, 1));   // Côté herbe
        grassBlock.setFaceTexture(FaceType.LEFT, new TextureCoord(4, 1));   // Côté herbe
        grassBlock.setFaceTexture(FaceType.RIGHT, new TextureCoord(4, 1));  // Côté herbe
        registerBlockTexture(0, grassBlock);

        // Bloc de pierre avec texture unique
        BlockTexture stoneBlock = new BlockTexture();
        stoneBlock.setFaceTexture(FaceType.FRONT, new TextureCoord(2, 1));  // Pierre
        stoneBlock.setFaceTexture(FaceType.BACK, new TextureCoord(2, 1));   // Pierre
        stoneBlock.setFaceTexture(FaceType.LEFT, new TextureCoord(2, 1));   // Pierre
        stoneBlock.setFaceTexture(FaceType.RIGHT, new TextureCoord(2, 1));  // Pierre
        stoneBlock.setFaceTexture(FaceType.TOP, new TextureCoord(2, 1));    // Pierre dessus
        stoneBlock.setFaceTexture(FaceType.BOTTOM, new TextureCoord(2, 1)); // Pierre dessous
        registerBlockTexture(1, stoneBlock);

        BlockTexture Water = new BlockTexture();
        Water.setFaceTexture(FaceType.FRONT, new TextureCoord(15, 14));  // Pierre
        Water.setFaceTexture(FaceType.BACK, new TextureCoord(15, 14));   // Pierre
        Water.setFaceTexture(FaceType.LEFT, new TextureCoord(15, 14));   // Pierre
        Water.setFaceTexture(FaceType.RIGHT, new TextureCoord(15, 14));  // Pierre
        Water.setFaceTexture(FaceType.TOP, new TextureCoord(15, 14));    // Pierre dessus
        Water.setFaceTexture(FaceType.BOTTOM, new TextureCoord(15, 14)); // Pierre dessous
        registerBlockTexture(2, Water);

        BlockTexture Sand = new BlockTexture();
        Sand.setFaceTexture(FaceType.FRONT, new TextureCoord(3, 2));  // Pierre
        Sand.setFaceTexture(FaceType.BACK, new TextureCoord(3, 2));   // Pierre
        Sand.setFaceTexture(FaceType.LEFT, new TextureCoord(3, 2));   // Pierre
        Sand.setFaceTexture(FaceType.RIGHT, new TextureCoord(3, 2));  // Pierre
        Sand.setFaceTexture(FaceType.TOP, new TextureCoord(3, 2));    // Pierre dessus
        Sand.setFaceTexture(FaceType.BOTTOM, new TextureCoord(3, 2)); // Pierre dessous
        registerBlockTexture(3, Sand);

        BlockTexture Gravel = new BlockTexture();
        Gravel.setFaceTexture(FaceType.FRONT, new TextureCoord(4, 2));  // Pierre
        Gravel.setFaceTexture(FaceType.BACK, new TextureCoord(4, 2));   // Pierre
        Gravel.setFaceTexture(FaceType.LEFT, new TextureCoord(4, 2));   // Pierre
        Gravel.setFaceTexture(FaceType.RIGHT, new TextureCoord(4, 2));  // Pierre
        Gravel.setFaceTexture(FaceType.TOP, new TextureCoord(4, 2));    // Pierre dessus
        Gravel.setFaceTexture(FaceType.BOTTOM, new TextureCoord(4, 2)); // Pierre dessous
        registerBlockTexture(4, Gravel);
    }

}