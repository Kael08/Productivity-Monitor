package productivityMonitor.interfaces;

public interface MonitoringMode {
    void start(long durationMillis); // Запуск режима (durationMillis = 0 для бесконечного режима)
    void stop(); // Остановка режима
    String getName(); // Название режима
}
