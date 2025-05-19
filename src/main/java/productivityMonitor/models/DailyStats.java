package productivityMonitor.models;

public class DailyStats {
    private final String date;
    private final String monitoringTime; // Формат: HH:mm:ss
    private final int blockedProcesses;
    private final int blockedDomains;

    public DailyStats(String date,String monitoringTimem,int blockedProcesses,int blockedDomains){
        this.date=date;
        this.monitoringTime=monitoringTimem;
        this.blockedProcesses=blockedProcesses;
        this.blockedDomains=blockedDomains;
    }

    public String getDate(){
        return date;
    }

    public String getMonitoringTime(){
        return monitoringTime;
    }

    public int getBlockedProcesses(){
        return blockedProcesses;
    }

    public int getBlockedDomains(){
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
