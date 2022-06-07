package com.company;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Sender {
    public Sender(String friendIP, String friendPort, String myID, String friendID, String myLogin, String pass) throws IOException {
        String hostName = friendIP;
        int portNumber = Integer.parseInt(friendPort);
        try {
            Socket fromServer = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(fromServer.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String fromUser;

            Stream str = new Stream(fromServer);
            str.start();
            boolean flag = true;
            while (flag)
            {
                fromUser = stdIn.readLine();
                if (fromUser != null)
                {
                    if (fromUser.equals("5"))
                    {
                        flag = false;
                    }
                    else {
                        //ШИФРОВАНИЕ СООБЩЕНИЯ
                        String temp;
                        temp = encrypt(fromUser, friendID);

                        //ОПРАВКА СООБЩЕНИЯ
                        out.println(myID + temp);
                        FileWriter writer = new FileWriter(friendID + ".txt", true);
                        BufferedWriter bufferedWriter = new BufferedWriter(writer);

                        bufferedWriter.write(encrypt1(myLogin + "\t" + fromUser, pass) + "\n");
                        bufferedWriter.close();
                    }
                }
            }
        }
        catch (UnknownHostException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
    }
    static class Stream extends Thread
    {
        private BufferedReader in;
        Stream(Socket socket)  throws IOException
        {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        public void run()
        {
            String message;
            try
            {
                while ((message = in.readLine()) != null)
                {
                    System.out.println(message);
                }
            }
            catch(IOException e) { e.printStackTrace(); }
        }
    }

    public String encrypt(String message, String IDFriend) throws IOException {

        BufferedReader reader = new BufferedReader(
                new FileReader("keys.txt"));
        String line = "";
        while (!line.contains(IDFriend))
        {
            line = reader.readLine();
        }

        byte[][] block1 = { { 0x4, 0xA, 0x9, 0x2, 0xD, 0x8, 0x0, 0xE, 0x6, 0xB, 0x1, 0xC, 0x7, 0xF, 0x5, 0x3 },
                { 0xE, 0xB, 0x4, 0xC, 0x6, 0xD, 0xF, 0xA, 0x2, 0x3, 0x8, 0x1, 0x0, 0x7, 0x5, 0x9 },
                { 0x5, 0x8, 0x1, 0xD, 0xA, 0x3, 0x4, 0x2, 0xE, 0xF, 0xC, 0x7, 0x6, 0x0, 0x9, 0xB },
                { 0x7, 0xD, 0xA, 0x1, 0x0, 0x8, 0x9, 0xF, 0xE, 0x4, 0x6, 0xC, 0xB, 0x2, 0x5, 0x3 },
                { 0x6, 0xC, 0x7, 0x1, 0x5, 0xF, 0xD, 0x8, 0x4, 0xA, 0x9, 0xE, 0x0, 0x3, 0xB, 0x2 },
                { 0x4, 0xB, 0xA, 0x0, 0x7, 0x2, 0x1, 0xD, 0x3, 0x6, 0x8, 0x5, 0x9, 0xC, 0xF, 0xE },
                { 0xD, 0xB, 0x4, 0x1, 0x3, 0xF, 0x5, 0x9, 0x0, 0xA, 0xE, 0x7, 0x6, 0x8, 0x2, 0xC },
                { 0x1, 0xF, 0xD, 0x0, 0x5, 0x7, 0xA, 0x4, 0x9, 0x2, 0x3, 0xE, 0x6, 0xB, 0x8, 0xC } };

        byte[][] block2 = { { 0x9, 0x6, 0x3, 0x2, 0x8, 0xB, 0x1, 0x7, 0xA, 0x4, 0xE, 0xF, 0xC, 0x0, 0xD, 0x5 },
                { 0x3, 0x7, 0xE, 0x9, 0x8, 0xA, 0xF, 0x0, 0x5, 0x2, 0x6, 0xC, 0xB, 0x4, 0xD, 0x1 },
                { 0xE, 0x4, 0x6, 0x2, 0xB, 0x3, 0xD, 0x8, 0xC, 0xF, 0x5, 0xA, 0x0, 0x7, 0x1, 0x9 },
                { 0xE, 0x7, 0xA, 0xC, 0xD, 0x1, 0x3, 0x9, 0x0, 0x2, 0xB, 0x4, 0xF, 0x8, 0x5, 0x6 },
                { 0xB, 0x5, 0x1, 0x9, 0x8, 0xD, 0xF, 0x0, 0xE, 0x4, 0x2, 0x3, 0xC, 0x7, 0xA, 0x6 },
                { 0x3, 0xA, 0xD, 0xC, 0x1, 0x2, 0x0, 0xB, 0x7, 0x5, 0x9, 0x4, 0x8, 0xF, 0xE, 0x6 },
                { 0x1, 0xD, 0x2, 0x9, 0x7, 0xA, 0x6, 0x0, 0x8, 0xC, 0x4, 0x5, 0xF, 0x3, 0xB, 0xE },
                { 0xB, 0xA, 0xF, 0x5, 0x0, 0xC, 0xE, 0x8, 0x6, 0x2, 0x3, 0x9, 0x1, 0x7, 0xD, 0x4 } };

        String keyTest = line.substring(6, 70);

        String s = message;

        Cipher encryptor = new Cipher();
        byte[] bytesForEnc = s.getBytes();
        byte[] key = DatatypeConverter.parseHexBinary(keyTest);
        encryptor = new Cipher(block2);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32_000_000; i++) {
            sb.append('0');
        }
        Hasher hasher = new Hasher();
        return DatatypeConverter.printHexBinary(hasher.hash(keyTest)) +
                DatatypeConverter.printHexBinary(encryptor.encrypt(bytesForEnc, key));
    }

    public String encrypt1(String message, String password) {

        byte[][] block1 = { { 0x4, 0xA, 0x9, 0x2, 0xD, 0x8, 0x0, 0xE, 0x6, 0xB, 0x1, 0xC, 0x7, 0xF, 0x5, 0x3 },
                { 0xE, 0xB, 0x4, 0xC, 0x6, 0xD, 0xF, 0xA, 0x2, 0x3, 0x8, 0x1, 0x0, 0x7, 0x5, 0x9 },
                { 0x5, 0x8, 0x1, 0xD, 0xA, 0x3, 0x4, 0x2, 0xE, 0xF, 0xC, 0x7, 0x6, 0x0, 0x9, 0xB },
                { 0x7, 0xD, 0xA, 0x1, 0x0, 0x8, 0x9, 0xF, 0xE, 0x4, 0x6, 0xC, 0xB, 0x2, 0x5, 0x3 },
                { 0x6, 0xC, 0x7, 0x1, 0x5, 0xF, 0xD, 0x8, 0x4, 0xA, 0x9, 0xE, 0x0, 0x3, 0xB, 0x2 },
                { 0x4, 0xB, 0xA, 0x0, 0x7, 0x2, 0x1, 0xD, 0x3, 0x6, 0x8, 0x5, 0x9, 0xC, 0xF, 0xE },
                { 0xD, 0xB, 0x4, 0x1, 0x3, 0xF, 0x5, 0x9, 0x0, 0xA, 0xE, 0x7, 0x6, 0x8, 0x2, 0xC },
                { 0x1, 0xF, 0xD, 0x0, 0x5, 0x7, 0xA, 0x4, 0x9, 0x2, 0x3, 0xE, 0x6, 0xB, 0x8, 0xC } };

        byte[][] block2 = { { 0x9, 0x6, 0x3, 0x2, 0x8, 0xB, 0x1, 0x7, 0xA, 0x4, 0xE, 0xF, 0xC, 0x0, 0xD, 0x5 },
                { 0x3, 0x7, 0xE, 0x9, 0x8, 0xA, 0xF, 0x0, 0x5, 0x2, 0x6, 0xC, 0xB, 0x4, 0xD, 0x1 },
                { 0xE, 0x4, 0x6, 0x2, 0xB, 0x3, 0xD, 0x8, 0xC, 0xF, 0x5, 0xA, 0x0, 0x7, 0x1, 0x9 },
                { 0xE, 0x7, 0xA, 0xC, 0xD, 0x1, 0x3, 0x9, 0x0, 0x2, 0xB, 0x4, 0xF, 0x8, 0x5, 0x6 },
                { 0xB, 0x5, 0x1, 0x9, 0x8, 0xD, 0xF, 0x0, 0xE, 0x4, 0x2, 0x3, 0xC, 0x7, 0xA, 0x6 },
                { 0x3, 0xA, 0xD, 0xC, 0x1, 0x2, 0x0, 0xB, 0x7, 0x5, 0x9, 0x4, 0x8, 0xF, 0xE, 0x6 },
                { 0x1, 0xD, 0x2, 0x9, 0x7, 0xA, 0x6, 0x0, 0x8, 0xC, 0x4, 0x5, 0xF, 0x3, 0xB, 0xE },
                { 0xB, 0xA, 0xF, 0x5, 0x0, 0xC, 0xE, 0x8, 0x6, 0x2, 0x3, 0x9, 0x1, 0x7, 0xD, 0x4 } };


        Hasher hasher = new Hasher();
        String keyTest = DatatypeConverter.printHexBinary(hasher.hash(password + password));

        String s = message;

        Cipher encryptor = new Cipher();
        byte[] bytesForEnc = s.getBytes();
        byte[] key = DatatypeConverter.parseHexBinary(keyTest);

        encryptor = new Cipher(block2);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32_000_000; i++) {
            sb.append('0');
        }

        return DatatypeConverter.printHexBinary(encryptor.encrypt(bytesForEnc, key));
    }
}