package com.company;

import java.io.*;
import java.math.BigInteger;

public class KeyExchange {
    public Client.UserInformation informationAboutNewFriend;
    public Client.UserInformation inf;
    private DiffieHellman diffieHellman;
    public KeyExchange(String friendInformation, String myInformation) throws Exception {
        informationAboutNewFriend = new Client.UserInformation(friendInformation);
        inf = new Client.UserInformation(myInformation);

        File file = new File( "key" + informationAboutNewFriend.IDFriend + ".txt");
        if (!file.exists())
            file.createNewFile();
        diffieHellman = new DiffieHellman();
    }
    public void sendK(String myInformation) throws IOException {
        diffieHellman.sendPublicKey(informationAboutNewFriend.IPFriend, informationAboutNewFriend.portFriend,
                inf.IDFriend, informationAboutNewFriend.IDFriend, inf.loginFriend, myInformation);
    }

    public void genSecret(BigInteger publicKeyFriend) throws IOException {
        BigInteger sharedKeyA = publicKeyFriend.modPow(diffieHellman.getSecretA(), diffieHellman.getPrimeValue());
        File file = new File( "keys.txt");
        if (!file.exists())
            file.createNewFile();

        FileWriter writer = new FileWriter("keys.txt", true);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write(informationAboutNewFriend.IDFriend  + " " + sharedKeyA.toString() + "\n");
        bufferedWriter.close();
    }
}