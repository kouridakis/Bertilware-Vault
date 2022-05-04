package com.bertilware.vault;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;

public class SyncServer implements Runnable {
    private boolean halt;
    public SyncServer() {
        halt = false;
    }

    @Override
    public void run() {
        while (!halt) {
            try (DatagramSocket broadcastSocket = new DatagramSocket(37515, InetAddress.getByName("0.0.0.0"))) {
                broadcastSocket.setBroadcast(true);
                // Wait for a hash UDP packet
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                broadcastSocket.setSoTimeout(60000);
                broadcastSocket.receive(packet);
                // Ignore if it originated from the same host
                if (packet.getAddress().getHostAddress().equals(InetAddress.getLocalHost().getHostAddress()))
                    continue;

                // Trim down to only the required bytes
                byte[] receivedHash = Arrays.copyOfRange(packet.getData(), 0, 64);

                // Check if hashes match
                if (Arrays.equals(receivedHash, VaultManager.getHash())) {
                    // Initiate a TCP connection and send all saved accounts
                    SocketAddress address = new InetSocketAddress(packet.getAddress().getHostAddress(), 37517);
                    try (Socket dataSocket = new Socket()) {
                        dataSocket.connect(address, 5000);

                        CipherOutputStream cipherOut = new CipherOutputStream(
                                dataSocket.getOutputStream(),
                                VaultManager.getCipherForSync(Cipher.ENCRYPT_MODE)
                        );
                        ObjectOutputStream objectOut = new ObjectOutputStream(cipherOut);
                        objectOut.writeObject(VaultManager.getAccounts());
                        objectOut.close();
                    }
                }
            } catch (SocketTimeoutException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void halt() {
        halt = true;
    }
}
