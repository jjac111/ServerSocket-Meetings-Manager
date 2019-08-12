/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package employeeserver;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javafx.application.Application;
import static javafx.application.Application.launch;
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
public class EmployeeServer extends Application {
    private static int CENTRAL_PORT = 8000;
    private int EMPLOYEE_SERVER_PORT;
    private final int UPDATE_MEETINGS = 1;
    private final int SEND_MEETINGS = 2;
    private Meeting[] meetings;
    private ServerSocket server;
    
    @Override
    public void start(Stage primaryStage) {
        TextArea textArea = new TextArea();
        
        StackPane root = new StackPane();
        root.getChildren().add(textArea);
        
        Scene scene = new Scene(root, 300, 250);
        
        primaryStage.setTitle("Employee Server");
        primaryStage.setScene(scene);
        primaryStage.show();
        //Read meetings & port file
        try{
            BufferedReader reader = new BufferedReader(new FileReader("port.txt"));
            EMPLOYEE_SERVER_PORT = Integer.parseInt(reader.readLine());
            
            reader = new BufferedReader(new FileReader("meetings.txt"));
            String jsonMeetings = reader.readLine();
            Gson gson = new Gson();
            meetings = gson.fromJson(jsonMeetings, Meeting[].class);
        }
        catch(Exception e){
            //Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setHeaderText(e.getMessage());
            alert.showAndWait();
            textArea.appendText("\nEXCEPTION THROWN");
        }
        
        new Thread(() -> {
            try{
                //Server initialization
                server = new ServerSocket(EMPLOYEE_SERVER_PORT);
                Platform.runLater(() -> {
                    textArea.appendText(Calendar.getInstance().getTime().toString() + ": Server Started at port: " + EMPLOYEE_SERVER_PORT + "\n");
                            });
                //Listen for central server connection
                while(true){
                    Socket centralServerSocket = server.accept();
                    DataInputStream fromCentralServer = new DataInputStream(centralServerSocket.getInputStream());
                    //Receives code for what the Central Server wants from this server
                    int action = fromCentralServer.readInt();
                    switch(action){
                        //Central server notifies of a change in the meetings of this server's employee
                        case UPDATE_MEETINGS:{
                            updateMeetings(centralServerSocket);
                            Platform.runLater(() -> {
                                textArea.appendText(Calendar.getInstance().getTime().toString() + ": Updated Meetings File\n");
                            });
                            break;
                        }
                        //Central Server asks for the employee's meetings
                        case SEND_MEETINGS:{
                            sendMeetings(centralServerSocket);
                            Platform.runLater(() -> {
                                textArea.appendText(Calendar.getInstance().getTime().toString() + ": Sent Meetings to Central Server\n");
                            });
                            break;
                        }
                        default:{
                            centralServerSocket.close();
                            throw new Exception("Unknown response from Central Server");
                        }
                    }
                    //End central server connection after that
                    centralServerSocket.close();
                }   
            }
            catch(Exception e){
                //Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Exception");
                alert.setHeaderText(e.getMessage());
                alert.showAndWait();
                Platform.runLater(() -> {
                    textArea.appendText("EXCEPTION THROWN\n");
                });
            }
        }).start();
    }

    
    
    public static void main(String[] args) {
        launch(args);
    }
    
    //Read Meetings (JSON) sent from Central Server, called when an employee creates/modifies a meeting
    private void updateMeetings(Socket centralServerSocket) throws IOException {
        DataInputStream fromCentralServer = new DataInputStream(centralServerSocket.getInputStream());
        String jsonMeetings = fromCentralServer.readUTF();
        //Parse JSON 
        Gson gson = new Gson();
        meetings = gson.fromJson(jsonMeetings, Meeting[].class);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter("meetings.txt", false));
            writer.write(jsonMeetings);
            writer.flush();
        }
        catch(Exception e){
            //Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Exception");
                alert.setHeaderText(e.getMessage());
                alert.showAndWait();
        }
        
    }
    //Send Meetings to Central Server, called when Central Server welcomes a connected employee
    private void sendMeetings(Socket centralServerSocket) throws IOException {
        DataOutputStream toCentralServer = new DataOutputStream(centralServerSocket.getOutputStream());
        //Convert Meetings to JSON
        Gson gson = new Gson();
        String jsonMeetings = gson.toJson(meetings);
        toCentralServer.writeUTF(jsonMeetings);
    }
    
    
}
