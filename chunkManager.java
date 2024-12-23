// ChunkManager.java
package com.CubicIdea.game;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class chunkManager {
    private Map<ChunkPosition, Chunk> loadedChunks;
    private Long worldSeed;
    private int baseHeight;
    private int reliefHeight;
    private float noiseScale;

    public chunkManager(Long worldSeed, int baseHeight, int reliefHeight, float noiseScale) {
        this.loadedChunks = new HashMap<>();
        this.worldSeed = worldSeed;
        this.baseHeight = baseHeight;
        this.reliefHeight = reliefHeight;
        this.noiseScale = noiseScale;
    }

    public void updateChunks(float cameraX, float cameraZ, int chunkRadius) {
        int currentChunkX = (int) Math.floor(cameraX / Chunk.CHUNK_SIZE);
        int currentChunkZ = (int) Math.floor(cameraZ / Chunk.CHUNK_SIZE);

        Set<ChunkPosition> chunksToKeep = new HashSet<>();

        // Déterminer les chunks à garder/charger
        for (int x = currentChunkX - chunkRadius; x <= currentChunkX + chunkRadius; x++) {
            for (int z = currentChunkZ - chunkRadius; z <= currentChunkZ + chunkRadius; z++) {
                ChunkPosition pos = new ChunkPosition(x, z);
                chunksToKeep.add(pos);

                if (!loadedChunks.containsKey(pos)) {
                    Chunk newChunk = new Chunk(x, z, worldSeed);
                    newChunk.generateTerrain(baseHeight, reliefHeight, noiseScale);
                    loadedChunks.put(pos, newChunk);
                }
            }
        }

        // Supprimer les chunks hors de portée
        loadedChunks.keySet().removeIf(pos -> !chunksToKeep.contains(pos));
    }

    public Cube getNeighborCube(int worldX, int y, int worldZ) {
        int chunkX = Math.floorDiv(worldX, Chunk.CHUNK_SIZE);
        int chunkZ = Math.floorDiv(worldZ, Chunk.CHUNK_SIZE);
        ChunkPosition pos = new ChunkPosition(chunkX, chunkZ);

        Chunk chunk = loadedChunks.get(pos);
        if (chunk == null) return null;

        return chunk.getCubeAt(worldX, y, worldZ);
    }

    public Collection<Chunk> getLoadedChunks() {
        return loadedChunks.values();
    }

    public static class ChunkPosition {
        private final int x, z;

        public ChunkPosition(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkPosition that = (ChunkPosition) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }
}