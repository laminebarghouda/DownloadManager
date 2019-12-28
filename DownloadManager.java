package main.java.app;

public interface DownloadManager {
    // Soumet une tâche de téléchargement à effectuer dès que possible. Méthode non bloquante.
    void submit(Download dl);

}

