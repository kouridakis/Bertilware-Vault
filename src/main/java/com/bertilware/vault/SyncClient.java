package com.bertilware.vault;

import javafx.concurrent.Task;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class SyncClient extends Task<Void> {
    @Override
    protected Void call() {
        try (
                DatagramSocket socket = new DatagramSocket();
                ServerSocket serverSocket = new ServerSocket(37517)
        ) {
            socket.setBroadcast(true);

            updateMessage("Broadcasting sync request...");
            // Send hash to every device in the network
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = netInterfaces.nextElement();
                if (netInterface.isLoopback() || !netInterface.isUp()) {
                    continue;
                }

                for (InterfaceAddress address : netInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = address.getBroadcast();
                    if (broadcast == null)
                        continue;

                    byte[] data = VaultManager.getHash();
                    DatagramPacket packet = new DatagramPacket(
                            data, data.length,
                            broadcast, 37515
                    );
                    socket.send(packet);
                }
            }

            // Wait for a connection
            updateMessage("Waiting for response...");
            serverSocket.setSoTimeout(5000);
            Socket dataSocket = serverSocket.accept();

            // Read the received ArrayList
            updateMessage("Connected to other device, receiving data...");
            CipherInputStream cipherIn = new CipherInputStream(
                    dataSocket.getInputStream(),
                    VaultManager.getCipherForSync(Cipher.DECRYPT_MODE)
            );
            ObjectInputStream objectIn = new ObjectInputStream(cipherIn);
            ArrayList<?> receivedList = (ArrayList<?>) objectIn.readObject();
            objectIn.close();

            updateMessage("Unpacking...");
            int newCount = 0;
            int updatedCount = 0;
            for (Object receivedObject : receivedList) {
                if (!(receivedObject instanceof Account receivedAccount)) {
                    continue;
                }

                // Check if account already exists
                if (VaultManager.getAccounts().contains(receivedAccount)) {
                    continue;
                }

                // Check if account is an updated version of an existing one
                boolean found = false;
                for (Iterator<Account> iterator = VaultManager.getAccounts().iterator(); iterator.hasNext(); ) {
                    Account local = iterator.next();

                    if (receivedAccount.isAlternativeOf(local) && receivedAccount.isNewerThan(local)) {
                        iterator.remove();
                        updatedCount++;
                        found = true;
                    }
                }

                VaultManager.getAccounts().add(receivedAccount);
                newCount = found ? newCount : newCount + 1;
            }

            updateMessage(String.format(
                    "Syncing finished with the following results:%n" +
                    "Received %d new accounts and updated %d accounts", newCount, updatedCount)
            );
        } catch (SocketTimeoutException e) {
            updateMessage("No response was received.");
        } catch (Exception e) {
            updateMessage(String.format("Something went wrong with the syncing process.%n%nDetails: %s", e.getMessage()));
        }
        return null;
    }
}
