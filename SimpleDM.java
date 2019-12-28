package main.java.app;

import java.util.ArrayList;

public class SimpleDM implements DownloadManager {

    public ArrayList<Download> array_download =new ArrayList<Download>();

    @Override
    public void submit(Download t) {
        new Thread(() -> {
                t.run();
        }).start();
    }

    public void pause(Download t){
        new Thread(() -> {
                t.pause();
        }).start();
    }
    public void resume(Download t){
        new Thread(() -> {
                t.resume();
        }).start();
    }


}
