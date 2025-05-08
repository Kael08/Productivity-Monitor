package productivityMonitor.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TodoItem {
    private int id;
    private int todoListId;
    private String description;
    private BooleanProperty isCompleted;
    private int priority;
    private LocalDate deadline;
    private String createdAt;
    private String updatedAt;

    // Форматтеры для дат
    public static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter SERVER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter SERVER_FULL_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    public TodoItem(int id, int todoListId, String description, boolean isCompleted, int priority, LocalDate deadline,
                    String createdAt, String updatedAt) {
        this.id = id;
        this.todoListId = todoListId;
        this.description = description;
        this.isCompleted = new SimpleBooleanProperty(isCompleted);
        this.priority = priority;
        this.deadline = deadline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTodoListId() { return todoListId; }
    public void setTodoListId(int todoListId) { this.todoListId = todoListId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return isCompleted.get(); }
    public BooleanProperty isCompletedProperty() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted.set(completed); }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public StringProperty descriptionProperty() {
        return new SimpleStringProperty(description);
    }

    public StringProperty priorityProperty() {
        return new SimpleStringProperty(String.valueOf(priority));
    }

    public StringProperty deadlineProperty() {
        return new SimpleStringProperty(deadline != null ? INPUT_FORMATTER.format(deadline) : "");
    }

    // Конвертация строки даты из формата ввода в LocalDate
    public static LocalDate parseInputDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, INPUT_FORMATTER);
    }

    // Конвертация LocalDate в формат сервера
    public String formatDeadlineForServer() {
        return deadline != null ? SERVER_FORMATTER.format(deadline) : null;
    }

    // Конвертация строки даты от сервера в LocalDate
    public static LocalDate parseServerDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Сначала пытаемся разобрать полный формат ISO 8601
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr, SERVER_FULL_FORMATTER);
            return zonedDateTime.toLocalDate();
        } catch (DateTimeParseException e) {
            // Если не удалось, пробуем формат yyyy-MM-dd
            return LocalDate.parse(dateStr, SERVER_FORMATTER);
        }
    }
}