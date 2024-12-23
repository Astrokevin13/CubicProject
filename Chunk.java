package com.CubicIdea.game;

import org.lwjgl.opengl.GL11;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Chunk {
    private Map<String, Cube> cubes;
    private PerlinNoise perlinNoise;
    private int chunkX, chunkZ;
    public static final int CHUNK_SIZE = 16;
    private static int WaterLevel = 45;
    public Chunk(int chunkX, int chunkZ) {
        this(chunkX, chunkZ, null);
    }

    public Chunk(int chunkX, int chunkZ, Long seed) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.cubes = new HashMap<>();

        if (seed != null) {
            this.perlinNoise = new PerlinNoise(seed);
        } else {
            this.perlinNoise = new PerlinNoise();
        }
    }

    public void generateTerrain(int baseHeight, int reliefHeight, float scale) {
        // Initialiser les textures avant la génération
        BlockTextureRegistry.initializeTextures();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int worldX = chunkX * CHUNK_SIZE + x;
                int worldZ = chunkZ * CHUNK_SIZE + z;

                // Génération des 32 blocs de sous-sol
                for (int y = 0; y < 32; y++) {
                    addCube(worldX, y, worldZ, 1); // Pierre
                }

                // Génération du relief
                double noiseHeight = perlinNoise.noise(worldX * 0.05, worldZ * 0.05);
                int additionalHeight = baseHeight + (int) ((noiseHeight - 64) / 32 * reliefHeight);
                int maxAdditionalHeight = Math.min(additionalHeight, 32);

                // Ajouter des blocs de surface
                for (int y = 32; y < 32 + maxAdditionalHeight; y++) {
                    int blockType = (y == 32 + maxAdditionalHeight - 1) ? 0 : 1; // Bloc d'herbe ou terre
                    addCube(worldX, y, worldZ, blockType);
                }

                // Ajout des blocs avec BlockType 2 pour l'eau sous Y=43
                for (int y = 0; y < WaterLevel; y++) {
                    if (!hasCubeAt(worldX, y, worldZ)) {
                        // Vérifier si le bloc est exposé à l'air ou à d'autres blocs non-aqueux
                        boolean shouldAddWater = false;
                        // Vérifier les 6 faces
                        int[][] directions = {{0,1,0}, {0,-1,0}, {1,0,0}, {-1,0,0}, {0,0,1}, {0,0,-1}};

                        for (int[] dir : directions) {
                            int checkX = worldX + dir[0];
                            int checkY = y + dir[1];
                            int checkZ = worldZ + dir[2];

                            if (!hasCubeAt(checkX, checkY, checkZ) ||
                                    (getCubeAt(checkX, checkY, checkZ) != null &&
                                            getCubeAt(checkX, checkY, checkZ).getBlockType() != 2)) {
                                shouldAddWater = true;
                                break;
                            }
                        }

                        if (shouldAddWater) {
                            addCube(worldX, y, worldZ, 2); // Eau
                        }
                    }
                }

                // Remplacer l'herbe par du sable autour de l'eau
                replaceGrassWithSandAroundWater(worldX, worldZ);

                // Remplacer le sable par du gravier avec une transition douce sous Y = 37
                replaceSandWithGravelUnder37(worldX, worldZ);
            }
        }
    }

    private void replaceSandWithGravelUnder37(int worldX, int worldZ) {
        Random random = new Random(); // Générateur de nombres aléatoires
        for (int y = 0; y < 43; y++) {
            if (hasCubeAt(worldX, y, worldZ) && getCubeType(worldX, y, worldZ) == 3) { // Sable
                // Remplacer tous les blocs sous la couche 37 par du gravier
                if (y <= 37) {
                    addCube(worldX, y, worldZ, 4); // Remplacer par du gravier (id 4)
                }
                // À partir de la couche 38 jusqu'à la 43, 1 bloc sur 4 sera remplacé par du gravier
                else if (y >= 38 && y <= 39) {
                    // Décider de manière aléatoire si c'est du gravier (1 chance sur 4)
                    if (random.nextInt(4) == 0) {  // Retourne un nombre entre 0 et 3, et si c'est 0, on remplace par du gravier
                        addCube(worldX, y, worldZ, 4); // Remplacer par du gravier (id 4)
                    }
                }
            }
        }
    }
    private void replaceGrassWithSandAroundWater(int worldX, int worldZ) {
        // Vérifier toutes les couches autour de chaque cube d'eau
        for (int y = 0; y < WaterLevel; y++) {
            if (hasCubeAt(worldX, y, worldZ) && getCubeType(worldX, y, worldZ) == 2) { // Bloc d'eau détecté
                // Vérifier tous les voisins dans un rayon autour du bloc d'eau (en 3D)
                for (int offsetX = -1; offsetX <= 1; offsetX++) {
                    for (int offsetY = -1; offsetY <= 1; offsetY++) {
                        for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                            // Calcul des coordonnées du voisin
                            int neighborX = worldX + offsetX;
                            int neighborY = y + offsetY;
                            int neighborZ = worldZ + offsetZ;

                            // Vérifier si le voisin est un bloc d'herbe et le remplacer par du sable
                            if (hasCubeAt(neighborX, neighborY, neighborZ) && getCubeType(neighborX, neighborY, neighborZ) == 0) {
                                addCube(neighborX, neighborY, neighborZ, 3); // Remplacer par du sable
                            }
                        }
                    }
                }
            }
        }
    }

    private void addCube(float x, float y, float z, int blockType) {
        BlockTextureRegistry.BlockTexture blockTexture = BlockTextureRegistry.getBlockTexture(blockType);
        Cube cube = new Cube(x, y, z, blockTexture, blockType); // Ajout du blockType
        cubes.put(generateKey(x, y, z), cube);
    }

    public boolean hasCubeAt(float x, float y, float z) {
        return cubes.containsKey(generateKey(x, y, z));
    }

    public Cube getCubeAt(float x, float y, float z) {
        return cubes.get(generateKey(x, y, z));
    }

    public int getCubeType(float x, float y, float z) {
        Cube cube = getCubeAt(x, y, z);
        return (cube != null) ? cube.getBlockType() : -1; // Retourne -1 si aucun cube n'est trouvé
    }

    public boolean isOnChunkBorder(float x, float y, float z) {
        int localX = (int)x % CHUNK_SIZE;
        int localZ = (int)z % CHUNK_SIZE;
        return localX == 0 || localX == CHUNK_SIZE - 1 ||
                localZ == 0 || localZ == CHUNK_SIZE - 1;
    }

    public void render() {
        // Premier passage : blocs non-eau (opaques)
        for (Cube cube : cubes.values()) {
            if (cube.getBlockType() != 2) { // Si ce n'est pas de l'eau
                cube.render(this);
            }
        }

        // Activer le blending pour l'eau
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);

        // Trier les blocs d'eau par distance
        List<Cube> waterBlocks = cubes.values().stream()
                .filter(cube -> cube.getBlockType() == 2)
                .sorted((c1, c2) -> {
                    float camX = Main.getCameraX();
                    float camY = Main.getCameraY();
                    float camZ = Main.getCameraZ();

                    double dist1 = Math.sqrt(
                            Math.pow(c1.getX() - camX, 2) +
                                    Math.pow(c1.getY() - camY, 2) +
                                    Math.pow(c1.getZ() - camZ, 2)
                    );
                    double dist2 = Math.sqrt(
                            Math.pow(c2.getX() - camX, 2) +
                                    Math.pow(c2.getY() - camY, 2) +
                                    Math.pow(c2.getZ() - camZ, 2)
                    );
                    return Double.compare(dist2, dist1);
                })
                .collect(Collectors.toList());

        // Render water blocks
        for (Cube waterCube : waterBlocks) {
            waterCube.renderWater(this);
        }

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private String generateKey(float x, float y, float z) {
        // Utilisation des coordonnées arrondies pour éviter les problèmes de flottants
        return ((int) Math.floor(x)) + "," + ((int) Math.floor(y)) + "," + ((int) Math.floor(z));
    }

    // Méthodes getter existantes
    public Map<String, Cube> getCubes() {
        return cubes;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }
}