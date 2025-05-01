package productivityMonitor.controllers;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import productivityMonitor.MainApp;
import productivityMonitor.utils.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static productivityMonitor.utils.TokenManager.*;

public class PlansController {

    @FXML private VBox navbarPane;
    @FXML private ImageView mainImageView;
    @FXML private Button profileButton;
    @FXML private Button statisticsButton;
    @FXML private Button settingsButton;
    @FXML private Button achievementsButton;
    @FXML private Button notesButton;
    @FXML private Button plansButton;
    @FXML private HBox plansContent;
    @FXML private VBox listsPane;
    @FXML private Label listsTitle;
    @FXML private Label authStatusLabel;
    @FXML private VBox listsContainer;
    @FXML private TextField listTitleField;
    @FXML private Button addListButton;
    @FXML private VBox itemsPane;
    @FXML private Label itemsTitle;
    @FXML private TableView<TodoItem> itemsTable;
    @FXML private TableColumn<TodoItem, Boolean> completedColumn;
    @FXML private TableColumn<TodoItem, String> descriptionColumn;
    @FXML private TableColumn<TodoItem, String> priorityColumn;
    @FXML private TextField itemDescriptionField;
    @FXML private TextField itemPriorityField;
    @FXML private Button addItemButton;
    @FXML private Button updateItemButton;
    @FXML private Button deleteItemButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String API_BASE_URL = "http://localhost:3000";
    private final Path LOCAL_LISTS_PATH = Path.of("src/main/resources/json_files/local_todo_lists.json");
    private List<TodoList> todoLists = new ArrayList<>();
    private TodoList selectedList = null;
    private TodoItem selectedItem = null;
    private Stage authStage;
    private Image iconImg = new Image(getClass().getResource("/images/icon.png").toExternalForm());

    // Модель to-do листа
    private static class TodoList {
        int id;
        String title;
        String createdAt;
        String updatedAt;
        List<TodoItem> items;
        boolean isLocal;

        TodoList(int id, String title, String createdAt, String updatedAt, List<TodoItem> items, boolean isLocal) {
            this.id = id;
            this.title = title;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.items = items;
            this.isLocal = isLocal;
        }
    }

    // Модель задачи
    private static class TodoItem {
        int id;
        int todoListId;
        String description;
        BooleanProperty isCompleted;
        int priority;
        String createdAt;
        String updatedAt;

        TodoItem(int id, int todoListId, String description, boolean isCompleted, int priority,
                 String createdAt, String updatedAt) {
            this.id = id;
            this.todoListId = todoListId;
            this.description = description;
            this.isCompleted = new SimpleBooleanProperty(isCompleted);
            this.priority = priority;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public BooleanProperty isCompletedProperty() {
            return isCompleted;
        }

        public StringProperty descriptionProperty() {
            return new SimpleStringProperty(description);
        }

        public StringProperty priorityProperty() {
            return new SimpleStringProperty(String.valueOf(priority));
        }
    }

    @FXML
    public void initialize() {
        mainImageView.setImage(iconImg);
        plansButton.setDisable(true);
        setupTableColumns();
        updateAuthStatus();
        loadTodoLists();
        updateListsUI();
    }

    private void setupTableColumns() {
        completedColumn.setCellValueFactory(cellData -> cellData.getValue().isCompletedProperty());
        completedColumn.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    checkBox.setOnAction(e -> {
                        TodoItem todoItem = getTableView().getItems().get(getIndex());
                        todoItem.isCompleted.set(checkBox.isSelected());
                        handleItemUpdateFromTable(todoItem);
                    });
                    setGraphic(checkBox);
                }
            }
        });

        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());

        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedItem = newSelection;
            if (newSelection != null) {
                itemDescriptionField.setText(newSelection.description);
                itemPriorityField.setText(String.valueOf(newSelection.priority));
                updateItemButton.setDisable(false);
                deleteItemButton.setDisable(false);
                itemsTitle.setText("Редактировать задачу");
            } else {
                clearItemDetails();
            }
        });
    }

    private void updateAuthStatus() {
        User user = User.getUser();
        if (user.isUserActive) {
            authStatusLabel.setText("Авторизован как " + user.getUsername());
        } else {
            authStatusLabel.setText("Не авторизован");
        }
    }

    private void loadTodoLists() {
        todoLists.clear();
        if (User.getUser().isUserActive) {
            loadServerLists();
        } else {
            loadLocalLists();
        }
    }

    private void loadServerLists() {
        try {
            String authToken = getAuthToken();
            if (authToken == null || authToken.isEmpty()) {
                showError("Токен авторизации отсутствует. Пожалуйста, авторизуйтесь заново.");
                return;
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/plans"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET /plans Response Code: " + response.statusCode());
            System.out.println("GET /plans Response Body: " + response.body());
            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonList = jsonArray.getJSONObject(i);
                    List<TodoItem> items = new ArrayList<>();
                    JSONArray jsonItems = jsonList.getJSONArray("items");
                    for (int j = 0; j < jsonItems.length(); j++) {
                        JSONObject jsonItem = jsonItems.getJSONObject(j);
                        items.add(new TodoItem(
                                jsonItem.getInt("id"),
                                jsonList.getInt("id"),
                                jsonItem.getString("description"),
                                jsonItem.getBoolean("is_completed"),
                                jsonItem.getInt("priority"),
                                jsonItem.getString("created_at"),
                                jsonItem.getString("updated_at")
                        ));
                    }
                    todoLists.add(new TodoList(
                            jsonList.getInt("id"),
                            jsonList.getString("title"),
                            jsonList.getString("created_at"),
                            jsonList.getString("updated_at"),
                            items,
                            false
                    ));
                }
            } else if (response.statusCode() == 404) {
                showError("Сервер не поддерживает списки задач. Проверьте конфигурацию сервера.");
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError("Ошибка авторизации. Пожалуйста, авторизуйтесь заново.");
            } else {
                showError("Не удалось загрузить списки задач: HTTP " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка подключения к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLocalLists() {
        try {
            if (Files.exists(LOCAL_LISTS_PATH)) {
                String content = Files.readString(LOCAL_LISTS_PATH);
                JSONArray jsonArray = new JSONArray(content);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonList = jsonArray.getJSONObject(i);
                    List<TodoItem> items = new ArrayList<>();
                    JSONArray jsonItems = jsonList.getJSONArray("items");
                    for (int j = 0; j < jsonItems.length(); j++) {
                        JSONObject jsonItem = jsonItems.getJSONObject(j);
                        items.add(new TodoItem(
                                jsonItem.getInt("id"),
                                jsonList.getInt("id"),
                                jsonItem.getString("description"),
                                jsonItem.getBoolean("is_completed"),
                                jsonItem.getInt("priority"),
                                jsonItem.getString("created_at"),
                                jsonItem.getString("updated_at")
                        ));
                    }
                    todoLists.add(new TodoList(
                            jsonList.getInt("id"),
                            jsonList.getString("title"),
                            jsonList.getString("created_at"),
                            jsonList.getString("updated_at"),
                            items,
                            true
                    ));
                }
            }
        } catch (Exception e) {
            showError("Ошибка загрузки локальных списков: " + e.getMessage());
        }
    }

    private void saveLocalLists() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (TodoList list : todoLists) {
                if (list.isLocal) {
                    JSONObject jsonList = new JSONObject();
                    jsonList.put("id", list.id);
                    jsonList.put("title", list.title);
                    jsonList.put("created_at", list.createdAt);
                    jsonList.put("updated_at", list.updatedAt);
                    JSONArray jsonItems = new JSONArray();
                    for (TodoItem item : list.items) {
                        JSONObject jsonItem = new JSONObject();
                        jsonItem.put("id", item.id);
                        jsonItem.put("description", item.description);
                        jsonItem.put("is_completed", item.isCompleted.get());
                        jsonItem.put("priority", item.priority);
                        jsonItem.put("created_at", item.createdAt);
                        jsonItem.put("updated_at", item.updatedAt);
                        jsonItems.put(jsonItem);
                    }
                    jsonList.put("items", jsonItems);
                    jsonArray.put(jsonList);
                }
            }
            Files.writeString(LOCAL_LISTS_PATH, jsonArray.toString(2));
        } catch (IOException e) {
            showError("Ошибка сохранения локальных списков: " + e.getMessage());
        }
    }

    private void updateListsUI() {
        listsContainer.getChildren().clear();
        for (TodoList list : todoLists) {
            HBox cardBox = new HBox(10);
            Label card = new Label(list.title);
            card.getStyleClass().add("note-card");
            card.setUserData(list);
            card.setOnMouseClicked(this::handleListCardClick);
            Button deleteButton = new Button("X");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setUserData(list);
            deleteButton.setOnAction(this::handleDeleteListButton);
            cardBox.getChildren().addAll(card, deleteButton);
            listsContainer.getChildren().add(cardBox);
        }
        clearItemDetails();
        itemsTable.setItems(FXCollections.observableArrayList());
        if (selectedList != null) {
            for (TodoList list : todoLists) {
                if (list.id == selectedList.id && list.isLocal == selectedList.isLocal) {
                    selectedList = list;
                    itemsTable.setItems(FXCollections.observableArrayList(selectedList.items));
                    itemsTitle.setText("Задачи: " + selectedList.title);
                    break;
                }
            }
        }
    }

    private void handleListCardClick(MouseEvent event) {
        Label card = (Label) event.getSource();
        selectedList = (TodoList) card.getUserData();
        itemsTable.setItems(FXCollections.observableArrayList(selectedList.items));
        itemsTitle.setText("Задачи: " + selectedList.title);
        clearItemDetails();
    }

    @FXML
    private void handleAddListButton(ActionEvent event) {
        String title = listTitleField.getText().trim();
        if (title.isEmpty()) {
            showError("Поле названия списка обязательно");
            return;
        }
        if (User.getUser().isUserActive) {
            addServerList(title);
        } else {
            addLocalList(title);
        }
        listTitleField.clear();
    }

    private void addServerList(String title) {
        try {
            JSONObject json = new JSONObject();
            json.put("title", title);
            String authToken = getAuthToken();
            if (authToken == null || authToken.isEmpty()) {
                showError("Токен авторизации отсутствует. Пожалуйста, авторизуйтесь заново.");
                return;
            }
            System.out.println("POST /plans/add Request Body: " + json.toString());
            System.out.println("POST /plans/add Auth Token: " + authToken);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/plans/add"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST /plans/add Response Code: " + response.statusCode());
            System.out.println("POST /plans/add Response Body: " + response.body());
            if (response.statusCode() == 201) {
                JSONObject newList = new JSONObject(response.body());
                todoLists.add(new TodoList(
                        newList.getInt("id"),
                        newList.getString("title"),
                        newList.getString("created_at"),
                        newList.getString("updated_at"),
                        new ArrayList<>(),
                        false
                ));
                updateListsUI();
            } else if (response.statusCode() == 404) {
                showError("Сервер не поддерживает добавление списков задач. Проверьте конфигурацию сервера.");
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError("Ошибка авторизации. Пожалуйста, авторизуйтесь заново.");
            } else {
                showError("Не удалось добавить список: HTTP " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка подключения к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addLocalList(String title) {
        int newId = todoLists.stream().mapToInt(l -> l.id).max().orElse(0) + 1;
        String now = ZonedDateTime.now().toString();
        TodoList list = new TodoList(newId, title, now, now, new ArrayList<>(), true);
        todoLists.add(list);
        saveLocalLists();
        updateListsUI();
    }

    @FXML
    private void handleDeleteListButton(ActionEvent event) {
        Button button = (Button) event.getSource();
        TodoList list = (TodoList) button.getUserData();
        if (list.isLocal) {
            deleteLocalList(list);
        } else {
            deleteServerList(list);
        }
    }

    private void deleteServerList(TodoList list) {
        try {
            JSONObject json = new JSONObject();
            json.put("list_id", list.id);
            String authToken = getAuthToken();
            if (authToken == null || authToken.isEmpty()) {
                showError("Токен авторизации отсутствует. Пожалуйста, авторизуйтесь заново.");
                return;
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/plans/delete"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("DELETE /plans/delete Response Code: " + response.statusCode());
            System.out.println("DELETE /plans/delete Response Body: " + response.body());
            if (response.statusCode() == 200) {
                todoLists.remove(list);
                if (selectedList == list) {
                    selectedList = null;
                }
                updateListsUI();
            } else if (response.statusCode() == 404) {
                showError("Сервер не поддерживает удаление списков задач. Проверьте конфигурацию сервера.");
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError("Ошибка авторизации. Пожалуйста, авторизуйтесь заново.");
            } else {
                showError("Ошибка удаления списка: HTTP " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка подключения к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteLocalList(TodoList list) {
        todoLists.remove(list);
        if (selectedList == list) {
            selectedList = null;
        }
        saveLocalLists();
        updateListsUI();
    }

    @FXML
    private void handleAddItemButton(ActionEvent event) {
        if (selectedList == null) {
            showError("Выберите список для добавления задачи");
            return;
        }
        String description = itemDescriptionField.getText().trim();
        String priorityStr = itemPriorityField.getText().trim();
        if (description.isEmpty()) {
            showError("Поле описания задачи обязательно");
            return;
        }
        int priority;
        try {
            priority = priorityStr.isEmpty() ? 0 : Integer.parseInt(priorityStr);
            if (priority < 0 || priority > 10) {
                showError("Приоритет должен быть от 0 до 10");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Приоритет должен быть числом");
            return;
        }
        if (selectedList.isLocal) {
            addLocalItem(description, priority);
        } else {
            addServerItem(description, priority);
        }
        clearItemDetails();
    }

    private void addServerItem(String description, int priority) {
        try {
            JSONObject json = new JSONObject();
            json.put("list_id", selectedList.id);
            json.put("description", description);
            json.put("priority", priority);
            String authToken = getAuthToken();
            if (authToken == null || authToken.isEmpty()) {
                showError("Токен авторизации отсутствует. Пожалуйста, авторизуйтесь заново.");
                return;
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/plans/items/add"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST /plans/items/add Response Code: " + response.statusCode());
            System.out.println("POST /plans/items/add Response Body: " + response.body());
            if (response.statusCode() == 201) {
                JSONObject newItem = new JSONObject(response.body());
                selectedList.items.add(new TodoItem(
                        newItem.getInt("id"),
                        selectedList.id,
                        newItem.getString("description"),
                        newItem.getBoolean("is_completed"),
                        newItem.getInt("priority"),
                        newItem.getString("created_at"),
                        newItem.getString("updated_at")
                ));
                updateListsUI();
            } else if (response.statusCode() == 404) {
                showError("Сервер не поддерживает добавление задач. Проверьте конфигурацию сервера.");
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError("Ошибка авторизации. Пожалуйста, авторизуйтесь заново.");
            } else {
                showError("Ошибка добавления задачи: HTTP " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка подключения к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addLocalItem(String description, int priority) {
        int newId = selectedList.items.stream().mapToInt(i -> i.id).max().orElse(0) + 1;
        String now = ZonedDateTime.now().toString();
        TodoItem item = new TodoItem(newId, selectedList.id, description, false, priority, now, now);
        selectedList.items.add(item);
        saveLocalLists();
        updateListsUI();
    }

    @FXML
    private void handleUpdateItemButton(ActionEvent event) {
        if (selectedItem == null) {
            showError("Выберите задачу для обновления");
            return;
        }
        String description = itemDescriptionField.getText().trim();
        String priorityStr = itemPriorityField.getText().trim();
        if (description.isEmpty()) {
            showError("Поле описания задачи обязательно");
            return;
        }
        int priority;
        try {
            priority = priorityStr.isEmpty() ? 0 : Integer.parseInt(priorityStr);
            if (priority < 0 || priority > 10) {
                showError("Приоритет должен быть от 0 до 10");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Приоритет должен быть числом");
            return;
        }
        if (selectedList.isLocal) {
            updateLocalItem(selectedItem, description, selectedItem.isCompleted.get(), priority);
        } else {
            updateServerItem(selectedItem, description, selectedItem.isCompleted.get(), priority);
        }
        clearItemDetails();
    }

    private void handleItemUpdateFromTable(TodoItem item) {
        if (item == null || selectedList == null) {
            showError("Ошибка: задача или список не выбраны");
            return;
        }
        if (selectedList.isLocal) {
            updateLocalItem(item, item.description, item.isCompleted.get(), item.priority);
        } else {
            updateServerItem(item, item.description, item.isCompleted.get(), item.priority);
        }
    }

    private void updateServerItem(TodoItem item, String description, boolean isCompleted, int priority) {
        try {
            JSONObject json = new JSONObject();
            json.put("item_id", item.id);
            json.put("description", description);
            json.put("is_completed", isCompleted);
            json.put("priority", priority);
            String authToken = getAuthToken();
            if (authToken == null || authToken.isEmpty()) {
                showError("Токен авторизации отсутствует. Пожалуйста, авторизуйтесь заново.");
                return;
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/plans/items/update"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("PATCH /plans/items/update Response Code: " + response.statusCode());
            System.out.println("PATCH /plans/items/update Response Body: " + response.body());
            if (response.statusCode() == 200) {
                JSONObject updatedItem = new JSONObject(response.body()).getJSONObject("item");
                item.description = updatedItem.getString("description");
                item.isCompleted.set(updatedItem.getBoolean("is_completed"));
                item.priority = updatedItem.getInt("priority");
                item.updatedAt = updatedItem.getString("updated_at");
                updateListsUI();
            } else if (response.statusCode() == 404) {
                showError("Сервер не поддерживает обновление задач. Проверьте конфигурацию сервера.");
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError("Ошибка авторизации. Пожалуйста, авторизуйтесь заново.");
            } else {
                showError("Ошибка обновления задачи: HTTP " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка подключения к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLocalItem(TodoItem item, String description, boolean isCompleted, int priority) {
        if (item == null) {
            showError("Ошибка: задача не выбрана");
            return;
        }
        item.description = description;
        item.isCompleted.set(isCompleted);
        item.priority = priority;
        item.updatedAt = ZonedDateTime.now().toString();
        saveLocalLists();
        updateListsUI();
    }

    @FXML
    private void handleDeleteItemButton(ActionEvent event) {
        if (selectedItem == null) {
            showError("Выберите задачу для удаления");
            return;
        }
        if (selectedList.isLocal) {
            deleteLocalItem();
        } else {
            deleteServerItem();
        }
        clearItemDetails();
    }

    private void deleteServerItem() {
        try {
            JSONObject json = new JSONObject();
            json.put("item_id", selectedItem.id);
            String authToken = getAuthToken();
            if (authToken == null || authToken.isEmpty()) {
                showError("Токен авторизации отсутствует. Пожалуйста, авторизуйтесь заново.");
                return;
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/plans/items/delete"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("DELETE /plans/items/delete Response Code: " + response.statusCode());
            System.out.println("DELETE /plans/items/delete Response Body: " + response.body());
            if (response.statusCode() == 200) {
                selectedList.items.remove(selectedItem);
                selectedItem = null;
                updateListsUI();
            } else if (response.statusCode() == 404) {
                showError("Сервер не поддерживает удаление задач. Проверьте конфигурацию сервера.");
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError("Ошибка авторизации. Пожалуйста, авторизуйтесь заново.");
            } else {
                showError("Ошибка удаления задачи: HTTP " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError("Ошибка подключения к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteLocalItem() {
        selectedList.items.remove(selectedItem);
        selectedItem = null;
        saveLocalLists();
        updateListsUI();
    }

    private void clearItemDetails() {
        selectedItem = null;
        itemDescriptionField.clear();
        itemPriorityField.clear();
        updateItemButton.setDisable(true);
        deleteItemButton.setDisable(true);
        itemsTitle.setText(selectedList != null ? "Задачи: " + selectedList.title : "Выберите список");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getAuthToken() {
        String token = getAccessToken();
        if (token == null || token.isEmpty()) {
            System.out.println("Warning: Auth token is null or empty");
        }
        return token;
    }

    private boolean isAccessTokenValid() {
        return User.getUser().isUserActive;
    }

    private boolean refreshAccessToken() {
        return false; // Заглушка
    }

    private void updateUser() {
        // Заглушка
    }

    @FXML
    private void handleMainImageClick(MouseEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/fxml/mainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/icon.png")));
        stage.setTitle("Productivity Monitor");
        stage.setMinWidth(850);
        stage.setMinHeight(500);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleProfileButton(ActionEvent event) throws IOException {
        if (isAccessTokenValid() && User.getUser().isUserActive) {
            loadProfileStage(event);
        } else {
            if (refreshAccessToken()) {
                updateUser();
                loadProfileStage(event);
            } else {
                loadAuthStage(event);
            }
        }
    }

    private void loadAuthStage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/authView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        AuthController authController = fxmlLoader.getController();
        authStage = new Stage();
        authController.setMainStage((Stage) ((Node) event.getSource()).getScene().getWindow());
        authController.setThisStage(authStage);
        authStage.setTitle("Authentication");
        authStage.setScene(scene);
        authStage.setResizable(false);
        authStage.show();
    }

    private void loadProfileStage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/profileView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Profile");
        stage.show();
    }

    @FXML private void handleStatisticsButton(ActionEvent event) {}
    @FXML private void handleSettingsButton(ActionEvent event) {}
    @FXML private void handleAchievementsButton(ActionEvent event) {}
    @FXML private void handleNotesButton(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/notesView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Notes");
        stage.show();
    }
    @FXML private void handlePlansButton(ActionEvent event) {}
}