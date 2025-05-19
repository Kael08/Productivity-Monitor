package productivityMonitor.controllers;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import productivityMonitor.models.User;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static productivityMonitor.application.MainApp.MainStage;
import static productivityMonitor.controllers.SettingsController.getLang;
import static productivityMonitor.services.StageService.createScene;
import static productivityMonitor.services.TokenManager.*;
import static productivityMonitor.models.User.getUser;
import static productivityMonitor.services.StageService.replaceMainScene;

public class NotesController {

    @FXML private VBox navbarPane;
    @FXML private ImageView mainImageView;
    @FXML private Button profileButton;
    @FXML private Button statisticsButton;
    @FXML private Button settingsButton;
    @FXML private Button achievementsButton;
    @FXML private Button notesButton;
    @FXML private Button plansButton;
    @FXML private HBox notesContent;
    @FXML private VBox notesListPane;
    @FXML private Label notesListTitle;
    @FXML private Label authStatusLabel;
    @FXML private VBox notesList;
    @FXML private Button addNoteButton;
    @FXML private VBox noteDetailsPane;
    @FXML private Label noteDetailsTitle;
    @FXML private TextField noteTitleField;
    @FXML private TextArea noteContentArea;
    @FXML private Button updateNoteButton;
    @FXML private Button deleteNoteButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String API_BASE_URL = "http://localhost:3000";
    private final Path LOCAL_NOTES_PATH = Path.of("src/main/resources/data/local_notes.json");
    private List<Note> notes = new ArrayList<>();
    private Note selectedNote = null;
    private Stage authStage;

    // Модель заметки
    private static class Note {
        int id; // Для серверных заметок
        String title;
        String content;
        String createdAt;
        String updateAt;
        boolean isLocal; // Локальная или серверная заметка

        Note(int id, String title, String content, String createdAt, String updateAt, boolean isLocal) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.createdAt = createdAt;
            this.updateAt = updateAt;
            this.isLocal = isLocal;
        }
    }

    private Image iconImg = new Image(getClass().getResource("/images/icon.png").toExternalForm());

    // ResourceBundle для локализации
    private ResourceBundle bundle;

    // Применение локализации
    private void applyLocalization() {
        MainStage.setTitle(bundle.getString("app.title"));
        profileButton.setText(bundle.getString("profile"));
        statisticsButton.setText(bundle.getString("statistics"));
        settingsButton.setText(bundle.getString("settings"));
        achievementsButton.setText(bundle.getString("achievements"));
        notesButton.setText(bundle.getString("notes"));
        plansButton.setText(bundle.getString("plans"));
        notesListTitle.setText(bundle.getString("notes"));
        authStatusLabel.setText(bundle.getString("notes.notAuthorized"));
        addNoteButton.setText(bundle.getString("notes.addNote"));
        noteDetailsTitle.setText(bundle.getString("notes.selectNote"));
        noteTitleField.setPromptText(bundle.getString("notes.noteTitle"));
        noteContentArea.setPromptText(bundle.getString("notes.noteContent"));
        updateNoteButton.setText(bundle.getString("notes.update"));
        deleteNoteButton.setText(bundle.getString("notes.delete"));
    }

    // Установка локализации
    public void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
    }

    @FXML
    public void initialize() {
        setLocalization(getLang());

        mainImageView.setImage(iconImg);
        notesButton.setDisable(true);
        updateAuthStatus();
        loadNotes();
        updateNoteListUI();
    }

    private void updateAuthStatus() {
        User user = User.getUser();
        if (user.isUserActive) {
            authStatusLabel.setText(bundle.getString("notes.authorized") + " " + user.getUsername());
        } else {
            authStatusLabel.setText(bundle.getString("notes.notAuthorized"));
        }
    }

    private void loadNotes() {
        notes.clear();
        if (User.getUser().isUserActive) {
            loadServerNotes();
        } else {
            loadLocalNotes();
        }
    }

    private void loadServerNotes() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/notes"))
                    .header("Authorization", "Bearer " + getAuthToken())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    notes.add(new Note(
                            json.getInt("id"),
                            json.getString("title"),
                            json.getString("content"),
                            json.getString("created_at"),
                            json.getString("updated_at"),
                            false
                    ));
                }
            } else {
                showError(bundle.getString("notes.errorUploadNotes") + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("notes.errorServer") + e.getMessage());
        }
    }

    private void loadLocalNotes() {
        try {
            if (Files.exists(LOCAL_NOTES_PATH)) {
                String content = Files.readString(LOCAL_NOTES_PATH);
                JSONArray jsonArray = new JSONArray(content);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    notes.add(new Note(
                            json.getInt("id"),
                            json.getString("title"),
                            json.getString("content"),
                            json.getString("created_at"),
                            json.getString("updated_at"),
                            true
                    ));
                }
            }
        } catch (Exception e) {
            showError(bundle.getString("notes.errorUploadLocalNotes") + e.getMessage());
        }
    }

    private void saveLocalNotes() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Note note : notes) {
                if (note.isLocal) {
                    JSONObject json = new JSONObject();
                    json.put("id", note.id);
                    json.put("title", note.title);
                    json.put("content", note.content);
                    json.put("created_at", note.createdAt);
                    json.put("updated_at", note.updateAt);
                    jsonArray.put(json);
                }
            }
            Files.writeString(LOCAL_NOTES_PATH, jsonArray.toString(2));
        } catch (IOException e) {
            showError(bundle.getString("notes.errorSaveLocalNotes") + e.getMessage());
        }
    }

    private void updateNoteListUI() {
        notesList.getChildren().clear();
        for (Note note : notes) {
            Label card = new Label(note.title);
            card.getStyleClass().add("note-card");
            card.setUserData(note);
            card.setOnMouseClicked(this::handleNoteCardClick);
            notesList.getChildren().add(card);
        }
        clearNoteDetails();
    }

    private void handleNoteCardClick(MouseEvent event) {
        Label card = (Label) event.getSource();
        selectedNote = (Note) card.getUserData();
        noteTitleField.setText(selectedNote.title);
        noteContentArea.setText(selectedNote.content);
        noteDetailsTitle.setText(bundle.getString("notes.edit"));
        updateNoteButton.setDisable(false);
        deleteNoteButton.setDisable(false);
    }

    @FXML
    private void handleAddNoteButton(ActionEvent event) {
        String title = noteTitleField.getText().trim();
        String content = noteContentArea.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            showError(bundle.getString("notes.errorHeaderContent"));
            return;
        }

        if (User.getUser().isUserActive) {
            addServerNote(title, content);
        } else {
            addLocalNote(title, content);
        }
        clearNoteDetails();
    }

    private void addServerNote(String title, String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("title", title);
            json.put("content", content);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/notes/add"))
                    .header("Authorization", "Bearer " + getAuthToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                JSONObject newNote = new JSONObject(response.body());
                notes.add(new Note(
                        newNote.getInt("id"),
                        newNote.getString("title"),
                        newNote.getString("content"),
                        newNote.getString("created_at"),
                        newNote.getString("updated_at"),
                        false
                ));
                updateNoteListUI();
            } else {
                showError(bundle.getString("notes.errorAddNote") + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("notes.errorServer") + e.getMessage());
        }
    }

    private void addLocalNote(String title, String content) {
        int newId = notes.stream().mapToInt(n -> n.id).max().orElse(0) + 1;
        String now = ZonedDateTime.now().toString();
        Note note = new Note(newId, title, content, now, now, true);
        notes.add(note);
        saveLocalNotes();
        updateNoteListUI();
    }

    @FXML
    private void handleUpdateNoteButton(ActionEvent event) {
        if (selectedNote == null) {
            showError(bundle.getString("notes.errorSelectNoteForUpdate"));
            return;
        }
        String title = noteTitleField.getText().trim();
        String content = noteContentArea.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            showError(bundle.getString("notes.errorHeaderContent"));
            return;
        }

        if (selectedNote.isLocal) {
            updateLocalNote(title, content);
        } else {
            updateServerNote(title, content);
        }
        clearNoteDetails();
    }

    private void updateServerNote(String title, String content) {
        try {
            JSONObject json = new JSONObject();
            json.put("note_id", selectedNote.id);
            json.put("title", title);
            json.put("content", content);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/notes/update"))
                    .header("Authorization", "Bearer " + getAuthToken())
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject updateNote = new JSONObject(response.body()).getJSONObject("note");
                selectedNote.title = updateNote.getString("title");
                selectedNote.content = updateNote.getString("content");
                selectedNote.updateAt = updateNote.getString("updated_at");
                updateNoteListUI();
            } else {
                showError(bundle.getString("notes.errorUpdateNote") + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("notes.errorServer") + e.getMessage());
        }
    }

    private void updateLocalNote(String title, String content) {
        selectedNote.title = title;
        selectedNote.content = content;
        selectedNote.updateAt = ZonedDateTime.now().toString();
        saveLocalNotes();
        updateNoteListUI();
    }

    @FXML
    private void handleDeleteNoteButton(ActionEvent event) {
        if (selectedNote == null) {
            showError(bundle.getString("notes.errorSelectNoteForDelete"));
            return;
        }
        if (selectedNote.isLocal) {
            deleteLocalNote();
        } else {
            deleteServerNote();
        }
        clearNoteDetails();
    }

    private void deleteServerNote() {
        try {
            JSONObject json = new JSONObject();
            json.put("note_id", selectedNote.id);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/notes/delete"))
                    .header("Authorization", "Bearer " + getAuthToken())
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                notes.remove(selectedNote);
                selectedNote = null;
                updateNoteListUI();
            } else {
                showError(bundle.getString("notes.errorDeleteNote") + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("notes.errorServer") + e.getMessage());
        }
    }

    private void deleteLocalNote() {
        notes.remove(selectedNote);
        selectedNote = null;
        saveLocalNotes();
        updateNoteListUI();
    }

    private void clearNoteDetails() {
        selectedNote = null;
        noteTitleField.clear();
        noteContentArea.clear();
        noteDetailsTitle.setText(bundle.getString("notes.selectNote"));
        updateNoteButton.setDisable(true);
        deleteNoteButton.setDisable(true);
    }

    private String getAuthToken() {
        return getAccessToken();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("plans.error"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isAccessTokenValid() {
        return User.getUser().isUserActive;
    }

    private boolean refreshAccessToken() {
        return false;
    }

    private void updateUser() {
    }

    @FXML
    private void handleMainImageClick(MouseEvent event) throws IOException {
        replaceMainScene("/fxml/mainView.fxml", bundle.getString("app.title"));
    }

    @FXML
    private void handleProfileButton(ActionEvent event) throws IOException {
        if (isAccessTokenValid() && getUser().isUserActive) {
            replaceMainScene("/fxml/profileView.fxml", bundle.getString("profile"));
        } else {
            if (refreshAccessToken()) {
                updateUser();
                replaceMainScene("/fxml/profileView.fxml", bundle.getString("profile"));
            } else {
                if (authStage != null && authStage.isShowing()) {
                    authStage.toFront();
                    return;
                }
                authStage = new Stage();
                createScene("/fxml/authView.fxml", bundle.getString("auth.title"), authStage, false);
            }
        }
    }

    @FXML private void handleStatisticsButton(ActionEvent action) throws IOException {
        replaceMainScene("/fxml/statisticsView.fxml",bundle.getString("statistics"));
    }// Нажатие кнопки статистики
    @FXML private void handleSettingsButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/settingsView.fxml", bundle.getString("settings"));
    }
    @FXML private void handleAchievementsButton(ActionEvent event) {}
    @FXML private void handleNotesButton(ActionEvent event) {}
    @FXML private void handlePlansButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/plansView.fxml", bundle.getString("plans"));
    }
}