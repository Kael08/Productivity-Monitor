package productivityMonitor.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WindowsFirewallDomainBlocker {

    // Список доменов для блокировки
    private static final List<String> BLOCKED_DOMAINS = List.of(
            "youtube.com",
            "www.youtube.com",
            "facebook.com",
            "www.facebook.com",
            "vk.ru",
            "www.vk.ru",
            "vk.com",
            "www.vk.com"
    );

    /*public static void main(String[] args) {
        // Блокируем все домены из списка
        BLOCKED_DOMAINS.forEach(WindowsFirewallDomainBlocker::blockDomain);

        // Проверяем статус блокировки
        BLOCKED_DOMAINS.forEach(WindowsFirewallDomainBlocker::checkBlockStatus);
    }*/

    public static void startBlocker(){
        // Блокировка доменов из списка
        for(String s:BLOCKED_DOMAINS){
            blockDomain(s);
        }

        // Проверяем статусы блокировки
        for(String s:BLOCKED_DOMAINS){
            checkBlockStatus(s);
        }
    }

    public static void stopBlocker(){
        // Разблокировка доменов из списка
        for(String s:BLOCKED_DOMAINS){
            unblockDomain(s);
        }

        // Проверяем статусы блокировки
        for(String s:BLOCKED_DOMAINS){
            checkBlockStatus(s);
        }
    }

    /**
     * Блокирует указанный домен через брандмауэр Windows
     * @param domain Домен для блокировки (например, "youtube.com")
     */
    public static void blockDomain(String domain) {
        if (isDomainBlocked(domain)) {
            System.out.println("Домен " + domain + " уже заблокирован");
            return;
        }
        try {
            // Получаем IP-адреса домена
            List<String> ipAddresses = resolveDomainToIp(domain);
            if (ipAddresses.isEmpty()) {
                System.err.println("Не удалось разрешить IP для домена: " + domain);
                return;
            }

            // Блокируем каждый IP-адрес
            for (String ip : ipAddresses) {
                // Блокируем HTTP (порт 80)
                executeCommand(
                        "netsh advfirewall firewall add rule name=\"Block " + domain + " HTTP\" " +
                                "dir=out action=block remoteip=" + ip + " protocol=TCP remoteport=80 enable=yes"
                );

                // Блокируем HTTPS (порт 443)
                executeCommand(
                        "netsh advfirewall firewall add rule name=\"Block " + domain + " HTTPS\" " +
                                "dir=out action=block remoteip=" + ip + " protocol=TCP remoteport=443 enable=yes"
                );
            }

            System.out.println("Домен " + domain + " успешно заблокирован");

        } catch (Exception e) {
            System.err.println("Ошибка при блокировке домена " + domain + ": " + e.getMessage());
        }
    }

    // Проверка существования правила перед созданием
    public static boolean isDomainBlocked(String domain) {
        try {
            Process process = Runtime.getRuntime().exec(
                    "netsh advfirewall firewall show rule name=\"Block " + domain + " HTTP\"");
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Разрешает доменное имя в список IP-адресов
     */
    private static List<String> resolveDomainToIp(String domain) throws IOException {
        List<String> ipAddresses = new ArrayList<>();
        Process process = Runtime.getRuntime().exec("nslookup " + domain);

        String encoding = System.getProperty("os.name").toLowerCase().contains("win")
                ? "CP866"
                : "UTF-8";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), encoding))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Address:") && !line.contains(":" + domain)) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        String ip = parts[parts.length - 1].trim();
                        if (ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                            ipAddresses.add(ip);
                        }
                    }
                }
            }
        }

        if (ipAddresses.isEmpty()) {
            throw new IOException("Не удалось разрешить IP для домена: " + domain);
        }

        return ipAddresses;
    }

    /**
     * Проверяет статус блокировки для домена
     */
    public static void checkBlockStatus(String domain) {
        try {
            executeCommand("netsh advfirewall firewall show rule name=\"Block " + domain + " HTTP\"");
            executeCommand("netsh advfirewall firewall show rule name=\"Block " + domain + " HTTPS\"");
        } catch (Exception e) {
            System.err.println("Ошибка при проверке статуса для " + domain + ": " + e.getMessage());
        }
    }

    /**
     * Удаляет блокировку для домена
     */
    public static void unblockDomain(String domain) {
        try {
            executeCommand("netsh advfirewall firewall delete rule name=\"Block " + domain + " HTTP\"");
            executeCommand("netsh advfirewall firewall delete rule name=\"Block " + domain + " HTTPS\"");
            System.out.println("Домен " + domain + " разблокирован");
        } catch (Exception e) {
            System.err.println("Ошибка при разблокировке домена " + domain + ": " + e.getMessage());
        }
    }

    /**
     * Выполняет команду в командной строке
     */
    private static void executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();

        // Для Windows используем CP866, для Linux/Mac - UTF-8
        String encoding = System.getProperty("os.name").toLowerCase().contains("win")
                ? "CP866"
                : "UTF-8";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), encoding))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        // Также читаем поток ошибок с правильной кодировкой
        try (BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), encoding))) {

            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println(errorLine);
            }
        }
    }
}