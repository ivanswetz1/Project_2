package org.example.project_2;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Abstract base class for different types of messages
abstract class BaseMessage {
    protected Date date;
    protected String author;

    // Constructor to initialize date and author
    public BaseMessage(String author) {
        this.date = new Date();
        this.author = author;
    }

    abstract String render();
}
// Text message class
class TextMessage extends BaseMessage {
    public String content;

    public TextMessage(String author, String content) {
        super(author);
        this.content = content;
    }

    @Override
    String render() {
        return content;
    }
}
// Image message class
class ImageMessage extends BaseMessage {
    private String imageUrl;

    public ImageMessage(String author, String imageUrl) {
        super(author);
        this.imageUrl = imageUrl;
    }

    @Override
    String render() {
        return "Image: " + imageUrl;
    }
}
// Voice message class
class VoiceMessage extends BaseMessage {
    private String audioUrl;

    public VoiceMessage(String author, String audioUrl) {
        super(author);
        this.audioUrl = audioUrl;
    }

    @Override
    String render() {
        return "Voice message: " + audioUrl;
    }
}
// Class representing a chat conversation
class Chat implements Serializable {
    public String chatName;
    private List<BaseMessage> messages;

    public Chat(String chatName) {
        this.chatName = chatName;
        this.messages = new ArrayList<>();
    }

    public void addMessage(BaseMessage message) {
        messages.add(message);
    }

    public List<BaseMessage> getMessages() {
        return messages;
    }
}

class ChatMessenger implements Serializable {
    private List<Chat> chats;

    public ChatMessenger() {
        this.chats = new ArrayList<>();
    }

    public void addChat(Chat chat) {
        chats.add(chat);
    }

    public List<Chat> getChats() {
        return chats;
    }

    // Method to save all chats to a file
    public void saveChatsToFile() {
        String fileName = "chats_" + System.currentTimeMillis() + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (Chat chat : chats) {
                writer.println("Chat: " + chat.chatName);
                for (BaseMessage message : chat.getMessages()) {
                    writer.println(message.author + ": " + message.render());
                }
                writer.println(); // Add a blank line between chats
            }
            System.out.println("Chats saved to file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
// Main JavaFX application
public class Messanger extends Application {
    private Label chatLabel;
    private TextField messageField;
    private ChoiceBox<String> messageTypeChoice;
    private TextField userNameField;
    private ChatMessenger chatMessenger;
    private ListView<String> usernameListView;
    private boolean isUser1Turn = true;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root, 800, 600);

        Label titleLabel = new Label("TELEKG");
        titleLabel.setFont(Font.font("Times New Roman", 40));
        titleLabel.setTextFill(Color.GREEN);

        chatLabel = new Label();
        chatLabel.setStyle("-fx-alignment: center;");

        userNameField = new TextField();
        userNameField.setPromptText("Username");
        userNameField.setPrefWidth(100);

        usernameListView = new ListView<>();
        userNameField.setOnAction(e -> {
            String username = userNameField.getText();
            if (!username.isEmpty() && !usernameListView.getItems().contains(username)) {
                usernameListView.getItems().add(username);
                userNameField.clear(); // Clear the field after adding the username
            }
        });
        usernameListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                userNameField.setText(newSelection);
            }
        });
        usernameListView.maxHeight(250);


        messageField = new TextField();
        messageField.setPromptText("Type your message here");
        messageField.setPrefWidth(450);
        messageField.setMaxWidth(450);

        messageTypeChoice = new ChoiceBox<>();
        messageTypeChoice.getItems().addAll("Text", "Photo", "Voice Message");
        messageTypeChoice.setValue("Text");

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());

        VBox inputBox = new VBox(messageField,  new HBox(userNameField,usernameListView,messageTypeChoice, sendButton));
        inputBox.setSpacing(10);

        root.getChildren().addAll(titleLabel, chatLabel, inputBox);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Messenger");
        primaryStage.show();

        // Create a new instance of ChatMessenger
        chatMessenger = new ChatMessenger();

        // Add a shutdown hook to save chats when the program is closed
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveChatsToFile));
    }


    private void sendMessage() {
        String messageType = messageTypeChoice.getValue();
        String messageText = messageField.getText();
        BaseMessage message;
        String currentUser = userNameField.getText();
        switch (messageType) {
            case "Text":
                message = new TextMessage(currentUser, messageText);
                addMessageToChat(message);
                break;
            case "Image":
                message = new ImageMessage(currentUser, messageText); // Assuming user enters image URL
                addMessageToChat(message);
                break;
            case "Voice":
                message = new VoiceMessage(currentUser, messageText); // Assuming user enters audio URL
                addMessageToChat(message);
                break;
            default:
                message = new TextMessage(currentUser, messageText);
                addMessageToChat(message);
        }
        messageField.clear();
        isUser1Turn = !isUser1Turn;
    }

    private void addMessageToChat(BaseMessage message) {
        if (chatMessenger.getChats().isEmpty()) {
            chatMessenger.addChat(new Chat("Chat 1"));
        }
        Chat chat = chatMessenger.getChats().get(0); // For simplicity, assuming there's only one chat
        chat.addMessage(message);
        displayChats();
    }

    private void displayChats() {
        StringBuilder chatText = new StringBuilder();
        for (Chat chat : chatMessenger.getChats()) {
            //chatText.append("Chat: ").append(chat.chatName).append("\n");
            for (BaseMessage message : chat.getMessages()) {
                chatText.append(message.author).append(": ").append(message.render()).append("\n");
            }
            chatText.append("\n");
        }
        chatLabel.setText(chatText.toString());
    }

    private void saveChatsToFile() {
        chatMessenger.saveChatsToFile();
    }

    public static void main(String[] args) {
        launch(args);
    }
}