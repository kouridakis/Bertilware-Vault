package com.bertilware.vault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class SyncServer implements Runnable {
    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(7515, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                // Receive hash
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                // Ignore if it originated from the same host
                if (packet.getAddress().getHostAddress().equals(InetAddress.getLocalHost().getHostAddress()))
                    continue;

                // Trim down to only the required bytes
                byte[] receivedHash = Arrays.copyOfRange(packet.getData(), 0, 64);

                DatagramPacket replyPacket;
                // Compare received hash with local one
                if (Arrays.equals(receivedHash, EncryptionManager.getHash()))  {
                    // Create UDP packet and send
                    try {
                        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
                        objectOut.writeObject(EncryptionManager.getAccounts());
                        objectOut.close();
                        byte[] accounts = byteOut.toByteArray();

                        replyPacket = new DatagramPacket(
                                accounts, accounts.length,
                                packet.getAddress(), packet.getPort()
                        );
                        socket.send(replyPacket);
                    } catch (Exception e) {
                        VaultDialog.createErrorDialog(e);
                    }
                }
            }

        } catch (IOException e) {
            VaultDialog.createErrorDialog(e);
        }
    }
}
