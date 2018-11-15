/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mrh4hdchatroomserver;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import static mrh4hdchatroomserver.ChatroomServer.ANSI_BLUE;
import static mrh4hdchatroomserver.ChatroomServer.ANSI_GREEN;
import static mrh4hdchatroomserver.ChatroomServer.ANSI_RED;
import static mrh4hdchatroomserver.ChatroomServer.ANSI_RESET;
import static mrh4hdchatroomserver.ChatroomServer.ANSI_YELLOW;

/**
 *
 * @author hudso
 */
public class ServerThread extends Thread {
    
    private Socket socket;
    private HashMap<String,String> passmap;
    private DataInputStream in;
    private DataOutputStream out;
    private String[] inputbuf;
    private String username;
    private String password;
    private String testpwd;
    private String newUsername;
    private String newPasswrd;
    private String destUser;
    private boolean loggedin = false;
    private boolean isAlive  = true;
    
    ServerThread(Socket socket,HashMap<String,String> hashmap) {
        this.socket = socket;
        this.passmap = hashmap;
    }
    @Override
    public void run() {
        try {
            ServerData.addSocket(socket);
            in  = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
            
            out.writeUTF(ANSI_GREEN + "Connected!" + ANSI_RESET);
            out.writeUTF("Login to access chat. Type \"help\" for options.");
            String input = "";
            while (!input.equals("logout") && isAlive) {
                input = in.readUTF();
                if (!isAlive) break;
                if (input.startsWith("login")) {
                    if(loggedin) {
                        out.writeUTF(ANSI_RED + "You are already logged in!" + ANSI_RESET);
                        continue;
                    }
                    inputbuf = input.split(" ");
                    if (inputbuf.length == 3) {
                        username = inputbuf[1];
                        password = inputbuf[2];
                        if (passmap.containsKey(username)) {
                            testpwd = passmap.get(username);
                            if (testpwd.equals(password)) {
                                if(ServerData.isLoggedIn(username) == false) {
                                    System.out.println(ANSI_GREEN + username + " logged in." + ANSI_RESET);
                                    loggedin = true;
                                    ServerData.addUser(username,socket);
                                    out.writeUTF(ANSI_GREEN + "Logged in!" + ANSI_RESET);
                                    ServerData.sendAll(ANSI_BLUE + "<" + username + "> entered the chatroom" + ANSI_RESET);
                                }
                                else out.writeUTF(ANSI_RED + username + " is already logged in!" + ANSI_RESET);
                                    
                            }
                            else {
                                out.writeUTF(ANSI_RED + "Login failed: incorrect username or password." + ANSI_RESET);
                                username = null;
                            }
                        }
                        else {
                            out.writeUTF(ANSI_RED + "Login failed: user not found! Try using \"newuser\"" + ANSI_RESET);
                            username = null;
                        }
                    }
                    
                }
                else if (input.startsWith("newuser")) {
                    inputbuf = input.split(" ");
                    if (inputbuf.length == 3) {
                        newUsername = inputbuf[1];
                        newPasswrd  = inputbuf[2];
                        if(passmap.get(newUsername) != null) {
                            out.writeUTF(ANSI_RED + "The username " + newUsername + " already exists!" + ANSI_RESET);
                            continue;
                        }
                        passmap.put(newUsername, newPasswrd); // adds new username and password into acting memory
                        addEntry(newUsername,newPasswrd);
                        username = newUsername;
                        loggedin = true;
                        ServerData.addUser(username,socket);
                        System.out.println(ANSI_GREEN + username + " logged in." + ANSI_RESET);
                        out.writeUTF(ANSI_GREEN + "Logged in!" + ANSI_RESET);
                        out.writeUTF(ANSI_BLUE + "<" + username + "> entered the chatroom" + ANSI_RESET);
                        
                    }
                }
                else if (input.equals("logout")) {
                    if (username == null) System.out.println(ANSI_BLUE + socket + " is logging out." + ANSI_RESET);
                    else {
                        ServerData.removeUser(username,socket);
                        System.out.println(ANSI_BLUE + username + " is logging out." + ANSI_RESET);
                    }
                    break;
                }
                else if (input.equals("who")) {
                    out.writeUTF("Users are: " + ServerData.getUsers());
                }
                else if (input.startsWith("all")) {
                    if(loggedin) {
                        System.out.println(ANSI_BLUE + "<" + username + ">" + ANSI_RESET + input.substring(3));
                        ServerData.sendAll(ANSI_BLUE + "<" + username + ">" + ANSI_RESET + input.substring(3));
                    }
                    else out.writeUTF(ANSI_YELLOW + "You must log in first!" + ANSI_RESET);
                    
                }
                else if (input.startsWith("unicast")) {
                    inputbuf = input.split(" ");
                    if (inputbuf.length >= 3) {
                        destUser = inputbuf[1];
                        input = "";
                        for (int i = 2; i < inputbuf.length; i++) {
                            input += inputbuf[i] + " ";
                        }
                    
                        if(loggedin) {
                            
                            int sent = ServerData.send(input, destUser,username);
                            if (sent == 0) {
                                out.writeUTF(destUser + " not found!");
                            }
                            else if (!username.equals(destUser)){
                                out.writeUTF(ANSI_YELLOW + "<" + username + "> => <" + destUser +
                                    "> " + ANSI_RESET + input);
                                System.out.println(ANSI_YELLOW + "<" + username 
                                        + "> => <" + destUser + "> "
                                        + ANSI_RESET + input);
                                
                            }  
                            else System.out.println(ANSI_YELLOW + "<" + username
                                    + "> => <" + destUser + "> " 
                                    + ANSI_RESET+ input);
                        }
                        else out.writeUTF(ANSI_YELLOW + "You must log in first!" + ANSI_RESET);
                    }
                }
                else out.writeUTF(ANSI_YELLOW + "You must log in first!" + ANSI_RESET);
            }
            
            
            System.out.print("Closing connection => ");
            ServerData.removeSocket(socket);
            socket.close();
            
            in.close();
            System.out.println(ANSI_GREEN + "closed" + ANSI_RESET);
            if (username != null) ServerData.sendAll(ANSI_BLUE + "<" + username
                    + "> " + ANSI_RESET + "has left the room.");
            out.close();
 
        } catch (IOException ex) {
            System.out.println(ANSI_YELLOW + "Connection terminated abruptly." + ANSI_RESET);
            ServerData.removeSocket(socket);
        }
            }
    
    private void addEntry(String u , String p) {
        try {
            Files.write(Paths.get("./passwords.csv"),(u + "," + p + "\n").getBytes(), StandardOpenOption.APPEND);
    }   catch (IOException e) {
        System.err.println("Error: " + e);
    }
    }
    
    public void kill() {
        isAlive = false;
    }
}


