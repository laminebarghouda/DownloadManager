package main.java.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.lang.Runnable ;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File ;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public  class  Download implements Runnable {
    /** The state of the download */


    // These are the status names.
    public static final String STATUSES[] = {"Downloading",
            "Paused", "Complete", "Cancelled", "Error"};

    // Contants for download's state
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETED = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    protected int mState;


    private String link;

    public static String[] getSTATUSES() {
        return STATUSES;
    }

    public String getLink() {
        return link;
    }

    public File getOut() {
        return out;
    }

    public double getPercentdownloaded() {
        return percentdownloaded;
    }

    private File out;
    private URL url ;
    private HttpURLConnection http ;
    private double fileSize ;
    BufferedInputStream in ;
    FileOutputStream fos ;
    BufferedOutputStream bout ;
    double encien_perc;
    double percentdownloaded;
    ObservableList<Download> data = null;


    public Download(String link, File out,ObservableList<Download> data) throws IOException {
        this.link = link;
        this.out = out;
        setState(DOWNLOADING);
        url = new URL(link);
        http = (HttpURLConnection) url.openConnection();
        fileSize = (double) http.getContentLengthLong();
        in = new BufferedInputStream(http.getInputStream());
        fos = new FileOutputStream(out);
        bout = new BufferedOutputStream(fos, 1024);
        encien_perc =0.00;
        this.data = data;


    }
    /* Optimisation de l'affichage console
        author : Lamine
     */
    protected void setState(int value) {
        mState = value;
        if(mState == DOWNLOADING)
            System.out.println("Downloading : " + link.substring(link.lastIndexOf('/')+1));
        else if(mState == PAUSED)
            System.out.println("Download Paused : " + link.substring(link.lastIndexOf('/')+1));
        else if (mState == COMPLETED)
            System.out.println("Download Complete : " + link.substring(link.lastIndexOf('/')+1));
    }
    /* Fonction qui retoure le state en cours utilisé pour controler les paramètres des actions pause et resume (on peut pas par exemple resumer un fichier déja en cours de téléchargement)
       author : Lamine
    */
    public int getmState(){
        return mState;
    }


    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            double downloaded = 0.00;
            int read = 0;
            read = in.read(buffer, 0, 1024);
             /* Compteur pour mettre à jour le pourcentage de téléchargement
                 author : Lamine
            */
            int seconde = 0;
            while (read >= 0 && this.mState==DOWNLOADING ) {
                this.bout.write(buffer, 0, read);
                downloaded += read;
                percentdownloaded =((downloaded * 100) / fileSize);
                seconde++;
                 /* Chaque 1 seconde presque les pourcentages de téléchargement sont mis à jours graçe à la fonction update et un mis à jour final si pourcentage = 100%
                    author : Lamine
                 */
                if((seconde==1000) || (percentdownloaded==100)) {
                    update();
                    seconde = 0;
                }
                    read = in.read(buffer, 0, 1024);

            }

            if (this.mState != PAUSED) {
                bout.close();
                in.close();
                http.disconnect();
                setState(COMPLETED);
                percentdownloaded=100;
                update();
            }
             /* Gestion du cas ou le connextion est perdu !
                Cause possible : Malfonctionnement du serveur ou bien pause prolongé de la part de l'utilisateur
                 author : Lamine
            */
        } catch (javax.net.ssl.SSLException s){
            System.out.println("Conncetion Perdu pour " + link.substring(link.lastIndexOf('/')+1) + " !! Peut être vous avez suspendu longement le télechargement ! Réessayez de nouveau");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /* Fonction qui permet de mettre à jour les pourcentages de téléchargements, elle remplace les anciens data par ceux actuelles chaque 1 seconde presque !
       Fonction très synchronisé pour palier à l'accès en même temps et le risque de l'incohérence de données
       author : Lamine
    */
    public synchronized void update () {
        synchronized (this) {
                ObservableList<Download> downloads = FXCollections.observableArrayList();
                for (int i = 0; i < data.size(); i++) {
                    downloads.add(data.get(i));
                }
                data.remove(0,data.size());
                for (int i = 0; i < downloads.size(); i++)
                    data.add(downloads.get(i));


        }
    }

    public void pause() {
        setState(PAUSED);
        this.encien_perc=percentdownloaded;
    }

    public void resume() {
        setState(DOWNLOADING);
        run();
    }

}
