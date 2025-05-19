package productivityMonitor.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static productivityMonitor.controllers.MainController.blockedProcesses;

public class ProcessUtils {
    private final ConsoleLogger logger;

    public ProcessUtils(ConsoleLogger logger) {
        this.logger = logger;
    }

    public void closeProcesses(List<String> list) {
        for (String pn : list) {
            try {
                ProcessBuilder builder = new ProcessBuilder("taskkill", "/IM", pn, "/F");
                Process process = builder.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    logger.log("Процесс " + pn + " был завершен\n");
                    blockedProcesses++;
                }
            } catch (Exception e) {
                logger.log("Не удалось завершить процесс " + pn + ": " + e.getMessage() + "\n");
            }
        }
    }

    public boolean isProcessesActive(List<String> requiredProcessesList) {
        return getProcessList().stream().anyMatch(requiredProcessesList::contains);
    }

    public List<String> getProcessList() {
        List<String> processList = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder("tasklist");
        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            boolean skipHeader = true;
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    String processName = line.split("\\s+")[0];
                    processList.add(processName);
                }
            }

            process.waitFor();
        } catch (Exception e) {
            logger.log("Ошибка получения списка процессов: " + e.getMessage() + "\n");
        }

        return processList;
    }
}