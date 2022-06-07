package com.company;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiffieHellman {

    int bitLength = 512;
    int certainty = 20;
    private static final SecureRandom rnd = new SecureRandom();
    private BigInteger secretA;
    private BigInteger publicA;
    public static BigInteger publicB;
    private BigInteger sharedKeyA;
    private BigInteger primeValue;

    public BigInteger getSharedKeyA()
    {
        return sharedKeyA;
    }

    public DiffieHellman(){
        Random randomGenerator = new Random();
        BigInteger generatorValue;

        primeValue = findPrime();
        primeValue = new BigInteger("1261467933995784831857471345958950204840534085433660717586221873" +
                "5217313940496111021034004666196825858112983186609846701651580605574267710404219849249965931");
        generatorValue = findPrimeRoot(primeValue);
        secretA = new BigInteger(bitLength-2,randomGenerator);
        publicA = generatorValue.modPow(secretA, primeValue);
    }

    public DiffieHellman(BigInteger value, BigInteger pubB)
    {
        Random randomGenerator = new Random();
        BigInteger generatorValue;

        primeValue = value;
        generatorValue	= findPrimeRoot(primeValue);
        secretA = new BigInteger(bitLength-2,randomGenerator);
        publicB = pubB;
        publicA=generatorValue.modPow(secretA, primeValue);
    }

    public void genSecretKey()
    {
        sharedKeyA = publicB.modPow(secretA, primeValue);
    }

    public void genSecretKey(String inf, String myInf) throws IOException {

        Client.UserInformation userInformation = new Client.UserInformation(inf);

        genSecretKey();
        File file = new File( "keys.txt");
        if (!file.exists())
            file.createNewFile();

        FileWriter writer = new FileWriter("keys.txt", true);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write(userInformation.IDFriend  + " " + sharedKeyA.toString() + "\n");
        bufferedWriter.close();

        sendPublicKey(userInformation.IPFriend, userInformation.portFriend, userInformation.IDFriend, myInf);
    }

    public BigInteger getSecretA()
    {
        return secretA;
    }

    public BigInteger getPrimeValue()
    {
        return primeValue;
    }

    private static boolean miller_rabin_pass(BigInteger a, BigInteger n) {
        BigInteger n_minus_one = n.subtract(BigInteger.ONE);
        BigInteger d = n_minus_one;
        int s = d.getLowestSetBit();
        d = d.shiftRight(s);
        BigInteger a_to_power = a.modPow(d, n);
        if (a_to_power.equals(BigInteger.ONE)) return true;
        for (int i = 0; i < s-1; i++) {
            if (a_to_power.equals(n_minus_one)) return true;
            a_to_power = a_to_power.multiply(a_to_power).mod(n);
        }
        if (a_to_power.equals(n_minus_one)) return true;
        return false;
    }

    public static boolean miller_rabin(BigInteger n) {
        for (int repeat = 0; repeat < 20; repeat++) {
            BigInteger a;
            do {
                a = new BigInteger(n.bitLength(), rnd);
            } while (a.equals(BigInteger.ZERO));
            if (!miller_rabin_pass(a, n)) {
                return false;
            }
        }
        return true;
    }

    boolean isPrime(BigInteger r){
        return miller_rabin(r);
    }

    public List<BigInteger> primeFactors(BigInteger number) {
        BigInteger n = number;
        BigInteger i=BigInteger.valueOf(2);
        BigInteger limit=BigInteger.valueOf(10000);
        List<BigInteger> factors = new ArrayList<BigInteger>();
        while (!n.equals(BigInteger.ONE)){
            while (n.mod(i).equals(BigInteger.ZERO)){
                factors.add(i);
                n=n.divide(i);
                if(isPrime(n)){
                    factors.add(n);
                    return factors;
                }
            }
            i=i.add(BigInteger.ONE);
            if(i.equals(limit))return factors;
        }
        System.out.println(factors);
        return factors;
    }

    boolean isPrimeRoot(BigInteger g, BigInteger p)
    {
        BigInteger totient = p.subtract(BigInteger.ONE);
        List<BigInteger> factors = primeFactors(totient);
        int i = 0;
        int j = factors.size();
        for(;i < j; i++)
        {
            BigInteger factor = factors.get(i);//elementAt
            BigInteger t = totient.divide( factor);
            if(g.modPow(t, p).equals(BigInteger.ONE))return false;
        }
        return true;
    }

    String download(String address){
        String txt="";
        URLConnection conn = null;
        InputStream in = null;
        try {
            URL url = new URL(address);
            conn = url.openConnection();
            conn.setReadTimeout(10000);
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            String encoding = "UTF-8";
            while ((numRead = in.read(buffer)) != -1) {
                txt+=new String(buffer, 0, numRead, encoding);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return txt;
    }

    void compareWolfram(BigInteger p){
        String url="http://api.wolframalpha.com/v2/query?appid=&input=primitive+root+"+p;
        System.out.println(url);
        String g= download(url);;
        String[] vals=g.split(".plaintext>");
        if(vals.length<3)	System.out.println(g);
        else System.out.println("wolframalpha generatorValue "+vals[3]);
    }

    BigInteger findPrimeRoot(BigInteger p){
        int start= 2001;
        if(start==2)compareWolfram(p);

        for(int i=start;i<100000000;i++)
            if(isPrimeRoot(BigInteger.valueOf(i),p))
                return BigInteger.valueOf(i);
        return BigInteger.valueOf(0);
    }

    BigInteger findPrime(){
        Random rnd=new Random();
        BigInteger p=BigInteger.ZERO;
        p= new BigInteger(bitLength, certainty, rnd);
        return p;
    }

    public void sendPublicKey(String friendIP, String friendPort, String myID, String friendID, String myLogin, String inform) throws IOException {
        SenderKey senderKey = new SenderKey(friendIP, friendPort, myID, friendID, myLogin, publicA, primeValue, inform);
    }

    public void sendPublicKey(String friendIP, String friendPort, String friendID, String myInf) throws IOException {
        SenderKey senderKey = new SenderKey(friendIP, friendPort, friendID, publicA, myInf);
    }

    public static class SenderKey {
        public SenderKey(String friendIP, String friendPort, String myID, String friendID, String myLogin, BigInteger publicKey,
                         BigInteger primeValue, String inform) throws IOException {
            String hostName = friendIP;
            int portNumber = Integer.parseInt(friendPort);
            Socket fromServer = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(fromServer.getOutputStream(), true);

            String fromUser = inform + "prime" + primeValue.toString();
            fromUser = fromUser + "key" + publicKey.toString();
            out.println(fromUser);
            fromServer.close();
        }
        public SenderKey(String friendIP, String friendPort, String friendID, BigInteger publicKey, String myIng) throws IOException {
            String hostName = friendIP;
            int portNumber = Integer.parseInt(friendPort);
            Socket fromServer = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(fromServer.getOutputStream(), true);
            String fromUser = publicKey.toString();


            if (fromUser != null) {
                out.println("answer" + fromUser);
            }
            fromServer.close();
        }
    }
}