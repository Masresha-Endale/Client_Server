package client_server;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Client extends Application {
    
    TextArea displayArea, statusArea;
    TextField enterField, portNumberTf, serverIpTf; 
    Button startBut, fileBut, sendFileBut, closeConnectionBut;
    ObjectOutputStream output;
    ObjectInputStream input;
    String message = "";
    String chatServerIp = "127.0.0.1"; // default IP address
    int chatServerPort = 8000; // default port 
    Socket client;
    FileChooser fileChooser = new FileChooser();
    File selectedFile = null;
    OutputStream os;
    double fileSize = 0.0;
    
    @Override
    public void start(Stage primaryStage) {      
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        VBox root = new VBox();
        
        HBox heading = new HBox();
        heading.setBackground(new Background(new BackgroundFill(Color.web("Black"),null,null)));
        
        Label headingText = new Label("Masresha Host-Client");
        headingText.setTextFill(Color.web("White"));
        headingText.setFont(Font.font("Nyala", FontWeight.BOLD, 25));
        headingText.setPadding(new Insets(10));
        headingText.setFocusTraversable(true);
        
        portNumberTf = new TextField();
        portNumberTf.setPromptText("Port Number here ...");
        
        serverIpTf = new TextField();
        serverIpTf.setPromptText("Server IP here ...");
        
        startBut = new Button("Connect");
        startBut.setPrefWidth(135);
        
        HBox headingFields = new HBox();
        headingFields.getChildren().add(serverIpTf);
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
//        statusArea.setBackground(new Background(new BackgroundFill(Color.web("#FF8D00"),null,null)));
        
        statusVb.getChildren().add(statusArea);
        statusVb.getChildren().add(closeButSp);
        VBox.setVgrow(closeButSp, Priority.ALWAYS);
        
        mainArea.getChildren().add(messagingArea);
        mainArea.getChildren().add(statusVb);
        
        HBox fileHb = new HBox();
        fileHb.setSpacing(10);
        
        fileBut = new Button("Browse File");
        fileBut.setPrefWidth(500); 
        
        StackPane fileLabeSp = new StackPane();
        Label fileName = new Label("No file is selected.");
        fileName.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
        fileName.setTextFill(Color.web("White"));
        fileLabeSp.getChildren().add(fileName);
        fileLabeSp.setAlignment(Pos.CENTER);
        
        StackPane sbSp = new StackPane();
        sendFileBut = new Button("Send File");
        sendFileBut.setPrefWidth(500);
        sendFileBut.setDisable(true);
        sbSp.getChildren().add(sendFileBut);
        sbSp.setAlignment(Pos.CENTER_RIGHT);

        
        fileHb.getChildren().add(fileBut);
        fileHb.getChildren().add(fileLabeSp);
        fileHb.getChildren().add(sbSp);
        HBox.setHgrow(sbSp, Priority.ALWAYS);
        HBox.setHgrow(fileLabeSp, Priority.ALWAYS);
        
        messagingArea.getChildren().add(fileHb);
        
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
                    sendData(enterField.getText(), 0);
                    enterField.clear();
                }                
            }
        });    
        startBut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               new Thread(() -> {
                    try {
                        runClient();
                    } catch(Exception ex) {}
                }).start();
                    }
        });
        fileBut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                selectedFile = fileChooser.showOpenDialog(primaryStage);
                Path path = Paths.get(selectedFile.getPath());
                fileSize = 0.0;
                try {
                    fileSize = Files.size(path);
                } catch(Exception ex) {}
                fileName.setTextFill(Color.web("Black"));
                fileName.setText("Filename: " 
                        + selectedFile.getName() 
                        + " (" + String.format("%.2f", (fileSize / 1024)) + " kb)");
                if(fileSize != 0.0) {
                    sendFileBut.setDisable(false);
                }
            }
        });
        
        sendFileBut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(selectedFile != null) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(selectedFile));   
                        String st = "", line; 
                        while ((line = br.readLine()) != null) 
                           st += line +  "\n"; 
                        sendData(st, 1);
                      } catch(Exception ex) {}
                    
                }
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
        
        primaryStage.setTitle("Masresha Host-Client");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();     
    }
    
    @Override
    public void stop() throws IOException{
        try{
            closeConnection();
        } catch(Exception ex){}
        Platform.exit();
    }
    
    public void sendData(String message, int type) {
        try {
            if(type == 0) {
                output.writeObject("CLIENT >>> " + message);
            } else if(type == 1) {
                output.writeObject("1");                
            }
            output.flush();
            if(type == 0) {
                displayMessage("\nCLIENT >>> " + message);
            } else if(type == 1) {
                output.writeObject(message);
                output.flush();
            }
            
        }catch(IOException ex) {
            enterField.appendText("\nError writing object");
        }
    }
    
    public void runClient() throws IOException {
        try {
            connectToServer();
            getStreams();
            processConnection();
        } catch(EOFException ex) {
            displayMessage("\nClient terminated connection.");
        } catch(IOException ex) {} finally {
            closeConnection();
        }
    }
    
    private void connectToServer() throws IOException {
        chatServerIp = serverIpTf.getText();
        chatServerPort = Integer.parseInt(portNumberTf.getText());
        
        statusArea.appendText("Attempting to connect to: " 
                + chatServerIp + "\n\n");
        client = new Socket(InetAddress.getByName(chatServerIp), chatServerPort);
        statusArea.appendText("Connected to: \n\n" 
                + client.getInetAddress().getHostName() 
                + "\nIP: " + chatServerIp 
                + "\nPort: " + chatServerPort + "\n\n");
    }
    
    private void displayMessage(final String messageToDisplay) {
        Platform.runLater(()
                -> displayArea.appendText(messageToDisplay));
            
        //displayArea.appendText(messageToDisplay);
    }
    
    private void setTextFieldEditable(final boolean editable) {
        Platform.runLater(() 
                -> enterField.setEditable(editable));
        //enterField.setEditable(editable);
    }
    
    private void getStreams() throws IOException {
        output = new ObjectOutputStream(client.getOutputStream());
        output.flush();
        input = new ObjectInputStream(client.getInputStream());
        displayMessage("\nGot I/O Streams\n");
    }
    
    private void processConnection() throws IOException {
        setTextFieldEditable(true);
        do {
            try {
                message = (String) input.readObject();
                displayMessage("\n" + message);
            }catch(ClassNotFoundException ex) {
                displayMessage("\nUnknown object type recieved");
            }
        } while(!message.equals("SERVER >>> TERMINATE"));
    }
    
    private void closeConnection() throws IOException {
        displayMessage("\nClosing Connection\n");
        setTextFieldEditable(false);
        try {
            output.close();
            input.close();
            client.close();
        } catch(Exception ex) {
//            ex.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
