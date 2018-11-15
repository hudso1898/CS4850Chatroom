/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mrh4hdchatroomserver;

import java.util.Scanner;

/**
 *
 * @author hudso
 */
public class ServerInput extends Thread {
    
    private Scanner scanner;
    private String input;
    ServerInput(Scanner scanner) {
        this.scanner = scanner;
    }
    @Override
    public void run() {
        while(true) {
            input = scanner.nextLine();
            if (input.equals(":q")) break;
            else if (input.equals(":i")) {
                System.out.println("Currently connected users   -> " + ServerData.getUsers());
                System.out.println("Currently connected sockets -> " + ServerData.getSockets());
                System.out.println("There are " + ServerData.getCurrentConnections()
                + " current connections.");
            }
        }
     
    }
}
