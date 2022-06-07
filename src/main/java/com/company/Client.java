package com.company;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class Client {

    //для отправки данных (broadcast)
    private static DatagramSocket socket = null;
    private static boolean isExit = false;

    //для приема данных (broadcast)
    private static byte[] receiveData = new byte[1024];
    private static int port;// = 4445;//4445
    private static ArrayList<String> listAddr = new ArrayList<String>();

    private static String myLogin;// = "Алиса";
    private static String myID;// = "11111";
    private static String password;// = "12345";
    private static String myPort;// = "6666";
    private static String myIP;

    Sender sender;
    /*
    Diplom3 - Алиса/11111/12345/6666, порт 4445
    Diplom4 - Боб/22222/54321/5555, порт 4446
    Diplom5 - Ева/33333/13452/7777, порт 4447
    */

    static {
        try {
            myIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static String informationAboutMe;

    private static ArrayList<Integer> listOfPort = new ArrayList<Integer>();
    private static ArrayList<String> availableUsers = new ArrayList<String>();
    private static ArrayList<String> listOfFriends = new ArrayList<String>();
    private static ArrayList<UserInformation> listOfUserInformation = new ArrayList<UserInformation>();

    public static BigInteger publicKeyB;

    public Client() throws Exception {
        System.out.println("1 - Войти");
        System.out.println("2 - Зарегистрироваться");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String button = reader.readLine();
        if (button.equals("1"))
        {
            File file = new File("Список друзей.txt");
            if (!file.exists())
            {
                System.out.println("Вам необходимо пройти регистрацию !");
                button = "2";
            }
            else {
                BufferedReader br = new BufferedReader(new FileReader("Мои данные.txt"));
                String line;
                line = br.readLine();
                myID = line;
                line = br.readLine();
                myLogin = line;
                line = br.readLine();
                password = line;

                System.out.println("Введите пароль : ");
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(System.in));

                String pass = "";
                Hasher hasher = new Hasher();
                while(!pass.equals(password)) {

                    pass = DatatypeConverter.printHexBinary(hasher.hash(reader1.readLine()));
                    if(!pass.equals(password))
                    {
                        System.out.println("Введен неверный пароль !");
                    }
                }



                line = br.readLine();
                port = Integer.parseInt(line);
                line = br.readLine();
                myPort = line;
                informationAboutMe = "/" + myIP + "/" + myPort + "/" + myLogin + "/" + myID + "/";
                System.out.println("Вы успешно вошли в аккаунт !");
            }
        }
        if (button.equals("2"))
        {
            File file = new File( "Мои данные.txt");
            if (!file.exists())
                file.createNewFile();

            FileWriter writer = new FileWriter("Мои данные.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            System.out.println("Введите ID: ");
            button = reader.readLine();
            myID = button;
            bufferedWriter.write(myID + "\n");

            System.out.println("Введите логин: ");
            button = reader.readLine();
            myLogin = button;
            bufferedWriter.write(myLogin + "\n");

            System.out.println("Введите пароль: ");
            button = reader.readLine();
            password = button;
            Hasher hasher = new Hasher();
            String pass = DatatypeConverter.printHexBinary(hasher.hash(password));
            bufferedWriter.write(pass + "\n");

            System.out.println("Введите порт (широковещательный): ");
            button = reader.readLine();
            port = Integer.parseInt(button);
            bufferedWriter.write(port + "\n");

            System.out.println("Введите порт (обычный): ");
            button = reader.readLine();
            myPort = button;
            bufferedWriter.write(myPort + "\n");

            bufferedWriter.close();

            informationAboutMe = "/" + myIP + "/" + myPort + "/" + myLogin + "/" + myID + "/";

            System.out.println("Регистрация успешно завершена !");
        }
        else if (!button.equals("1"))
        {
            System.out.println("Ошибка");
            System.exit(-1);
        }

        //Алиса - 4445
        //Боб - 4446
        //Ева - 4447
        listOfPort.add(4445);
        listOfPort.add(4446);
        listOfPort.add(4447);

        File file = new File("Список друзей.txt");
        if (!file.exists())
            file.createNewFile();

        BufferedReader br = new BufferedReader(new FileReader("Список друзей.txt"));
        String line;
        while ((line = br.readLine()) != null)
        {
            listOfFriends.add(line);
        }

        for (int i = 0;i < listOfFriends.size();i++)
        {
            UserInformation inf = new UserInformation(listOfFriends.get(i));
            listOfUserInformation.add(inf);
        }

        broadcast test = new broadcast(informationAboutMe, listAllBroadcastAddresses().get(0));
        test.start();

        broadcastListener test2 = new broadcastListener();
        test2.start();

        Listener listener = new Listener(myPort, listOfUserInformation);
    }

    public void showAvailableUsers()
    {
        if(availableUsers.isEmpty())
        {
            System.out.println("Доступных пользователей нет");
            return;
        }
        System.out.println("Список доступных пользователей: ");
        for(int i = 0;i < availableUsers.size();i++)
        {
            System.out.println(i + 1 + "\t" + availableUsers.get(i));
        }
    }

    public void showFriends()
    {
        if(listOfFriends.isEmpty())
        {
            System.out.println("В списке друзей нет пользователей !");
            return;
        }
        System.out.println("Список друзей: ");
        for(int i = 0;i < listOfFriends.size();i++)
        {
            System.out.println(i + 1 + "\t" + listOfFriends.get(i));
        }
    }

    public String decryptor(String message) {

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
        byte[] bytesForEnc = DatatypeConverter.parseHexBinary(s);
        byte[] key = DatatypeConverter.parseHexBinary(keyTest);
        encryptor = new Cipher(block2);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32_000_000; i++) {
            sb.append('0');
        }
        return new String(encryptor.decrypt(bytesForEnc, key), StandardCharsets.UTF_8);
    }

    public void addFriend() throws Exception {
        showAvailableUsers();
        if(availableUsers.isEmpty()) { return; }

        System.out.print("Чтобы добавить пользователя в список друзей, введите его индекс: ");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        String userIndex = reader.readLine();
        if(Integer.parseInt(userIndex) > 0 || Integer.parseInt(userIndex) <= availableUsers.size()) {
            if (listOfFriends.contains(availableUsers.get(Integer.parseInt(userIndex) - 1))) {
                System.out.println("Этот пользователь уже добавлен в список друзей !");
                return;
            }
            else
            {
                for(int i = 0;i < listOfFriends.size();i++)
                {
                    if(listOfFriends.get(i).equals(availableUsers.get(Integer.parseInt(userIndex) - 1).substring(0,
                            availableUsers.get(Integer.parseInt(userIndex) - 1).lastIndexOf("/") + 1)))
                    {
                        System.out.println("Этот пользователь уже добавлен в список друзей !");
                        return;
                    }
                    else
                    {
                        System.out.println(listOfFriends.get(i));
                        System.out.println(availableUsers.get(Integer.parseInt(userIndex) - 1));
                        System.out.println(listOfFriends.get(i).length() + "\t" + availableUsers.get(Integer.parseInt(userIndex) - 1).length());
                    }
                }
                KeyExchange keyExchange = new KeyExchange(availableUsers.get(Integer.   parseInt(userIndex) - 1),
                        informationAboutMe);
                keyExchange.sendK(informationAboutMe);
                Thread.sleep(2500);
                keyExchange.genSecret(publicKeyB);
                publicKeyB = null;

                listOfFriends.add(availableUsers.get(Integer.   parseInt(userIndex) - 1));

                FileWriter writer = new FileWriter("Список друзей.txt", true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.write(availableUsers.get(Integer.parseInt(userIndex) - 1) + "\n");
                bufferedWriter.close();

                if(listOfUserInformation.size() == 0)
                {
                    UserInformation inf = new UserInformation(listOfFriends.get(0));
                    listOfUserInformation.add(inf);
                }
                else{
                    UserInformation inf = new UserInformation(listOfFriends.get(listOfFriends.size() - 1));
                    listOfUserInformation.add(inf);
                }

                File file = new File(listOfUserInformation.get(listOfUserInformation.size() - 1).IDFriend +
                        ".txt");
                file.createNewFile();
            }
        }
        else
        {
            System.out.println("Введен неверный индекс пользователя !");
            return;
        }
    }

    public void writeMessage() throws Exception {
        if (listOfFriends.isEmpty())
        {
            System.out.println("Вы можете отправлять сообщения только друзьям, а в вашем списке друзей нет пользователей !");
            return;
        }
        System.out.println("Ваш список друзей:");
        for(int i = 0;i < listOfUserInformation.size();i++)
        {
            System.out.println(i + 1 + "\t" + listOfUserInformation.get(i).loginFriend);
        }
        System.out.println("Введите индекс друга, с которым вы хотите начать диалог: ");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        String userFriend = reader.readLine();

        BufferedReader br = new BufferedReader(new FileReader
                (listOfUserInformation.get(Integer.parseInt(userFriend) - 1).IDFriend + ".txt"));
        String line;
        while ((line = br.readLine()) != null)
        {

            System.out.println(decryptor(line));
        }
        sender = new Sender(listOfUserInformation.get(Integer.parseInt(userFriend) - 1).IPFriend,
                listOfUserInformation.get(Integer.parseInt(userFriend) - 1).portFriend, myID,
                listOfUserInformation.get(Integer.parseInt(userFriend) - 1).IDFriend, myLogin, password);
    }

    public static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }
            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }

    public static class broadcast extends Thread
    {
        private String broadcastMessage;
        private InetAddress address;
        public broadcast(String broadcastMessage, InetAddress address)
        {
            this.broadcastMessage = broadcastMessage;
            this.address = address;
        }
        public void run()
        {
            while(true) {
                try {
                    socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    byte[] buffer = broadcastMessage.getBytes();
                    for(int i = 0;i < listOfPort.size();i++) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, listOfPort.get(i));
                        socket.send(packet);
                    }
                    socket.close();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class broadcastListener extends Thread
    {
        private static DatagramSocket serverSocket;
        public broadcastListener() throws SocketException {
            serverSocket = new DatagramSocket(port);
        }
        public void run()
        {
            while (!isExit) {
                try {
                    while (true) {
                        receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        String message = new String(receivePacket.getData());
                        if(!informationAboutMe.equals(message.substring(0, informationAboutMe.length())) &&
                                !availableUsers.contains(message)) {
                            availableUsers.add(message);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static class UserInformation
    {
        public String IPFriend;
        public String portFriend;
        public String loginFriend;
        public String IDFriend;
        public UserInformation(String information)
        {
            int a = 1;
            for(int i = 0;i < information.length();i++)
            {
                if (information.charAt(i) == '/' && i != 0)
                {
                    if(IPFriend == null)
                    {
                        IPFriend = information.substring(a, i);
                    }
                    else if (portFriend == null) {
                        portFriend = information.substring(a, i);
                    } else if (loginFriend == null) {
                        loginFriend = information.substring(a, i);
                    } else if (IDFriend == null) {
                        IDFriend = information.substring(a, i);
                    }
                    a += i - a + 1;
                }
            }
        }
        public void print()
        {
            System.out.println("IP - " + IPFriend);
            System.out.println("порт - " + portFriend);
            System.out.println("логин - " + loginFriend);
            System.out.println("ID - " + IDFriend);
        }
    }

    public static class Listener {
        public Listener(String myPort, ArrayList<Client.UserInformation> listOfUserInformation) throws IOException {

            int portNumber = Integer.parseInt(myPort);
            ServerSocket serverSocket;
            ArrayList<clientThread> users = new ArrayList<>();
            serverSocket = new ServerSocket(portNumber);
            class AcceptThread extends Thread {
                public void run() {
                    while (true) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            clientThread cliThread = new clientThread(clientSocket, listOfUserInformation);
                            synchronized (users)
                            {
                                users.add(cliThread);
                            }
                            cliThread.start();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            }
            AcceptThread at = new AcceptThread();
            at.start();
        }

        static class clientThread extends Thread {
            public static BigInteger publicK;
            Socket socket;
            PrintWriter out;
            BufferedReader in;
            String input;
            boolean newMessage = false;
            ArrayList<Client.UserInformation> list;

            clientThread(Socket s, ArrayList<Client.UserInformation> listOfUserInformation) throws IOException {
                socket = s;
                out = new PrintWriter(s.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                list = listOfUserInformation;
            }

            public void run() {
                try {
                    while (true) {
                        input = in.readLine();
                        newMessage = true;
                        if (input != null) {
                            if (input.contains("prime")) {

                                String tmp = input.substring(0, input.lastIndexOf("/") + 1);

                                listOfFriends.add(tmp);

                                FileWriter writer = new FileWriter("Список друзей.txt", true);
                                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                                bufferedWriter.write(input.substring(0, input.lastIndexOf("/") + 1) + "\n");
                                bufferedWriter.close();

                                DiffieHellman diffieHellman = new DiffieHellman(
                                        new BigInteger(input.substring(input.lastIndexOf("/") + 6,
                                                input.indexOf("key"))),
                                        new BigInteger(input.substring(input.indexOf("y") + 1)));

                                diffieHellman.genSecretKey(input.substring(0, input.lastIndexOf("/") + 1), informationAboutMe);
                            }
                            else if (input.contains("answer"))
                            {
                                Client.publicKeyB = new BigInteger(input.substring(6));
                                DiffieHellman.publicB = new BigInteger(input.substring(6));
                                publicK = new BigInteger(input.substring(6));
                            }
                            else {
                                String temp = decryptor(input.substring(5), input.substring(0, 5));
                                System.out.println(temp);

                                FileWriter writer = new FileWriter(input.substring(0, 5) + ".txt", true);
                                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                                String name = "";
                                for (int i = 0; i < list.size(); i++) {
                                    if (input.substring(0, 5).equals(list.get(i).IDFriend)) {
                                        name = list.get(i).loginFriend;
                                        break;
                                    }
                                }
                                bufferedWriter.write(encrypt(name + "\t" + temp) + "\n");
                                bufferedWriter.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public String decryptor(String message, String IDFriend) throws IOException {
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
                String s = message.substring(64);

                Cipher encryptor = new Cipher();
                byte[] bytesForEnc = DatatypeConverter.parseHexBinary(s);
                byte[] key = DatatypeConverter.parseHexBinary(keyTest);
                encryptor = new Cipher(block2);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 32_000_000; i++) {
                    sb.append('0');
                }

                Hasher hasher = new Hasher();

                if(DatatypeConverter.printHexBinary(hasher.hash(keyTest)).equals(message.substring(0, 64)))
                {
                    return new String(encryptor.decrypt(bytesForEnc, key),
                            StandardCharsets.UTF_8);
                }
                else
                {
                    return "";
                }
            }

            public String encrypt(String message) {

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
    }
}