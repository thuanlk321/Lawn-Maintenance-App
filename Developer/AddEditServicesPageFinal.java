import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.geometry.Pos;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.security.auth.callback.TextInputCallback;

import java.util.Optional;

public class AddEditServicesPageFinal extends Application {

    private static final String SERVICES_FILE = "data/services.txt";

    private TextField nameField;
    private TextField priceField;
    private TextArea displayArea;
    private ArrayList<String> services = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Admin Page For Edit Services");

        Button addButton = new Button("Add Service");
        addButton.setOnAction(e -> addService());

        Button editButton = new Button("Edit Service");
        editButton.setOnAction(e -> editService());

        Button deleteButton = new Button("Delete Service");
        deleteButton.setOnAction(e -> deleteService());

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> saveToFile());

        Label nameLabel = new Label("Service Name:");
        nameField = new TextField();

        Label priceLabel = new Label("Service Price ($):");
        priceField = new TextField();

        displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setPrefHeight(250);

        loadFromFile();
        updateDisplay();

        VBox layout = new VBox(10, nameLabel, nameField, priceLabel, priceField,
                addButton, editButton, deleteButton, saveButton, displayArea);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Add new service
    private void addService() {

        String name = nameField.getText().trim();
        String price = priceField.getText().trim();

        if (name.isEmpty() || price.isEmpty()) {
            showAlert("Error", "Please fill in both fields.");
            return;
        }

        String service = name + ", $" + price;
        services.add(service);
        updateDisplay();
        nameField.clear();
        priceField.clear();
    }

    // Edit existing service
    private void editService() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter service number to edit:");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                int index = Integer.parseInt(result.get()) - 1;
                if (index >= 0 && index < services.size()) {
                    String newName = nameField.getText().trim();
                    String newPrice = priceField.getText().trim();
                    if (!newName.isEmpty() && !newPrice.isEmpty()) {
                        services.set(index, newName + ", $" + newPrice);
                        updateDisplay();
                    } else {
                        showAlert("Error", "Please enter new name and price.");
                    }
                } else {
                    showAlert("Error", "Invalid service number.");
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Enter a valid number.");
            }
        }
    }

    // Delete a service
    private void deleteService() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter service number to delete:");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                int index = Integer.parseInt(result.get()) - 1;
                if (index >= 0 && index < services.size()) {
                    services.remove(index);
                    updateDisplay();
                } else {
                    showAlert("Error", "Invalid service number.");
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Enter a valid number.");
            }
        }
    }


    // Display all services in TextArea
    private void updateDisplay() {
        displayArea.clear();
        for (int i = 0; i < services.size(); i++) {
            displayArea.appendText((i + 1) + ". " + services.get(i) + "\n");
        }
    }

    // Save all services to file
    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SERVICES_FILE))) {
            for (String s : services) {
                writer.write(s);
                writer.newLine();
            }
            showAlert("Success", "Services saved successfully!");
        } catch (IOException e) {
            showAlert("Error", "Could not save services.");
            e.printStackTrace();
        }
    }

    // Load all services from file
    private void loadFromFile() {
        File file = new File(SERVICES_FILE);
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    services.add(scanner.nextLine());
                }
            } catch (FileNotFoundException e) {
                showAlert("Error", "Could not load services.");
                e.printStackTrace();
            }
        }
    }

    // Show alert messages
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
