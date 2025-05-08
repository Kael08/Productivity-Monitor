package productivityMonitor.models;

import java.util.ArrayList;
import java.util.List;

public class TodoList {
    private int id;
    private String title;
    private String createdAt;
    private String updatedAt;
    private List<TodoItem> items;
    private boolean isLocal;

    public TodoList(int id, String title, String createdAt, String updatedAt, List<TodoItem> items, boolean isLocal) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items != null ? items : new ArrayList<>();
        this.isLocal = isLocal;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public List<TodoItem> getItems() { return items; }
    public void setItems(List<TodoItem> items) { this.items = items; }
    public boolean isLocal() { return isLocal; }
    public void setLocal(boolean local) { isLocal = local; }
}