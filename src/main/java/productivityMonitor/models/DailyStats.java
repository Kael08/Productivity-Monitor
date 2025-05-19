package productivityMonitor.models;

public class DailyStats {
    private final String date;
    private final String monitoringTime; // Будем хранить как строку в формате HH:mm:ss
    private final int blockedProcesses;
    private final int blockedDomains;

    public DailyStats(String date, int hours, int minutes, int seconds, int blockedProcesses, int blockedDomains) {
        this.date = date;
        // Форматируем переданные часы, минуты и секунды в строку HH:mm:ss
        this.monitoringTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        this.blockedProcesses = blockedProcesses;
        this.blockedDomains = blockedDomains;
    }

    public String getDate() {
        return date;
    }

    public String getMonitoringTime() {
        return monitoringTime;
    }

    public int getBlockedProcesses() {
        return blockedProcesses;
    }

    public int getBlockedDomains() {
        return blockedDomains;
    }

    // Метод для конвертации времени мониторинга в минуты (для графика)
    public double getMonitoringTimeInMinutes() {
        String[] parts = monitoringTime.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 60 + minutes + seconds / 60.0;
    }
}