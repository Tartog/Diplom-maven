package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        Client client = new Client();
        System.out.println("1 - Доступные пользователи");
        System.out.println("2 - добавить пользователя в список друзей");
        System.out.println("3 - список друзей");
        System.out.println("4 - открыть диалог с другом");
        System.out.println("5 - закрыть диалог с другом");
        String temp;
        while(true)
        {
            temp = reader.readLine();
            switch (temp) {
                case "1":
                    client.showAvailableUsers();
                    break;
                case "2":
                    client.addFriend();
                    break;
                case "3":
                    client.showFriends();
                    break;
                case "4":
                    client.writeMessage();
                    break;
            }
        }
    }
}
