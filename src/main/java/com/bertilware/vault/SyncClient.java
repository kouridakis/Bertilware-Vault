package com.bertilware.vault;

import javafx.concurrent.Task;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class SyncClient extends Task<Void> {
    @Override
    protected Void call() throws Exception {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(5000);

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

                    byte[] data = EncryptionManager.getHash();
                    DatagramPacket packet = new DatagramPacket(
                            data, data.length,
                            broadcast, 7515
                    );
                    socket.send(packet);
                }
            }

            updateMessage("Waiting for response...");
            // Wait for a response
            byte[] buffer = new byte[1500000];
            DatagramPacket replyPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(replyPacket);

            updateMessage("Processing received data...");
            byte[] data = replyPacket.getData();
            ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
            ObjectInputStream objectIn = new ObjectInputStream(byteIn);
            ArrayList<Account> receivedAccounts = (ArrayList<Account>) objectIn.readObject();
            objectIn.close();
            socket.close();

            // Discard duplicate accounts
            receivedAccounts.removeIf(receivedAccount -> EncryptionManager.getAccounts().contains(receivedAccount));

            int newCount = receivedAccounts.size();
            int updatedCount = 0;
            EncryptionManager.getAccounts().addAll(receivedAccounts);
            for (Iterator<Account> iterator = EncryptionManager.getAccounts().iterator(); iterator.hasNext(); ) {
                Account account = iterator.next();

                // If a newer version is found, remove the current outdated version
                for (int i = 0; i < EncryptionManager.getAccounts().size(); i++) {
                    Account other = EncryptionManager.getAccounts().get(i);
                    if (other.isAlternativeOf(account) && other.isNewerThan(account)) {
                        iterator.remove();
                        newCount--;
                        updatedCount++;
                    }
                }
            }

            if (newCount < 0)
                newCount = 0;
            updateMessage(String.format("Syncing finished with the following results:%n" +
                    "Received %d new accounts and updated %d accounts", newCount, updatedCount)
            );
        } catch (SocketTimeoutException e) {
            updateMessage("No response was received.");
        }
        return null;
    }
}
