/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package centralserver;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Juan Javier
 */
public class CentralServer extends Application {
    private static int CENTRAL_PORT = 8000;
    private final int UPDATE_MEETINGS = 1;
    private final int SEND_MEETINGS = 2;
    private final int CREATE_MEETING = 3;
    private final int MODIFY_MEETING = 4;
    private final int CONNECTION_ACCEPTED = 5;
    private final int CONNECTION_DECLINED = 6;
    private final int DISCONNECT = 7;
    private static HashMap<String, Integer> employeesMap = new HashMap<>();
    //This list is initialized by reading from all servers when this server is initialized
    private ArrayList<Meeting> allMeetings = new ArrayList<>();
    private ServerSocket server;
    
    @Override
    public void start(Stage primaryStage) {
        TextArea textArea = new TextArea();
        
        StackPane root = new StackPane();
        root.getChildren().add(textArea);
        
        Scene scene = new Scene(root, 300, 250);
        
        primaryStage.setTitle("Central Server");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        try{
            //Read properties file
            FileReader reader = new FileReader("properties.txt");
            BufferedReader buffReader = new BufferedReader(reader);
            while(buffReader.ready()){
                employeesMap.put(buffReader.readLine(), Integer.parseInt(buffReader.readLine()));
            }
            //Retrieve all meetings
            retrieveallMeetings();
            textArea.appendText("Meetings retrieved from available Servers.\n");
        }
        catch(Exception e){
            //Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setHeaderText(e.getMessage());
            alert.showAndWait();
            textArea.appendText("EXCEPTION THROWN\n");
        }
        
        new Thread(() -> {
            try{
                //Server initialization
                server = new ServerSocket(CENTRAL_PORT);
                //Listen for client connections
                Platform.runLater(() -> {
                    textArea.appendText(Calendar.getInstance().getTime().toString() + ": Server Started\n");
                            });
                while(true){
                    new ClientSession(server.accept()).run();
                }   
            }
            catch(Exception e){
                //Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Exception");
                alert.setHeaderText(e.getMessage());
                alert.showAndWait();
                textArea.appendText("EXCEPTION THROWN\n");
            }
        }).start();
    }

    //Utility for checking if employee exists in properties.txt file
    public static boolean employeeExists(String employee){
        return employeesMap.containsKey(employee);
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    //Retrieve all meetings info from all available servers
    private void retrieveallMeetings() {
        for(String employee : employeesMap.keySet()){
            //Connects to an employee server
            try{
                int port = employeesMap.get(employee);
                Socket employeeServerSocket = new Socket("localhost", port);
                DataInputStream fromEmpServer = new DataInputStream(employeeServerSocket.getInputStream());
                DataOutputStream toEmpServer = new DataOutputStream(employeeServerSocket.getOutputStream());
                toEmpServer.writeInt(SEND_MEETINGS);
                String jsonMeetings = fromEmpServer.readUTF();
                //End connection to employee server
                employeeServerSocket.close();
                Gson gson = new Gson();
                if(!jsonMeetings.equals("null")){
                    Meeting[] meetings = gson.fromJson(jsonMeetings, Meeting[].class);
                    for(Meeting m : meetings){
                        if(!allMeetings.contains(m))
                            allMeetings.add(m);
                    }
                }
            }
            catch(Exception e){
                System.out.println("EXCEPTION: "+ e.getMessage() + "\n");
            }
        }
    }
    
    //Clase que maneja sesiones con los clientes
    private class ClientSession implements Runnable {
    
        private Socket client;
        private DataInputStream fromClient;
        private DataOutputStream toClient;

        public ClientSession(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            try {
                fromClient = new DataInputStream(client.getInputStream());
                toClient = new DataOutputStream(client.getOutputStream());
                //Read the employee's name
                String employee = fromClient.readUTF();
                //If this is not an existing employee, disconnect them.
                if(!CentralServer.employeeExists(employee)){
                    toClient.writeInt(CONNECTION_DECLINED);
                    client.close();
                    return;
                }
                else{
                    toClient.writeInt(CONNECTION_ACCEPTED);
                }
                //Listen for client action
                while(true){
                    int action = fromClient.readInt();
                    switch(action){
                        //Initial meetings query for client
                        case SEND_MEETINGS:{
                            sendMeetings(employee);
                            break;
                        }
                        //Create a meeting
                        case CREATE_MEETING:{
                            sendCreation();
                            break;
                        }
                        //Modify a meeting
                        case MODIFY_MEETING:{
                            sendModification();
                            break;
                        }
                        //Disconnect
                        case DISCONNECT:{
                            client = null;
                            return;
                        }
                        default:{
                            client = null;
                            throw new Exception("Unknown response from client: "+employee);
                        }
                    }
                }
            } 
            catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Exception");
                alert.setHeaderText(e.getMessage());
                alert.showAndWait();
            }
        }
        
        //Contact employee server to retrieve their current meetings
        private void sendMeetings(String employee) throws IOException {
            //Connects to an employee server
            Socket employeeServerSocket = new Socket("localhost", employeesMap.get(employee));
            DataInputStream fromEmpServer = new DataInputStream(employeeServerSocket.getInputStream());
            DataOutputStream toEmpServer = new DataOutputStream(employeeServerSocket.getOutputStream());
            toEmpServer.writeInt(SEND_MEETINGS);
            String jsonMeetings = fromEmpServer.readUTF();
            //End connection to employee server
            employeeServerSocket.close();
            if(jsonMeetings.equals("null")){
                Gson gson = new Gson();
                jsonMeetings = gson.toJson(new Meeting[0]);
            }
            toClient.writeUTF(jsonMeetings);
        }

        //Create a meeting and notify all respective servers
        private void sendCreation() throws IOException {
            String jsonMeeting = fromClient.readUTF();
            Gson gson = new Gson();
            Meeting newMeeting = gson.fromJson(jsonMeeting, Meeting.class);
            allMeetings.add(newMeeting);
            notifyServers(newMeeting);
        }

        //Modify a meeting and notify all respective servers
        private void sendModification() throws IOException {
            String jsonOldMeeting = fromClient.readUTF();
            String jsonNewMeeting = fromClient.readUTF();
            Gson gson = new Gson();
            Meeting oldMeeting = gson.fromJson(jsonOldMeeting, Meeting.class);
            Meeting newMeeting = gson.fromJson(jsonNewMeeting, Meeting.class);
            for(int i=0 ; i<allMeetings.size() ; i++){
                Meeting meeting = allMeetings.get(i);
                if(meeting.myEquals(oldMeeting))
                    allMeetings.remove(meeting);
            }
            allMeetings.remove(oldMeeting);
            allMeetings.add(newMeeting);
            notifyServers(newMeeting);
        }

        //Notify all employee servers about the new/updated meeting
        private void notifyServers(Meeting updatedMeeting) throws IOException {
            Gson gson = new Gson();
            //For all employees
            for(String employee : employeesMap.keySet()){
                //Only notify if employee is included in meeting
                if(updatedMeeting.getNameOfInvitees().contains(employee)){
                    Socket employeeServerSocket = new Socket("localhost", employeesMap.get(employee));
                    DataOutputStream toEmpServer = new DataOutputStream(employeeServerSocket.getOutputStream());
                    toEmpServer.writeInt(UPDATE_MEETINGS);
                    ArrayList<Meeting> employeeMeetings = new ArrayList<>();
                    for(Meeting m : allMeetings){
                        if(m.getNameOfInvitees().contains(employee))
                            employeeMeetings.add(m);
                    }
                    String jsonMeetings = gson.toJson(employeeMeetings.toArray());
                    toEmpServer.writeUTF(jsonMeetings);
                    employeeServerSocket.close();
                }
            }
        }
    }
}
