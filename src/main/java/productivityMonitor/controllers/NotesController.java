package productivityMonitor.controllers;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import productivityMonitor.MainApp;
import productivityMonitor.utils.User;
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
import javafx.scene.Node;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
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

import static productivityMonitor.utils.TokenManager.*;
import static productivityMonitor.utils.TokenManager.updateUser;
import static productivityMonitor.utils.User.getUser;

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
    private final Path LOCAL_NOTES_PATH = Path.of("src/main/resources/json_files/local_notes.json");
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

    @FXML
    public void initialize() {
        mainImageView.setImage(iconImg);
        updateAuthStatus();
        loadNotes();
        updateNoteListUI();
    }

    private void updateAuthStatus() {
        User user = User.getUser();
        if (user.isUserActive) {
            authStatusLabel.setText("Авторизован как " + user.getUsername());
        } else {
            authStatusLabel.setText("Не авторизован");
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
                            json.getString("update_at"),
                            false
                    ));
                }
            } else {
                showError("Ошибка загрузки заметок: " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка сервера: " + e.getMessage());
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
                            json.getString("update_at"),
                            true
                    ));
                }
            }
        } catch (Exception e) {
            showError("Ошибка загрузки локальных заметок: " + e.getMessage());
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
                    json.put("update_at", note.updateAt);
                    jsonArray.put(json);
                }
            }
            Files.writeString(LOCAL_NOTES_PATH, jsonArray.toString(2));
        } catch (IOException e) {
            showError("Ошибка сохранения локальных заметок: " + e.getMessage());
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
        noteDetailsTitle.setText("Редактировать заметку");
        updateNoteButton.setDisable(false);
        deleteNoteButton.setDisable(false);
    }

    @FXML
    private void handleAddNoteButton(ActionEvent event) {
        String title = noteTitleField.getText().trim();
        String content = noteContentArea.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            showError("Поля заголовка и содержимого обязательны");
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
                        newNote.getString("update_at"),
                        false
                ));
                updateNoteListUI();
            } else {
                showError("Ошибка добавления заметки: " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка сервера: " + e.getMessage());
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
            showError("Выберите заметку для обновления");
            return;
        }
        String title = noteTitleField.getText().trim();
        String content = noteContentArea.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            showError("Поля заголовка и содержимого обязательны");
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
                selectedNote.updateAt = updateNote.getString("update_at");
                updateNoteListUI();
            } else {
                showError("Ошибка обновления заметки: " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка сервера: " + e.getMessage());
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
            showError("Выберите заметку для удаления");
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
                showError("Ошибка удаления заметки: " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка сервера: " + e.getMessage());
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
        noteDetailsTitle.setText("Выберите заметку");
        updateNoteButton.setDisable(true);
        deleteNoteButton.setDisable(true);
    }

    private String getAuthToken() {
        // Замените на реальный способ получения токена
        return getAccessToken();
        //return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjMsImxvZ2luIjoidXNlcjIiLCJpYXQiOjE3NDYwOTMzMDcsImV4cCI6MTc0NjA5NjkwN30.gc6BkS7SUvS8NLbTgoUI_YYBGVIWfeq7loIyQjAJA8s";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Проверка валидности токена
    private boolean isAccessTokenValid() {
        // Реализуйте проверку токена (например, проверка exp)
        // Здесь заглушка, предполагающая, что токен валиден
        return User.getUser().isUserActive;
    }

    // Обновление токена
    private boolean refreshAccessToken() {
        // Реализуйте запрос к /auth/refresh или аналогичному эндпоинту
        // Возвращайте true, если токен успешно обновлен
        return false; // Заглушка
    }

    // Обновление данных пользователя
    private void updateUser() {
        // Реализуйте обновление данных пользователя, если нужно
    }

    // Переход на главную страницу
    @FXML
    private void handleMainImageClick(MouseEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/fxml/mainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/icon.png")));
        stage.setTitle("Productivity Monitor");
        stage.setMinWidth(850);
        stage.setMinHeight(500);

        stage.setOnCloseRequest(e-> {
            Platform.exit();
            System.exit(0);
        });

        stage.setScene(scene);
        stage.show();
    }

    // Переход на профиль
    @FXML
    private void handleProfileButton(ActionEvent event) throws IOException {
        // Проверяем валидность токена и активность пользователя
        if (isAccessTokenValid() && getUser().isUserActive) {
            loadProfileStage(event);
        } else {
            // Пробуем обновить токен, если access-токен невалиден
            if (refreshAccessToken()) {
                updateUser(); // Обновляем данные пользователя
                loadProfileStage(event);
            } else {
                // Если refresh тоже не сработал - показываем окно авторизации
                loadAuthStage(event);
            }
        }
    }

    private void loadAuthStage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/authView.fxml"));
        Parent root = fxmlLoader.load();

        AuthController authController = fxmlLoader.getController();
        authController.setMainStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        authStage=new Stage();
        authController.setThisStage(authStage);
        authStage.setTitle("Authentification");
        authStage.setScene(new Scene(root));
        authStage.setResizable(false);
        authStage.show();
    }

    private void loadProfileStage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/profileView.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // <-- вот ключ
        stage.setScene(new Scene(root));
        stage.setTitle("Profile");
        stage.show();
    }

    // Методы навигации (заглушки)
    @FXML private void handleStatisticsButton(ActionEvent event) {}
    @FXML private void handleSettingsButton(ActionEvent event) {}
    @FXML private void handleAchievementsButton(ActionEvent event) {}
    @FXML private void handleNotesButton(ActionEvent event) {}
    @FXML private void handlePlansButton(ActionEvent event) {}
}