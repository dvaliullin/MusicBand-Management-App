package ru.itmo.lab8.client.gui.util;

import ru.itmo.lab8.client.gui.components.main.BandFormDialog;
import ru.itmo.lab8.client.gui.components.main.BandTable;
import ru.itmo.lab8.client.gui.components.main.MainWindow;
import ru.itmo.lab8.client.network.Requester;
import ru.itmo.lab8.common.data.MusicBand;
import ru.itmo.lab8.common.util.Request;
import ru.itmo.lab8.common.util.Response;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;

/**
 * Класс для обработки команд и триггеров
 */
public class CommandHandler {
    private final Requester requester;
    private final MainWindow mainWindow;
    private final BandTable bandTable;

    public CommandHandler(Requester requester, MainWindow mainWindow, BandTable bandTable) {
        this.requester = requester;
        this.mainWindow = mainWindow;
        this.bandTable = bandTable;
    }

    /**
     * @param name
     * @param arg
     * @param band
     * Обработка команды
     */
    public void handleCommand(String name, String arg, MusicBand band) {
        try {
            Request request = new Request(name, arg, band);
            Response response = requester.sendRequest(request);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(mainWindow,
                        response.getMessage(),
                        LocalizationManager.getString("success"),
                        JOptionPane.INFORMATION_MESSAGE);
                refreshCollectionFromServer();
            } else {
                JOptionPane.showMessageDialog(mainWindow,
                        response.getMessage(),
                        LocalizationManager.getString("error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    LocalizationManager.getString("cannot_connect") + ": " + e.getMessage(),
                    LocalizationManager.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Обновление коллекции по данным с сервера
     */
    public synchronized void refreshCollectionFromServer() {
        try {
            Request request = new Request("show", "", null);
            Response response = requester.sendRequest(request);
            if (response.isSuccess() && response.getData() instanceof Map) {
                Map<Integer, MusicBand> elements = (Map<Integer, MusicBand>) response.getData();
                bandTable.setElementsMap(elements);
                bandTable.refreshDataViews();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Триггер добавления элемента без заданных координат
     */
    public void triggerInsert() {
        triggerInsertAt(null, null);
    }

    /**
     * @param defaultX
     * @param defaultY
     * Триггер добавления элемента с заданными координатами
     */
    public void triggerInsertAt(Integer defaultX, Long defaultY) {
        BandFormDialog dialog = new BandFormDialog(mainWindow, null, null, defaultX, defaultY);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            handleCommand("insert", String.valueOf(dialog.getResultKey()), dialog.getResultBand());
        }
    }

    /**
     * Триггер редактирования элемента
     */
    public void triggerEditSelected() {
        if (mainWindow.getSelectedKey() == null) {
            showNoSelectionWarning();
            return;
        }

        MusicBand band = bandTable.getBySelectedKey();
        if (band != null) {
            if (!mainWindow.getCurrentUsername().equals(band.getOwner())) {
                JOptionPane.showMessageDialog(mainWindow,
                        LocalizationManager.getString("owner_error"),
                        LocalizationManager.getString("error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            BandFormDialog dialog = new BandFormDialog(mainWindow, mainWindow.getSelectedKey(), band, null, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                handleCommand("update", String.valueOf(band.getId()), dialog.getResultBand());
            }
        }
    }

    /**
     * Триггер удаления элемента
     */
    public void triggerDeleteSelected() {
        if (mainWindow.getSelectedKey() == null) {
            showNoSelectionWarning();
            return;
        }

        MusicBand band = bandTable.getBySelectedKey();
        if (band != null) {
            if (!mainWindow.getCurrentUsername().equals(band.getOwner())) {
                JOptionPane.showMessageDialog(mainWindow,
                        LocalizationManager.getString("owner_error"),
                        LocalizationManager.getString("error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = getConfirm("confirm_delete" ,"cmd_delete");

            if (confirm == 0) {
                handleCommand("remove_key", String.valueOf(mainWindow.getSelectedKey()), null);
                mainWindow.setSelectedKey(null);
            }
        }
    }

    /**
     * Триггер выполнения скрипта
     */
    public void triggerExecuteScript() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(LocalizationManager.getString("script_chooser_title"));
        int option = chooser.showOpenDialog(mainWindow);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            handleCommand("execute_script", file.getAbsolutePath(), null);
        }
    }

    /**
     * Триггер подсчета элементов больше заданного
     */
    public void triggerCountGreater() {
        String input = JOptionPane.showInputDialog(mainWindow,
                LocalizationManager.getString("dialog_participants_threshold"),
                LocalizationManager.getString("cmd_count_greater"),
                JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            handleCommand("count_greater_than_number_of_participants", input.trim(), null);
        }
    }

    /**
     * Триггер удаления элементов больше заданного
     */
    public void triggerRemoveGreater() {
        BandFormDialog dialog = new BandFormDialog(mainWindow, null, null, null, null);
        dialog.keyField.setText("1");
        dialog.keyField.setEnabled(false);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            handleCommand("remove_greater", "", dialog.getResultBand());
        }
    }

    /**
     * Триггер удаления элементов с ключом больше заданного
     */
    public void triggerRemoveGreaterKey() {
        String input = JOptionPane.showInputDialog(mainWindow,
                LocalizationManager.getString("dialog_key_threshold"),
                LocalizationManager.getString("cmd_remove_greater_key"),
                JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            handleCommand("remove_greater_key", input.trim(), null);
        }
    }

    /**
     * @param currentUsername
     * Триггер замены элементов больше заданного
     */
    public void triggerReplaceIfGreater(String currentUsername) {
        if (mainWindow.getSelectedKey() == null) {
            showNoSelectionWarning();
            return;
        }

        MusicBand band = bandTable.getBySelectedKey();
        if (band != null) {
            if (!currentUsername.equals(band.getOwner())) {
                JOptionPane.showMessageDialog(mainWindow,
                        LocalizationManager.getString("owner_error"),
                        LocalizationManager.getString("error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            BandFormDialog dialog = new BandFormDialog(mainWindow, mainWindow.getSelectedKey(), band, null, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                handleCommand("replace_if_greater", String.valueOf(mainWindow.getSelectedKey()), dialog.getResultBand());
            }
        }
    }

    /**
     * Триггер просмотра всех элементов в порядке возрастания
     */
    public void triggerShowAscending() {
        try {
            Request request = new Request("print_ascending", "", null);
            Response response = requester.sendRequest(request);

            if (response.isSuccess()) {
                JScrollPane scrollPane = getAscendingElementsScrollPane(response.getMessage());

                JOptionPane.showMessageDialog(mainWindow,
                        scrollPane,
                        LocalizationManager.getString("cmd_print_ascending"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainWindow,
                        response.getMessage(),
                        LocalizationManager.getString("error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    LocalizationManager.getString("cannot_connect") + ": " + e.getMessage(),
                    LocalizationManager.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Триггер очистки элементов пользователя с подтверждением
     */
    public void triggerClear() {
        // Индекс 0 соответствует первому переданному варианту (yes_btn)
        if (getConfirm("confirm_clear", "cmd_clear") == 0) {
            handleCommand("clear", "", null);
        }
    }

    /**
     * @param message
     * @return панель со скроллингом элементов при вызове команды print_ascending
     */
    private JScrollPane getAscendingElementsScrollPane(String message) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(245, 245, 245));
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 320));
        return scrollPane;
    }


    /**
     * @param confirmMessage
     * @return подтверждение об удалении элемента (элементов) или отмена команды
     */
    private int getConfirm(String confirmMessage, String cmdName) {
        String[] options = {
                LocalizationManager.getString("yes_btn"),
                LocalizationManager.getString("no_btn")
        };

        return JOptionPane.showOptionDialog(
                mainWindow,
                LocalizationManager.getString(confirmMessage),
                LocalizationManager.getString(cmdName),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );
    }

    /**
     * Предупреждение о необходимости выбрать элемент для выполнения команды
     */
    private void showNoSelectionWarning() {
        JOptionPane.showMessageDialog(mainWindow,
                LocalizationManager.getString("not_selected"),
                LocalizationManager.getString("error"),
                JOptionPane.WARNING_MESSAGE);
    }
}
