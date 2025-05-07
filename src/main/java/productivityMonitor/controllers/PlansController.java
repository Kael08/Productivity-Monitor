package productivityMonitor.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
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
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import productivityMonitor.models.User;

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
import java.util.Locale;
import java.util.ResourceBundle;

import static productivityMonitor.application.MainApp.MainStage;
import static productivityMonitor.services.StageService.createScene;
import static productivityMonitor.services.TokenManager.*;
import static productivityMonitor.services.StageService.replaceMainScene;
import static productivityMonitor.controllers.SettingsController.getLang;

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
    private final Path LOCAL_LISTS_PATH = Path.of("src/main/resources/data/local_todo_lists.json");
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

    // ResourceBundle для локализации
    private ResourceBundle bundle;

    // Применение локализации
    private void applyLocalization() {
        MainStage.setTitle(bundle.getString("plans"));
        profileButton.setText(bundle.getString("profile"));
        statisticsButton.setText(bundle.getString("statistics"));
        settingsButton.setText(bundle.getString("settings"));
        achievementsButton.setText(bundle.getString("achievements"));
        notesButton.setText(bundle.getString("notes"));
        plansButton.setText(bundle.getString("plans"));
        listsTitle.setText(bundle.getString("plans.taskList"));
        authStatusLabel.setText(bundle.getString("plans.notAuthorized"));
        listTitleField.setPromptText(bundle.getString("plans.listName"));
        addListButton.setText(bundle.getString("plans.addList"));
        itemsTitle.setText(bundle.getString("plans.selectList"));
        completedColumn.setText(bundle.getString("plans.done"));
        descriptionColumn.setText(bundle.getString("plans.description"));
        priorityColumn.setText(bundle.getString("plans.priority"));
        itemDescriptionField.setPromptText(bundle.getString("plans.taskDescription"));
        itemPriorityField.setPromptText(bundle.getString("plans.priority(0-10)"));
        addItemButton.setText(bundle.getString("plans.addTask"));
        updateItemButton.setText(bundle.getString("plans.updateTask"));
        deleteItemButton.setText(bundle.getString("plans.deleteTask"));
    }

    // Установка локализации
    private void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
    }

    @FXML
    public void initialize() {
        setLocalization(getLang());

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
                itemsTitle.setText(bundle.getString("plans.editTask"));
            } else {
                clearItemDetails();
            }
        });
    }

    private void updateAuthStatus() {
        User user = User.getUser();
        if (user.isUserActive) {
            authStatusLabel.setText(bundle.getString("plans.authorized") + user.getUsername());
        } else {
            authStatusLabel.setText(bundle.getString("plans.notAuthorized"));
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
                showError(bundle.getString("plans.authTokenMissing"));
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
                showError(bundle.getString("plans.notSuppTaskList"));
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError(bundle.getString("plans.errorAuthTryAg"));
            } else {
                showError(bundle.getString("plans.errorLoadTaskList") + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("plans.errorServerBadConnect") + e.getMessage());
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
            showError(bundle.getString("plans.errorLoadLocalLists") + e.getMessage());
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
            showError(bundle.getString("plans.errorSaveLocalLists") + e.getMessage());
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
                    itemsTitle.setText(bundle.getString("plans.tasks") + selectedList.title);
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
            showError(bundle.getString("plans.errorFieldName"));
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
                showError(bundle.getString("plans.authTokenMissing"));
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
                showError(bundle.getString("plans.errorServerNotSuppAddTaskLists"));
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError(bundle.getString("plans.errorAuthTryAg"));
            } else {
                showError(bundle.getString("plans.errorAddList") + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("plans.errorServerBadConnect") + e.getMessage());
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
                showError(bundle.getString("plans.authTokenMissing"));
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
                showError(bundle.getString("plans.errorServerNotSuppDelListsTask"));
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError(bundle.getString("plans.errorAuthTryAg"));
            } else {
                showError(bundle.getString("plans.errorDelList") + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("plans.errorServerBadConnect") + e.getMessage());
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
            showError(bundle.getString("plans.errorSelectListForTaskAdd"));
            return;
        }
        String description = itemDescriptionField.getText().trim();
        String priorityStr = itemPriorityField.getText().trim();
        if (description.isEmpty()) {
            showError(bundle.getString("plans.errorFieldTaskDesc"));
            return;
        }
        int priority;
        try {
            priority = priorityStr.isEmpty() ? 0 : Integer.parseInt(priorityStr);
            if (priority < 0 || priority > 10) {
                showError(bundle.getString("plans.errorPriorityMustBe1-10"));
                return;
            }
        } catch (NumberFormatException e) {
            showError(bundle.getString("plans.errorPriorityMustBeDec"));
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
                showError(bundle.getString("plans.authTokenMissing"));
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
                showError(bundle.getString("plans.errorServerNotSuppAddTask"));
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError(bundle.getString("plans.errorAuthTryAg"));
            } else {
                showError(bundle.getString("plans.errorAddTask") + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("plans.errorServerBadConnect") + e.getMessage());
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
            showError(bundle.getString("plans.selectTaskToUpdate"));
            return;
        }
        String description = itemDescriptionField.getText().trim();
        String priorityStr = itemPriorityField.getText().trim();
        if (description.isEmpty()) {
            showError(bundle.getString("plans.errorFieldTaskDesc"));
            return;
        }
        int priority;
        try {
            priority = priorityStr.isEmpty() ? 0 : Integer.parseInt(priorityStr);
            if (priority < 0 || priority > 10) {
                showError(bundle.getString("plans.errorPriorityMustBe1-10"));
                return;
            }
        } catch (NumberFormatException e) {
            showError(bundle.getString("plans.errorPriorityMustBeDec"));
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
            showError(bundle.getString("plans.errorTaskOrListNotSel"));
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
                showError(bundle.getString("plans.authTokenMissing"));
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
                showError(bundle.getString("plans.errorServerNotSuppUpdateTasks"));
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError(bundle.getString("plans.errorAuthTryAg"));
            } else {
                showError(bundle.getString("plans.errorUpdateTask") + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("plans.errorServerBadConnect") + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLocalItem(TodoItem item, String description, boolean isCompleted, int priority) {
        if (item == null) {
            showError(bundle.getString("plans.errorTaskNotSel"));
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
            showError(bundle.getString("plans.errorSelectTaskForDelete"));
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
                showError(bundle.getString("plans.authTokenMissing"));
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
                showError(bundle.getString("plans.errorServerNotSuppDelTasks"));
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                showError(bundle.getString("plans.errorAuthTryAg"));
            } else {
                showError(bundle.getString("plans.errorDelTask") + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            showError(bundle.getString("plans.errorServerBadConnect") + e.getMessage());
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
        itemsTitle.setText(selectedList != null ? bundle.getString("plans.tasks") + selectedList.title : bundle.getString("plans.errorSelectList"));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("plans.error"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getAuthToken() {
        String token = getAccessToken();
        if (token == null || token.isEmpty()) {
            System.out.println(bundle.getString("plans.errorAuthTokenNull"));
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
        replaceMainScene("/fxml/mainView.fxml","Productivity Monitor");
    }

    @FXML
    private void handleProfileButton(ActionEvent event) throws IOException {
        if (isAccessTokenValid() && User.getUser().isUserActive) {
            replaceMainScene("/fxml/profileView.fxml","Profile");
        } else {
            if (refreshAccessToken()) {
                updateUser();
                replaceMainScene("/fxml/profileView.fxml","Profile");
            } else {
                if(authStage!=null&&authStage.isShowing()) {
                    authStage.toFront();
                    return;
                }
                authStage=new Stage();
                createScene("/fxml/authView.fxml","Authentification",authStage,false);
            }
        }
    }


    @FXML private void handleStatisticsButton(ActionEvent event) {}
    @FXML private void handleSettingsButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/settingsView.fxml","Settings");
    }
    @FXML private void handleAchievementsButton(ActionEvent event) {}
    @FXML private void handleNotesButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/notesView.fxml","Notes");
    }
    @FXML private void handlePlansButton(ActionEvent event) {
        // Кнопка заблокирована
    }
}