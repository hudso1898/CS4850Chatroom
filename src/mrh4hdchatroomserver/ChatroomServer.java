/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mrh4hdchatroomserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


/**
 *
 * @author hudso
 */
public class ChatroomServer {
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    private static final int MAX_CLIENTS = 3;
    private static final int PORT = 13000;
   
    private ServerSocket server = null;
    private Scanner scanner = new Scanner(System.in);
    private Socket socket     = null;
    private ArrayList<Socket> sockets  = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private HashMap<String,String> passmap = parsePasswd();
    private ArrayList<ServerThread> threads = new ArrayList<>();
    private ServerThread thread;
    
    public ChatroomServer() throws InterruptedException {
        try {
            if (passmap == null) return;
            server = new ServerSocket(PORT);
            System.out.println(ANSI_GREEN + "Server started!" + ANSI_RESET);
            ServerInput si = new ServerInput(scanner);
            si.start();
            server.setSoTimeout(1000);
            while(si.isAlive()) {
                if (ServerData.getCurrentConnections() < MAX_CLIENTS) {
                    
                    try {
                        socket = server.accept();
                    }
                    catch (IOException ex) {} // catch if no input, no need to do anything
                    
                
                    if(socket == null) continue; // only do the following if there's a new socket
                    passmap = parsePasswd(); // update the hashmap if any new entries
                    System.out.println(ANSI_BLUE + "New connection established" + ANSI_RESET);
             
                    thread = new ServerThread(socket,passmap);
                    threads.add(thread);
                    thread.start();
                    socket = null;
                }
                
                Thread.sleep(1000);
                
                
            }
            
            System.out.println(ANSI_GREEN + "Server shutting down!" + ANSI_RESET);
            ServerData.sendAll(ANSI_YELLOW + "<<< SERVER IS SHUTTING DOWN >>>" + ANSI_RESET);
            if (ServerData.getCurrentConnections() > 0) System.out.println(ANSI_YELLOW
                    + "Waiting for clients to close connections..." + ANSI_RESET);
            threads.forEach((thr) -> {
                thr.kill();
            });
           
        } catch (IOException ex) {
            System.out.println("Server is already in use on port " + PORT +". Exiting program");
        }
    }
    
    private HashMap<String,String> parsePasswd() {
  
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get("./passwords.csv"));
        } catch (IOException ex) {
            System.out.println(ANSI_RED + "Error: passwords.csv does not exist!" + ANSI_RESET);
            return null;
        }

        HashMap<String, String> map = new HashMap<>();
        for (String line: lines) {
            String[] strings = line.split(",");
            map.put(strings[0],strings[1]);
        }
  
        return map;
    }
    
    
        public static void main(String[] args) {
        try {
            ChatroomServer chatroomServer = new ChatroomServer();
        } catch (InterruptedException ex) {
            System.out.println("Error in sleep function.");
        }
        }
        
    }
    
   
