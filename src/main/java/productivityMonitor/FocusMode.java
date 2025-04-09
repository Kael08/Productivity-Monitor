package productivityMonitor;

import javafx.scene.control.TextArea;

import java.util.concurrent.atomic.AtomicReference;

import static productivityMonitor.utils.SharedData.*;

public class FocusMode {

    private TextArea consoleTextArea;

    private final Thread monitorThread = new Thread();

    private final AtomicReference<Runnable> currentTask = new AtomicReference<>();

    public FocusMode(TextArea consoleTextArea){
        this.consoleTextArea=consoleTextArea;
    }

    // Режим, который блокирует запуск приложение и переход на определенные домены
    public void fullLockdown(){

    }

    // Задача для закрытия процессов
    Runnable runMonitor = () -> {
        if(minutes==0) {
            consoleTextArea.appendText("Монитор запущен!\n");
            while (isMonitoringActive) {
                try {
                    closeProcess(processList);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    consoleTextArea.appendText("Монитор остановлен!\n");
                    return;
                }
            }
        } else {
            long endTime = System.currentTimeMillis()+minutes * 60 * 1000;
            consoleTextArea.appendText("Монитор запущен с таймеров на "+minutes+" минут!\n");
            while (isMonitoringActive&&System.currentTimeMillis()<endTime){
                try{
                    closeProcess(processList);
                    Thread.sleep(2000);
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    consoleTextArea.appendText("Монитор прерван!\n");
                    return;
                }
            }
            consoleTextArea.appendText("Монитор завершил работу! Время вышло!\n");
        }
        isMonitoringActive=false;
    };

    // Режим, при котором для запуска приложения или перехода
    // на определенный домен требуется выполнить трудное задание
    public void sailorsKnot(){

    }

    // Режим, который требует подождать несколько минут для
    // запуска нежелательного приложения или домена
    public void delayGratification(){

    }

    // Режим, который пытается отговорить пользователя
    // от запуска нежелательного приложения или домена
    public void mindfulness(){

    }

    // Режим, суть которого заключается в перемене
    // 5 минут каждые 25 минут работы(параметры времени можно настроить)
    public void pomodoro(){

    }
}
