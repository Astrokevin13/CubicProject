package com.CubicIdea.game;

public class RenderDistanceManager {
    private int result; // Variable pour stocker le résultat

    // Enumération des opérations possibles
    public enum Operation {
        MULTIPLY,
        DIVIDE
    }

    // Méthode pour appliquer une opération avec une valeur personnalisée
    public void applyOperation(int value, Operation operation, int customValue) {
        switch (operation) {
            case MULTIPLY:
                this.result = value * customValue; // Multiplication avec la valeur personnalisée
                break;
            case DIVIDE:
                // Vérification pour éviter une division par zéro
                if (customValue != 0) {
                    this.result = value / customValue; // Division avec la valeur personnalisée
                } else {
                    System.out.println("Erreur : division par zéro.");
                    this.result = 0;
                }
                break;
            default:
                System.out.println("Opération non reconnue.");
                this.result = 0;
        }
    }

    // Méthode pour obtenir le résultat
    public int getResult() {
        return this.result;
    }
}