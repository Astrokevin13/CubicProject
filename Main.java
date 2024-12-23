package com.CubicIdea.game;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class Main {
    private static float cameraX = 168.0f;
    private static float cameraY = 50.0f;
    private static float cameraZ = -352.0f; // Position initiale (5 unités en arrière)
    private static float rotationY = 0.0f;
    private static float rotationX = 0.0f; // Rotation verticale (pitch)
    private static long window;
    private static com.CubicIdea.game.chunkManager chunkManager;

    private static float NoiseScale = 2.0f;
    private static int baseHeight = 15;
    private static int reliefHeight = 16;
    public static float getCameraX() {
        return cameraX;
    }

    public static float getCameraY() {
        return cameraY;
    }

    public static float getCameraZ() {
        return cameraZ;
    }
    public static float getRotationY() {
        return rotationY;
    }

    public static float getRotationX() {
        return rotationX;
    }
    // Ajout d'un seed pour la génération du monde
    private static final Long WORLD_SEED = 1L; // Mettre un Long ou laisser null pour un seed aléatoire
        public static int chunkRadius = 2;
    private static final int KEY_FORWARD = GLFW.GLFW_KEY_Z; // Z pour avancer
    private static final int KEY_BACKWARD = GLFW.GLFW_KEY_S; // S pour reculer
    private static final int KEY_LEFT = GLFW.GLFW_KEY_Q; // Q pour gauche
    private static final int KEY_RIGHT = GLFW.GLFW_KEY_D;
    private static int windowWidth = 800;  // Largeur de la fenêtre
    private static int windowHeight = 600;
    public static chunkManager getChunkManager() {
        return chunkManager;
    }




    public static void main(String[] args) {
        // Initialisation de GLFW
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) throw new IllegalStateException("Impossible d'initialiser GLFW");

        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, "SomethingCubic", 0, 0);
        if (window == 0) throw new RuntimeException("Échec de la création de la fenêtre");

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glEnable(GL11.GL_DEPTH_TEST); // Activer le test de profondeur pour gérer les objets en 3D
        Debug.init();
        Debug.loadFont("com/CubicIdea/game/ressource/Silkscreen.ttf"); // Configuration de la projection initiale
        adjustProjection();
        GLFW.glfwSetFramebufferSizeCallback(window, (window, width, height) -> adjustProjection());

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glFrustum(-1.0, 1.0, -1.0, 1.0, 1.0, 100.0); // Perspective
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_FOG); // Active le brouillard

        // Réglage du mode de brouillard (ici un brouillard linéaire)
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
        RenderDistanceManager renderManager = new RenderDistanceManager(); // Créer une instance de RenderDistanceManager
        // Exemple de valeur à multiplier ou diviser
        int FogEndMultiplier = 20; // Facteur personnalisé
        // Appliquer une multiplication avec le facteur personnalisé
        renderManager.applyOperation(chunkRadius, RenderDistanceManager.Operation.MULTIPLY, FogEndMultiplier);
        int FogEnd = renderManager.getResult();
        int FogStartMultiplier = 5;

        renderManager.applyOperation(chunkRadius, RenderDistanceManager.Operation.MULTIPLY, FogStartMultiplier);
        int FogStartResult = renderManager.getResult();



        // Paramètres du brouillard : distance de début et de fin
        GL11.glFogf(GL11.GL_FOG_START, FogStartResult);  // Début du brouillard
        GL11.glFogf(GL11.GL_FOG_END, FogEnd);    // Fin du brouillard

        // Configuration de la couleur du brouillard (utilisation de RGBA)
        GL11.glFogfv(GL11.GL_FOG_COLOR, new float[]{0.2f, 0.3f, 0.5f, 1.5f});  // RGBA

        BlockTextureRegistry.initializeTextures();
        TextureManager.loadTexture(); // Charger la texture une seule fois

        chunkManager = new chunkManager(WORLD_SEED, baseHeight, reliefHeight, NoiseScale);
        while (!GLFW.glfwWindowShouldClose(window)) {

            glEnable(GL11.GL_DEPTH_TEST);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Configurez la matrice de vue
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            double[] mousePosX = new double[1];
            double[] mousePosY = new double[1];
            GLFW.glfwGetCursorPos(window, mousePosX, mousePosY);

            double deltaX = mousePosX[0] - 400.0; // Calculer le delta depuis le centre de la fenêtre
            double deltaY = mousePosY[0] - 300.0;

            rotationY += deltaX * 0.1f; // Rotation autour de l'axe Y (horizontal)
            rotationX += deltaY * 0.1f; // Rotation verticale (pitch)
            if (rotationX > 89.0f) rotationX = 89.0f;
            if (rotationX < -89.0f) rotationX = -89.0f;

            // Recentrer la souris au centre de la fenêtre
            GLFW.glfwSetCursorPos(window, 400.0, 300.0);

            GL11.glClearColor(0.2f, 0.3f, 0.5f, 1.0f); // Fond bleu clair

            // Calcul de la direction de la caméra
            double dirX = Math.sin(Math.toRadians(rotationY));
            double dirY = -Math.sin(Math.toRadians(rotationX));
            double dirZ = -Math.cos(Math.toRadians(rotationY));
            
            double leftX = Math.sin(Math.toRadians(rotationY - 90));
            double leftZ = -Math.cos(Math.toRadians(rotationY - 90));

            if (GLFW.glfwGetKey(window, KEY_FORWARD) == GLFW.GLFW_PRESS) {
                cameraX += dirX * 1f;
                cameraY += dirY * 1f;
                cameraZ += dirZ * 1f;
            }
            if (GLFW.glfwGetKey(window, KEY_BACKWARD) == GLFW.GLFW_PRESS) {
                cameraX -= dirX * 1f;
                cameraY -= dirY * 1f;
                cameraZ -= dirZ * 1f;
            }
            if (GLFW.glfwGetKey(window, KEY_LEFT) == GLFW.GLFW_PRESS) {
                cameraX += leftX * 1f;
                cameraZ += leftZ * 1f;
            }
            if (GLFW.glfwGetKey(window, KEY_RIGHT) == GLFW.GLFW_PRESS) {
                cameraX -= leftX * 1f;
                cameraZ -= leftZ * 1f;
            }
            GL11.glLoadIdentity();
            GL11.glRotatef(rotationX, 1.0f, 0.0f, 0.0f); // Rotation verticale
            GL11.glRotatef(rotationY, 0.0f, 1.0f, 0.0f); // Rotation horizontale
            GL11.glTranslatef(-cameraX, -cameraY, -cameraZ); // Position de la caméra
            chunkManager.updateChunks(cameraX, cameraZ, chunkRadius);

            for (Chunk chunk : chunkManager.getLoadedChunks()) {
                chunk.render();
            }

            Debug.updateFPS();
            Debug.toggleFPS(window); // Affichage du debug (FPS)

            // Affichage des FPS (en haut à gauche)
            int[] width = new int[1];
            int[] height = new int[1];
            GLFW.glfwGetFramebufferSize(window, width, height);
            Debug.renderFPS(width[0], height[0]);

            // Rendu du terrain
            GL11.glEnd();

            // Swap buffers
            GLFW.glfwSwapBuffers(window);

            // Poll events
            GLFW.glfwPollEvents();
        }

        // Libérer les ressources et fermer
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
    public static int getWindowWidth() {
        return windowWidth;
    }

    public static int getWindowHeight() {
        return windowHeight;
    }
    // Ajustement de la projection pour la fenêtre
    private static void adjustProjection() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glFrustum(-1.0, 1.0, -1.0, 3.0, 1.0, 100.0); // Perspective
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

}
