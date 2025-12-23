import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class AssignWorkerPage extends Application {

    private static final String BOOKINGS_FILE = "data/bookings.txt";
    private static final String ASSIGNED_JOBS_FILE = "data/assigned_jobs.txt";
    private static final String COMPLETED_JOBS_FILE = "data/completed_jobs.txt";

    private ComboBox<String> bookingBox;
    private ComboBox<WorkerTeam> workerBox;
    private ComboBox<String> assignedJobsBox;
    private TextArea outputArea;

    // Data lists
    private ObservableList<String> bookings = FXCollections.observableArrayList();
    private ObservableList<WorkerTeam> workerTeams = FXCollections.observableArrayList();
    private ObservableList<String> assignedJobs = FXCollections.observableArrayList();
    private ObservableList<String> completedJobs = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Assign Worker to Customer Booking");

        loadBookingsFromFile();
        loadWorkerTeams();
        loadAssignedJobsFromFile();
        loadCompletedJobsFromFile();

        Label bookingLabel = new Label("Select Customer Booking:");
        bookingBox = new ComboBox<>(bookings);

        Label workerLabel = new Label("Select Worker:");
        workerBox = new ComboBox<>(workerTeams);

        Button assignButton = new Button("Assign Worker");
        assignButton.setOnAction(e -> assignWorker());
        assignButton.disableProperty().bind(
                bookingBox.valueProperty().isNull().or(workerBox.valueProperty().isNull())
        );

        Label assignedLabel = new Label("Assigned Jobs:");
        assignedJobsBox = new ComboBox<>(assignedJobs);

        Button completeButton = new Button("Mark Completed");
        completeButton.setOnAction(e -> markCompleted());

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(200);

        VBox layout = new VBox(
                10,
                bookingLabel, bookingBox,
                workerLabel, workerBox,
                assignButton,
                assignedLabel, assignedJobsBox,
                completeButton,
                outputArea
        );

        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 15;");

        Scene scene = new Scene(layout, 400, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    // LOAD / SAVE FILE DATA
    private void loadBookingsFromFile() {
        bookings.clear();
        File file = new File(BOOKINGS_FILE);

        if (!file.exists()) {
            System.out.println("Bookings file not found.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    bookings.add(line.trim());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAssignedJobsFromFile() {
        assignedJobs.clear();
        File file = new File(ASSIGNED_JOBS_FILE);

        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    assignedJobs.add(line.trim());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCompletedJobsFromFile() {
        completedJobs.clear();
        File file = new File(COMPLETED_JOBS_FILE);

        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    completedJobs.add(line.trim());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAssignedJobsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ASSIGNED_JOBS_FILE))) {
            for (String job : assignedJobs) {
                writer.write(job);
                writer.newLine();
            }
        } catch (IOException e) {
            showAlert("File Error", "Could not save assigned jobs.");
        }
    }

    private void saveCompletedJobsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COMPLETED_JOBS_FILE))) {
            for (String job : completedJobs) {
                writer.write(job);
                writer.newLine();
            }
        } catch (IOException e) {
            showAlert("File Error", "Could not save completed jobs.");
        }
    }

    // WORKER TEAM LOADING
    private void loadWorkerTeams() {
        workerTeams.clear();
        workerTeams.addAll(
                new WorkerTeam(1, "Team 1"),
                new WorkerTeam(2, "Team 2"),
                new WorkerTeam(3, "Team 3"),
                new WorkerTeam(4, "Team 4")
        );
    }

    // ASSIGN LOGIC
    private void assignWorker() {
        String bookingRecord = bookingBox.getValue();
        WorkerTeam worker = workerBox.getValue();

        // Extract username from booking record
        String numberOrder = extractOrderNumber(bookingRecord);
        String username = extractUsername(bookingRecord);

        if (isDuplicateBooking(numberOrder)) {
            showAlert("Duplicate Booking", "This booking already has an assigned job!");
            return;
        }

        String assignmentRecord = numberOrder + ", " + username + " - Assigned to " + worker.getName();

        assignedJobs.add(assignmentRecord);
        outputArea.appendText("Assigned: " + assignmentRecord + "\n");

        saveAssignedJobsToFile();
        showAlert("Success", "Worker assigned successfully!");
    }

    //Set Job completed
    private void markCompleted() {
        String selectedJob = assignedJobsBox.getValue();
        if (selectedJob == null) {
            showAlert("Error", "Please select a job to mark as completed.");
            return;
        }

        completedJobs.add(selectedJob);
        outputArea.appendText("Completed: " + selectedJob + "\n");

        saveCompletedJobsToFile();
        showAlert("Success", "Job marked as completed.");
    }
    
    // Extracts order number from booking record
    private String extractOrderNumber(String bookingLine) {
        return bookingLine.split(",")[0].trim();
    }
    
    // Extracts username from booking record
    private String extractUsername(String bookingLine) {
        return bookingLine.split(",")[1].trim();
    }
    

    // DUPLICATE DETECTION USING USERNAME
    private boolean isDuplicateBooking(String numberOrder) {
        for (String job : assignedJobs) {
            if (job.startsWith(numberOrder + ",")) {
                return true;
            }
        }
        return false;
    }

    // UTILITY: ALERT BOX
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String args[])
    {
        // launch the application
        launch(args);
    }
}

