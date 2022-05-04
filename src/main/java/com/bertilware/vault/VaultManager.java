package com.bertilware.vault;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public final class VaultManager {
    // Record that represents a user's saved data.
    private record VaultUserData(ArrayList<Account> accounts, String notes, String theme) implements Serializable {
        public ArrayList<Account> getAccounts() {
            return accounts;
        }
        public String getNotes() {
            return notes;
        }
        public String getTheme() {
            return theme;
        }
    }

    private static ArrayList<Account> accounts = null;
    private static String notes = "";
    private static String theme = VaultThemes.BERTILWARE;
    private static String user = "";
    private static byte[] hash;
    private static final File vaultDirectory = new File(
            System.getProperty("user.home") + File.separator +
                    ".bertilware" + File.separator + "vault"
    );
    private static File userFile;
    private static Key key;
    private static Cipher cipher;

    // Getters
    public static ArrayList<Account> getAccounts() { return accounts; }
    public static String getTheme() {
        return theme;
    }
    public static String getNotes() {
        return notes;
    }
    public static String getUser() {
        return user;
    }
    public static byte[] getHash() {
        return hash;
    }
    public static File getVaultDirectory() {
        return vaultDirectory;
    }
    public static File getUserFile() {
        return userFile;
    }

    // Setters
    public static void setTheme(String theme) {
        VaultManager.theme = theme;
    }
    public static void setNotes(String notes) {
        VaultManager.notes = notes;
    }
    public static void setUser(String user) {
        VaultManager.user = user;
    }

    public static void initialize(String username, String password)
            throws NoSuchPaddingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // Run the password through SHA-256 to get a 256-bit key
        byte[] raw = md.digest(password.getBytes(StandardCharsets.UTF_8));

        userFile = new File(
                vaultDirectory + File.separator +
                        username + ".vault"
        );
        user = username;
        key = new SecretKeySpec(raw, "AES");
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // Run both the username and password through SHA-256 for syncing
        hash = md.digest((user + password).getBytes(StandardCharsets.UTF_8));
    }

    public static void encrypt()
            throws IOException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {

        // Read the user's data
        VaultUserData data = new VaultUserData(
                accounts == null ? new ArrayList<>() : accounts,
                notes,
                theme
        );

        // Serialize the vault object into a ByteArrayOutputStream,
        // and read that output stream into a byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
        objectOut.writeObject(data);
        objectOut.close();
        byte[] decrypted = byteOut.toByteArray();

        // Create a new byte array and populate it using a SecureRandom generator
        byte[] ivArray = new byte[16];
        SecureRandom generator = new SecureRandom();
        generator.nextBytes(ivArray);
        // Use that random byte array as an IV for the cipher
        IvParameterSpec iv = new IvParameterSpec(ivArray);

        // Initialize cipher to encryption mode,
        // and encrypt the serialized object
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrypted = cipher.doFinal(decrypted);


        byte[] vaultFile = new byte[encrypted.length + ivArray.length];
        // Add the encrypted object
        System.arraycopy(encrypted, 0, vaultFile, 0, encrypted.length);
        // Append the IV bytes
        System.arraycopy(ivArray, 0,vaultFile, encrypted.length, ivArray.length);
        // Write to disk
        Files.write(userFile.toPath(), vaultFile);

        saveSession();
    }

    public static void decrypt()
            throws IOException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, ClassNotFoundException {
        // Read encrypted object and the IV bytes from the user's file
        byte[] vaultFile = Files.readAllBytes(userFile.toPath());

        // Copy the encrypted object from the read bytes
        byte[] encrypted = new byte[vaultFile.length - 16];
        System.arraycopy(vaultFile, 0, encrypted, 0, vaultFile.length - 16);
        // Copy the IV bytes from read bytes
        byte[] byteArray = new byte[16];
        System.arraycopy(vaultFile, encrypted.length, byteArray, 0, 16);
        // Create the IV
        IvParameterSpec iv = new IvParameterSpec(byteArray);

        // Try to decrypt the file
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decrypted = cipher.doFinal(encrypted);

        // Create input streams and read serialized vault object
        ByteArrayInputStream byteIn = new ByteArrayInputStream(decrypted);
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        VaultUserData data = (VaultUserData) objectIn.readObject();
        objectIn.close();

        // Set the accounts and the theme
        accounts = data.getAccounts();
        notes = data.getNotes();
        theme = data.getTheme();

        saveSession();
    }

    public static Cipher getCipherForSync(int mode)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        // CTR instead of CBC because padding was giving issues during sync
        Cipher syncCipher = Cipher.getInstance("AES/CTR/NoPadding");
        byte[] nonce = Arrays.copyOf(hash, 12);
        byte[] nonceAndCounter = new byte[16];
        System.arraycopy(nonce, 0, nonceAndCounter, 0, nonce.length);
        IvParameterSpec iv = new IvParameterSpec(nonceAndCounter);
        syncCipher.init(mode, key, iv);
        return syncCipher;
    }

    public static void readSession() throws FileNotFoundException {
        File session = new File(vaultDirectory + File.separator + "session.txt");
        if (!session.exists())
            return;

        // Count the lines within the session file,
        // and exit if it does not have the required amount
        Scanner in = new Scanner(session);
        int count = 0;
        while (in.hasNextLine()) {
            in.nextLine();
            count++;
        }
        in.close();

        if (count != 2)
            return;

        // Finally, read the last session's info
        in = new Scanner(session);
        user = in.nextLine();
        theme = in.nextLine();
        in.close();
    }

    private static void saveSession() throws FileNotFoundException {
        // Retain some information about the current session
        File session = new File(vaultDirectory + File.separator + "session.txt");
        PrintWriter writer = new PrintWriter(session);
        writer.println(user);
        writer.println(theme);
        writer.close();
    }
}