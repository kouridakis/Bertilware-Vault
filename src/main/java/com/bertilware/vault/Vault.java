package com.bertilware.vault;

public class Vault {
    public static void main(String[] args) {
        // Create the SyncServer background thread
        SyncServer sync = new SyncServer();
        Thread thread = new Thread(sync);
        thread.start();

        MainApplication.launchApplication();

        sync.halt();
        System.exit(0);
    }
}