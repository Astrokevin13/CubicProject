package com.CubicIdea.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PerlinNoise {

    private int[] permutationTable;
    private static final int TABLE_SIZE = 256;
    private Map<String, Double> noiseCache; // Cache pour stocker les résultats du bruit
    private long seed;

    private static final int BASE_HEIGHT = 20; // Hauteur de base uniforme
    private static final double MAX_RELIEF = 44; // Amplitude maximale du relief
    private static final double MOUNTAIN_FREQUENCY = 0.2; // Fréquence des montagnes
    private static final double MOUNTAIN_AMPLITUDE = 64; // Hauteur maximale des montagnes

    public PerlinNoise() {
        this(new Random().nextLong());
    }

    public PerlinNoise(long seed) {
        this.seed = seed;
        permutationTable = new int[TABLE_SIZE * 2];
        noiseCache = new HashMap<>();
        Random random = new Random(seed);

        for (int i = 0; i < TABLE_SIZE; i++) {
            permutationTable[i] = i;
        }
        shuffleTable(random);
    }

    private void shuffleTable(Random random) {
        for (int i = 0; i < TABLE_SIZE; i++) {
            int randomIndex = random.nextInt(TABLE_SIZE);
            int temp = permutationTable[i];
            permutationTable[i] = permutationTable[randomIndex];
            permutationTable[randomIndex] = temp;
        }
        System.arraycopy(permutationTable, 0, permutationTable, TABLE_SIZE, TABLE_SIZE);
    }

    public double noise(double x, double y) {
        int octaves = 4;
        double persistence = 0.5;
        double frequency = 1;
        double amplitude = 1;
        double total = 0;

        for (int i = 0; i < octaves; i++) {
            total += singlePerlinNoise(x * frequency, y * frequency) * amplitude;
            frequency *= 2;
            amplitude *= persistence;
        }

        total = (total + 1) / 2;

        // Ajout des montagnes
        double mountainHeight = generateMountainNoise(x, y);
        total = BASE_HEIGHT + (total * MAX_RELIEF) + mountainHeight;

        return total;
    }

    private double generateMountainNoise(double x, double y) {
        // Génère des montagnes occasionnelles basées sur un bruit plus rare
        double mountainNoise = singlePerlinNoise(x * MOUNTAIN_FREQUENCY, y * MOUNTAIN_FREQUENCY);
        mountainNoise = (mountainNoise + 1) / 2; // Normaliser entre 0 et 1
        return mountainNoise * MOUNTAIN_AMPLITUDE;
    }

    private double singlePerlinNoise(double x, double y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);

        double u = fade(x);
        double v = fade(y);

        int A = permutationTable[X] + Y;
        int B = permutationTable[X + 1] + Y;

        return lerp(v,
                lerp(u, grad(permutationTable[A], x, y),
                        grad(permutationTable[B], x - 1, y)),
                lerp(u, grad(permutationTable[A + 1], x, y - 1),
                        grad(permutationTable[B + 1], x - 1, y - 1))
        );
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double grad(int hash, double x, double y) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : 0);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }
}