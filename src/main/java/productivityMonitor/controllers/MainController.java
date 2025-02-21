package productivityMonitor.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Pattern;

public class MainController {

    private Task<Void> task;
    @FXML
    private Button startBT;
    @FXML
    private Button stopBT;
    @FXML
    private TextArea infoArea;
    @FXML
    private Button addBT;
    @FXML
    private TextField textFieldAdd;

    @FXML
    private Button deleteBT;
    @FXML
    private ComboBox<String> comboBox = new ComboBox<>();
    private ObservableList<String> items = FXCollections.observableArrayList();
    private List<String> badProccesses = new ArrayList<>();

    @FXML
    public void initialize(){
        stopBT.setDisable(true);

        for(String s:badProccesses)
            items.add(s);

        comboBox.setItems(items);
    }

    @FXML
    private void add(){
        String processName = textFieldAdd.getText();
        //^.*\\.exe$ - проверка на наличие текста и окончания на .exe
        //^(?!.*\\.exe$).+$ - проверка на наличие текста но уже без окончание на .exe
        if (processName.length()!=4&&Pattern.matches("^.*\\.exe$", processName)) {
            badProccesses.add(processName);
            //comboBox.getItems().add(processName);
            items.add(processName);
            infoArea.setText(infoArea.getText()+"\n"+processName+" успешно добавлен");
        } else if (Pattern.matches("^(?!.*\\.exe$).+$",processName)) {
            badProccesses.add(processName+".exe");
            //comboBox.getItems().add(processName+".exe");
            items.add(processName+".exe");
            infoArea.setText(infoArea.getText()+"\n"+processName+".exe успешно добавлен1");
        } else{
            infoArea.setText(infoArea.getText()+"\nОшибка при добавлении процесса "+processName);
        }
        textFieldAdd.clear();
    }

    @FXML
    private void delete(){
        String selectedItem = comboBox.getValue();
        badProccesses.remove(selectedItem);
        items.remove(selectedItem);
        infoArea.setText(infoArea.getText()+"\n"+selectedItem+"успешно удален");
    }

    @FXML
    private void start(){

        startBT.setDisable(true);
        stopBT.setDisable(false);
        addBT.setDisable(true);
        deleteBT.setDisable(true);
        if(task!=null&&task.isRunning()){
            return;
        }

        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                // Имя процесса, который нужно закрыть
                infoArea.setText(infoArea.getText()+"\nМонитор запущен\nБлокируемые процессы:");
                for(String s:badProccesses)
                    infoArea.setText(infoArea.getText()+"\n"+s);

                while (!isCancelled()) {
                    try {
                        for(String processName:badProccesses){
                            // команда для закрытия процесса
                            String command = "taskkill /F /IM "+ processName;
                            // Запуск команды через Runtime
                            Process process = Runtime.getRuntime().exec(command);
                            // Ожидание завершения процесса
                            int exitCode = process.waitFor();
                            switch (exitCode){
                                case 0:
                                    infoArea.setText(infoArea.getText()+"\n" + processName + " закрыт");
                                    break;
                                case 128:
                                    break;
                                default:
                                    infoArea.setText(infoArea.getText()+"\nПроцесс завершился с ошибкой. Код завершения: " + exitCode);
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(1000);
                }
                return null;
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                startBT.setDisable(false);
                stopBT.setDisable(true);
                addBT.setDisable(false);
                deleteBT.setDisable(false);
                infoArea.setText(infoArea.getText()+"\nМонитор завершил работу");
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void stop()
    {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
    }
}