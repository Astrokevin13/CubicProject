package com.CubicIdea.game;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES;

public class Debug {
    private static boolean showFPS = false;
    private static int fps = 0;
    private static long lastTime = System.currentTimeMillis();
    private static int frameCount = 0;
    private static long nvgContext;

    // Initialisation de NanoVG
    public static void init() {
        nvgContext = NanoVGGL3.nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (nvgContext == 0) {
            throw new IllegalStateException("Échec de l'initialisation de NanoVG !");
        }
        System.out.println("NanoVG initialisé.");
    }

    public static void cleanup() {
        NanoVGGL3.nvgDelete(nvgContext);
    }

    public static void loadFont(String fontPath) {
        int fontResult = nvgCreateFont(nvgContext, "sans", fontPath);
        if (fontResult == -1) {
            throw new IllegalStateException("Échec du chargement de la police: " + fontPath);
        }
    }

    public static void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 1000) {
            fps = frameCount;
            frameCount = 0;
            lastTime = currentTime;
        }
    }

    private static boolean wasF1Pressed = false;

    public static void toggleFPS(long window) {
                boolean isF1Pressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_F1) == GLFW.GLFW_PRESS;
        if (isF1Pressed && !wasF1Pressed) {
            showFPS = !showFPS;
        }
        wasF1Pressed = isF1Pressed;
    }

    public static void renderFPS(int windowWidth, int windowHeight) {
        if (showFPS) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glOrtho(0, windowWidth, windowHeight, 0, -1, 1);

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();

            nvgBeginFrame(nvgContext, windowWidth, windowHeight, 1);

            nvgFontSize(nvgContext, 24.0f);
            nvgFontFace(nvgContext, "sans");
            NVGColor color = NVGColor.create();
            nvgRGBA((byte) 255, (byte) 255, (byte) 255, (byte) 255, color);
            nvgFillColor(nvgContext, color);
            nvgTextAlign(nvgContext, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);

            // Arrondi du FPS avec Math.floor()
            int roundedFPS = (int) Math.floor(fps);

            // Affichage du FPS arrondi
            String fpsText = "FPS: " + roundedFPS;
            nvgText(nvgContext, 10, 10, fpsText);

            // Afficher les coordonnées de la caméra
            String cameraCoordinates = "Camera X: " + (int) Main.getCameraX() + " Y: " + (int) Main.getCameraY() + " Z: " + (int) Main.getCameraZ();
            nvgText(nvgContext, 10, 40, cameraCoordinates); // Affiche les coordonnées à 40 pixels de hauteur

            nvgEndFrame(nvgContext);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }
}

