package Client_Server;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Server extends Application {
    
    TextArea displayArea, statusArea;
    TextField enterField, portNumberTf;
    Button startBut, closeConnectionBut;
    ObjectOutputStream output;
    ObjectInputStream input;
    Socket connection;
    ServerSocket server;
    int counter = 1;
    int portNumber = 8000; // default port number
    boolean filTypeMessage = false;
    
    @Override
    public void start(Stage primaryStage) {        
        VBox root = new VBox();
        
        HBox heading = new HBox();
        heading.setBackground(new Background(new BackgroundFill(Color.web("Black"),null,null)));
        
        Label headingText = new Label("Masresha Host-Server");
        headingText.setTextFill(Color.web("White"));
        headingText.setFont(Font.font(18));
        headingText.setPadding(new Insets(10));
        headingText.setFocusTraversable(true);
        
        portNumberTf = new TextField();
        portNumberTf.setPromptText("Port Number here ...");
        
        startBut = new Button("Start a Server");
        startBut.setPrefWidth(135);
        
        HBox headingFields = new HBox();
        headingFields.getChildren().add(portNumberTf);
        headingFields.getChildren().add(startBut);
        headingFields.setAlignment(Pos.CENTER_RIGHT);
        headingFields.setPadding(new Insets(10));
        headingFields.setSpacing(10);
        
        heading.getChildren().add(headingText);
        heading.getChildren().add(headingFields);
        HBox.setHgrow(headingFields, Priority.ALWAYS);
        
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        
        Scene scene = new Scene(root, 800, 500); 
        
        displayArea = new TextArea();
        displayArea.setPrefHeight(400);
        enterField = new TextField();
        enterField.setEditable(false);
        
        HBox mainArea = new HBox();
        mainArea.setSpacing(10);
        
        VBox messagingArea = new VBox();
        messagingArea.setSpacing(10);
        messagingArea.setPadding(new Insets(0, 50, 0, 0));
        messagingArea.setPrefWidth(700);
        
        displayArea = new TextArea();
        displayArea.setPrefHeight(400);
        displayArea.setPromptText("Your message history goes here ...");
        
        enterField = new TextField();        
        enterField.setEditable(false);
        enterField.setPromptText("Type your message here ... ");
        
        messagingArea.getChildren().add(enterField);
        messagingArea.getChildren().add(displayArea);
        
        VBox statusVb = new VBox();
        statusVb.setSpacing(10);
        
        StackPane closeButSp = new StackPane();
        closeConnectionBut = new Button("Close Connection");
        closeConnectionBut.setPrefWidth(150);
        closeButSp.getChildren().add(closeConnectionBut);
        closeButSp.setAlignment(Pos.CENTER_RIGHT);
        
        statusArea = new TextArea();
        statusArea.setPromptText("Connection Status goes here ...");
        statusArea.setPrefHeight(400);
        
        statusVb.getChildren().add(statusArea);
        statusVb.getChildren().add(closeButSp);
        VBox.setVgrow(closeButSp, Priority.ALWAYS);
        
        mainArea.getChildren().add(messagingArea);
        mainArea.getChildren().add(statusVb); 
        
        HBox footer = new HBox();
        footer.setBackground(new Background(new BackgroundFill(Color.web("Black"),null,null)));
        
        StackPane footerSp = new StackPane();
       
        Label footerText = new Label("By: Masresha");
        footerText.setTextFill(Color.web("White"));
        footerText.setFont(Font.font("Nyala", FontWeight.BOLD, 16));
        footerText.setPadding(new Insets(10));
        footerText.setUnderline(true);
        
        footerSp.getChildren().add(footerText);
        footerSp.setAlignment(Pos.CENTER);
        
        footer.getChildren().add(footerSp);
        footer.setHgrow(footerSp, Priority.ALWAYS);
        
        mainArea.setPadding(new Insets(0, 0, 10, 0));
        
        enterField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    sendData(enterField.getText());
                    enterField.clear();
                }                
            }
        });    
        startBut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               new Thread(() -> {
                    try {
                        runServer();
                    } catch(Exception ex) {}
                }).start();
                    }
        });
        
        closeConnectionBut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    closeConnection();
                } catch (IOException ex) {                    
                }
            }
        });
        root.getChildren().add(heading);
        root.getChildren().add(mainArea); 
        root.getChildren().add(footer);
        
        primaryStage.setTitle("Masresha Host-Server");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();     
        
        
    }
    
    @Override
    public void stop(){
        try{
           closeConnection();
        }catch(IOException ex) {}
        Platform.exit();
    }
    
    public void sendData(String message) {
        try {
            output.writeObject("SERVER >>> " + message);
            output.flush();
            displayMessage("\nSERVER >>> " + message);
        }catch(IOException ex) {
            enterField.appendText("\nError writing object");
        }
    }
    
    public void runServer() {
        try {
            try{
                portNumber = Integer.parseInt(portNumberTf.getText().toString());
                portNumber = portNumber > 0 ? portNumber: 8000;
            } catch(Exception ex) { portNumber = 8000; }
            server = new ServerSocket(portNumber, 500);
            while(true) {
                try {
                    waitForConnection();
                    getStreams();
                    processConnection();
                } catch(EOFException ex) {
                    displayMessage("\nServer Terminated Connection.");
                } finally {
                    closeConnection();
                    ++counter;
                }
            }
        } catch(IOException ex) {} 
    }
    
    private void displayMessage(final String messageToDisplay) {
        Platform.runLater(() -> {
            if(messageToDisplay.trim().equals("1")){
                filTypeMessage = true;
                return;
            }
            if(filTypeMessage == true) {
                try{
                   FileOutputStream outputStream = new FileOutputStream("file.txt");
                    byte[] strToBytes = messageToDisplay.getBytes();
                    outputStream.write(strToBytes);
 
                    outputStream.close(); 
                } catch(Exception ex) {}                
                
                sendData("File recieved successfully!");
                filTypeMessage = false;
            } else {
                displayArea.appendText(messageToDisplay);
            }           
        });
            
        //displayArea.appendText(messageToDisplay);
    }
    
    private void setTextFieldEditable(final boolean editable) {
        Platform.runLater(() 
                -> enterField.setEditable(editable));
        //enterField.setEditable(editable);
    }
    
    private void waitForConnection() throws IOException {
        statusArea.appendText("Waiting for Connection\n\n");
        connection = server.accept();
        statusArea.appendText("Connection " + counter + " recieved from: \n\n"
            + connection.getInetAddress().getHostName() 
            + "\nOn port: " + portNumber
            + "\nOn IP address: " + connection.getInetAddress().getHostAddress() + "\n\n");
    }
    
    private void getStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        displayMessage("\nGot I/O Streams\n");
    }
    
    private void processConnection() throws IOException {        
        String message = "Connection Successful";
        sendData(message);
        setTextFieldEditable(true);
        do {
            try {
                message = (String) input.readObject();
                displayMessage("\n" + message);
            }catch(ClassNotFoundException ex) {
                displayMessage("\nUnknown object type recieved");
            }
        } while(!message.equals("CLIENT >>> TERMINATE"));
    }
    
    private void closeConnection() throws IOException {
        displayMessage("\nTerminating Connection\n");
        setTextFieldEditable(false);
        try {
            output.close();
            input.close();
            connection.close();
            server.close();
        } catch(Exception ex){} 
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
