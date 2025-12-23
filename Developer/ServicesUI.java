import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public class ServicesUI extends Application {

    // UI elements
    private ComboBox<String> servicesComboBox;
    private Label servicesLabel;
    private Button submitOrderButton;
    private Button signupButton;
    private Button clearButton;
    private Button historyButton;
    private Button sortButton;
    private Text underText;
    private Text orderSummaryText;
    private TextArea selectedServicesArea;
    private DatePicker datePicker;
    private String currentUser = null;
    private String currentRole = "USER";  // USER or ADMIN
    private String bookingDate = "";

    // Load services.txt
    private List<String> loadServicesFromFile() {
        String SERVICES_FILE = "data/services.txt";
        return FileStorage.readLines(SERVICES_FILE);
    }

    private ArrayList<String> selectedServices = new ArrayList<>();

    private Scene servicesScene;

    // USER STORAGE (TEXT FILE)
    private HashMap<String, String> userDatabase = new HashMap<>();
    private static final String BOOKINGS_FILE = "data/bookings.txt";
    private static final String USER_FILE = "data/users.txt";
    private static final String SERVICES_FILE = "data/services.txt";

    // Load users
    private void loadUsersFromFile() {
        List<String> lines = FileStorage.readLines(USER_FILE);
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 2) {
                userDatabase.put(parts[0].trim(), parts[1].trim());
            }
        }
    }

    // Save users
    private void saveUsersToFile() {
        List<String> lines = new ArrayList<>();
        for (String user : userDatabase.keySet()) {
            lines.add(user + "," + userDatabase.get(user));
        }
        FileStorage.writeLines(USER_FILE, lines);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Services Page");

        underText = new Text("Welcome to the Lawn Maintenance App");

        signupButton = new Button("Signup/Login");
        signupButton.setOnAction(event -> openSignupPage(primaryStage));

        // Services dropdown
        servicesComboBox = new ComboBox<>();
        List<String> servicesList = loadServicesFromFile();
        
        servicesComboBox.getItems().addAll(servicesList);

        servicesComboBox.setOnAction(event -> processServiceSelection(servicesComboBox.getValue()));

        servicesLabel = new Label("Select a Service:");

        // Selected services display
        selectedServicesArea = new TextArea();
        selectedServicesArea.setEditable(false);
        selectedServicesArea.setPromptText("Your selected services will appear here.");
        selectedServicesArea.setPrefHeight(100);

        // Submit order
        submitOrderButton = new Button("Submit Order");
        submitOrderButton.setOnAction(this::processSubmitOrder);

        // Clear
        clearButton = new Button("Clear");
        clearButton.setOnAction(event -> clearSelection());

        // User History
        historyButton = new Button("Booking History");
        historyButton.setOnAction(this::processHistorySelection);
	
        orderSummaryText = new Text();
        
        //Calendar for customers to book the date
        datePicker = new DatePicker();
        datePicker.setOnAction(this::processDateSelection);
        
        // Layout
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setPadding(new Insets(20));

        vbox.getChildren().addAll(
                underText,
                signupButton,
                servicesLabel,
                servicesComboBox,
                datePicker,
                selectedServicesArea,
                submitOrderButton,
                historyButton,
                clearButton,
                orderSummaryText
        );

        servicesScene = new Scene(vbox, 400, 600);
        primaryStage.setScene(servicesScene);
        primaryStage.show();
    }
    
    //Datetime Picker
    private void processDateSelection(ActionEvent event){
        LocalDate chosenDate = datePicker.getValue();
        LocalDate currentDate = LocalDate.now();
        int chosenDateYear = Integer.valueOf(chosenDate.toString().substring(0,4));
        int currentDateYear = Integer.valueOf(currentDate.toString().substring(0,4));
        
        if(chosenDateYear > (currentDateYear + 2)){
            showAlert("Invalid Date Booking", "Chosen date is outside of our booking range. Please choose another date");
            return;
        }
        if(chosenDate.compareTo(currentDate) < 0){
            showAlert("Invalid Date Booking", "Improper date chosen. Please choose another date");
            return;
        }
        
        bookingDate = chosenDate.toString();
    }
    
    //Check DropBox
    private void processServiceSelection(String input){
        boolean exists = false;

        for (String s : selectedServices){
            if (s.equals(input)){
                exists = true;
                break;
            }
        }
        if (!exists){
            selectedServices.add(input);
        }
        
        StringBuilder servicesText = new StringBuilder();
        for (String service : selectedServices) {
            servicesText.append(service).append("\n");
        }
        selectedServicesArea.setText(servicesText.toString() + (exists ? "\n\n Please only select unique services!" : ""));
    }

    // FIXED FINAL VERSION
    private void processSubmitOrder(ActionEvent event){
        if(currentUser == null){
            clearSelection();
            selectedServicesArea.setText("Please log in before submitting an order.");
            return;
        }
        
        if(bookingDate.equals("")){
            selectedServicesArea.setText("Please select a date before submitting an order.");
            return;
        }
        
        int orderNum = initializeOrderNum();
        String selected = "";
        double total = 0;
        Scanner scan = null;
        PrintWriter writer = null;
        NumberFormat fmt = NumberFormat.getCurrencyInstance();
	   
        for (String s : selectedServices) {
            selected += s + "\n";

            scan = new Scanner(s);
            scan.useDelimiter("[;\\$]");
            scan.next();
            scan.useDelimiter("\n");
            total += Integer.valueOf(scan.next().substring(1).trim());
        }

        String bill = "Bill for your order:\n\n" + selected + "\nOrder total: " + fmt.format(total);

        orderSummaryText.setText(bill);

        // Store the order 
        try{
            writer = new PrintWriter(new FileWriter(BOOKINGS_FILE, true));
            
            String orderData = orderNum++ + "," + currentUser + ",";
            
            for (String s : selectedServices){
                scan = new Scanner(s);
                scan.useDelimiter(",");
                String nextService = scan.next();
                orderData += nextService + ",";
            }
            LocalDate processedDate = LocalDate.now();
            orderData += total + "," + bookingDate + ", " + processedDate.toString();
            
            writer.println(orderData);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            // check if the writer still has contents in it
            if (writer != null){
                writer.close();
            }
        }

        selectedServices.clear();
    }
    
    //Booking History
    private void processHistorySelection(ActionEvent event){
        if(currentUser == null){
            clearSelection();
            selectedServicesArea.setText("Please log in to view order history.");
            return;
        }
        else{
            Scanner bookingsScan;
            StringBuilder bookingsCollection = new StringBuilder();
            
            try{
                bookingsScan = new Scanner(new File(BOOKINGS_FILE));
                String currentLine;
                while(bookingsScan.hasNextLine()){
                    currentLine = bookingsScan.nextLine();
                    Scanner currScan = new Scanner(currentLine);
                    currScan.useDelimiter(",");
                    currScan.next();
                    String currentLineUser = currScan.next().trim();
                    
                    if(currentLineUser.equals(currentUser)){
                        bookingsCollection.append(currentLine).append("\n");
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            
            if(bookingsCollection.length() == 0){
                selectedServicesArea.setText("No Booking History found for user " + currentUser + ".");
            }
            else{
                selectedServicesArea.setText("Booking History for " + currentUser + ":\n\n" + bookingsCollection.toString());
            }
        }
    }

    //Clear Button
    private void clearSelection() {
        selectedServices.clear();
        selectedServicesArea.clear();
        orderSummaryText.setText("");
    }

    // SIGNUP + LOGIN PAGE
    private void openSignupPage(Stage primaryStage) {

        loadUsersFromFile();

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        Button signupButton = new Button("Signup");
        Button clearButton = new Button("Clear");
        Button backButton = new Button("Back");
        

        Text messageText = new Text("Please enter your details below:");

        // LOGIN
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            // ADMIN LOGIN
            if (username.equals("admin") && password.equals("admin123")) {
                currentUser = "admin";
                currentRole = "ADMIN";
                messageText.setText("Admin login successful!");
                openAdminHome(primaryStage);
                return;
            }

            // NORMAL USER
            if (userDatabase.containsKey(username) &&
                    userDatabase.get(username).equals(password)) {

                currentUser = username;
                currentRole = "USER";
                messageText.setText("Login successful!");
                primaryStage.setScene(servicesScene);
            } else {
                messageText.setText("Invalid username or password.");
            }
        });

        // SIGNUP
        signupButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Access Denied", "Username and password cannot be empty.");
                return;
            }
            if (userDatabase.containsKey(username)) {
                showAlert("Access Denied", "Username already exists!");
                return;
            }

            userDatabase.put(username, password);
            saveUsersToFile();

            messageText.setText("Signup successful!");
        });

        clearButton.setOnAction(e -> {
            usernameField.clear();
            passwordField.clear();
            messageText.setText("Fields cleared.");
        });

        backButton.setOnAction(e -> {
            primaryStage.setScene(servicesScene);
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);

        grid.add(messageText, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);

        HBox buttonBox = new HBox(10, loginButton, signupButton, clearButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        grid.add(buttonBox, 0, 3, 2, 1);

        Scene signupScene = new Scene(grid, 400, 250);
        primaryStage.setScene(signupScene);
    }

    private void openAdminHome(Stage primaryStage) {
        VBox adminBox = new VBox(15);
        adminBox.setAlignment(Pos.CENTER);
        adminBox.setPadding(new Insets(20));

        Label adminLabel = new Label("ADMIN HOME");

        Button editServicesBtn = new Button("Add / Edit Services");
        editServicesBtn.setOnAction(e -> {
            try {
                new AddEditServicesPageFinal().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button assignWorkerBtn = new Button("Assign Worker to Job");
        assignWorkerBtn.setOnAction(e -> {
            try {
                new AssignWorkerPage().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            currentUser = null;
            currentRole = "USER";
            primaryStage.setScene(servicesScene);
        });

        adminBox.getChildren().addAll(
                adminLabel,
                editServicesBtn,
                assignWorkerBtn,
                logoutBtn
        );

        Scene adminScene = new Scene(adminBox, 400, 300);
        primaryStage.setScene(adminScene);
    }
    
    private int initializeOrderNum(){
        Scanner bookingsScan;
        int result = 0;
        try{
            bookingsScan = new Scanner(new File(BOOKINGS_FILE));
            
            if(bookingsScan.hasNextLine() == false){
                result = 1;
            }
            else {
                String lastLine = null;
                while(bookingsScan.hasNextLine()){
                    lastLine = bookingsScan.nextLine();
                }
                bookingsScan.close();
                bookingsScan = new Scanner(lastLine);
                bookingsScan.useDelimiter(",");
                result = Integer.valueOf(bookingsScan.next()) + 1;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        return result;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

