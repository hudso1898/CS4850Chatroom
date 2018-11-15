/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mrh4hdchatroomserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mrh4hdchatroomserver.ChatroomServer.ANSI_RESET;
import static mrh4hdchatroomserver.ChatroomServer.ANSI_YELLOW;

/**
 *
 * @author hudso
 */
public class ServerData {
    private static HashMap<String,Socket> users = new HashMap<>();
    private static HashMap<Socket,String> sockets = new HashMap<>();
    private static HashMap<String,Socket> usersToSockets = new HashMap<>();
    private static DataOutputStream out;
    private static int currentConnections = 0;
    
    public static void addSocket(Socket s) {
        sockets.put(s,null);
        currentConnections++;
    }
    public static void removeSocket(Socket s) {
        sockets.remove(s);
        currentConnections--;
    }
    public static void sendAll(String s) {
        sockets.keySet().forEach((socket) -> {
            try {
                out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(s);
            } catch (IOException ex) {
                Logger.getLogger(ServerData.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    public static int send(String s, String username, String srcUser) {
        if(sockets.containsValue(username)) {
            try {
                out = new DataOutputStream(users.get(username).getOutputStream());
                out.writeUTF(ANSI_YELLOW + "<" + srcUser + "> => <" + username + "> "
                        + ANSI_RESET+ s);
                
                
                return 1;
            } catch (IOException ex) {
                Logger.getLogger(ServerData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return 0;
    }
    public static void addUser(String s,Socket socket) {
        users.put(s,socket);
        sockets.replace(socket, s);
    }
    public static void removeUser(String s,Socket socket) {
        users.remove(s,socket);
        sockets.replace(socket,null);
    }
    
    public static String getUsers() {
        return users.keySet().toString();
    }
    public static String getSockets() {
        return sockets.keySet().toString();
    }
    
    public static boolean isLoggedIn(String username) {
        return sockets.containsValue(username);
    }
    public static int getCurrentConnections() {
        return currentConnections;
    }
    
}
        

