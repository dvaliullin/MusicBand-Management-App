package ru.itmo.lab8.client.gui.components.main;

import org.jxmapviewer.viewer.GeoPosition;
import ru.itmo.lab8.client.gui.components.visualization.VisualizationPanel;
import ru.itmo.lab8.client.gui.util.CommandHandler;
import ru.itmo.lab8.client.gui.util.LocalizationManager;
import ru.itmo.lab8.client.network.Requester;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * Главное окно
 */
public class MainWindow extends JFrame {
    private final CommandHandler commandHandler;
    private final String currentUsername;

    private Integer selectedKey = null;

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JLabel userLabel = new JLabel();
    private final JComboBox<LocaleItem> languageComboBox = new JComboBox<>();
    private final JButton logoutBtn = new JButton();

    private final BandTable bandTable;
    private final JLabel filterLabel = new JLabel();
    private final JTextField filterField = new JTextField(15);
    private final JComboBox<String> columnFilterComboBox = new JComboBox<>();

    private final VisualizationPanel visualizationPanel;

    private final JButton cmdHelp = new JButton();
    private final JButton cmdInfo = new JButton();
    private final JButton cmdAdd = new JButton();
    private final JButton cmdUpdate = new JButton();
    private final JButton cmdDelete = new JButton();
    private final JButton cmdClear = new JButton();
    private final JButton cmdExecute = new JButton();
    private final JButton cmdCountGreater = new JButton();
    private final JButton cmdRemoveGreater = new JButton();
    private final JButton cmdRemoveGreaterKey = new JButton();
    private final JButton cmdReplaceGreater = new JButton();
    private final JButton cmdPrintAscending = new JButton();
    private final JButton cmdPrintDescPart = new JButton();

    /**
     * Вложенный класс для локали
     */
    private static class LocaleItem {
        private final Locale locale;
        private final String displayName;

        LocaleItem(Locale locale, String displayName) {
            this.locale = locale;
            this.displayName = displayName;
        }

        public Locale getLocale() {
            return locale;
        }

        @Override
        public String toString() { return displayName; }
    }

    public MainWindow(Requester requester, String username) {
        this.currentUsername = username;
        this.visualizationPanel = new VisualizationPanel(this);
        this.bandTable = new BandTable(this, visualizationPanel);
        this.commandHandler = new CommandHandler(requester, this, bandTable);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 750);
        setMinimumSize(new Dimension(900, 600));

        addLanguageItems();
        setLocaleIndex();
        addSetLocaleListener();

        setLayout(new BorderLayout());

        initHeaderPanel();
        initTableTabPanel();

        filterField.addCaretListener(e -> bandTable.refreshDataViews());
        columnFilterComboBox.addActionListener(e -> bandTable.refreshDataViews());

        tabbedPane.addTab("", visualizationPanel);
        add(tabbedPane, BorderLayout.CENTER);

        initSideBarPanel();

        addCmdListeners();

        updateTexts();

        new Timer(1500, e -> commandHandler.refreshCollectionFromServer()).start();
        new Timer(40, e -> visualizationPanel.repaint()).start();

        commandHandler.refreshCollectionFromServer();
    }

    /**
     * @param geoPos
     * Триггер добавления объекта с заданными координатами
     */
    public void triggerInsertWithDefaultCoords(GeoPosition geoPos) {
        int defaultX = (int) Math.round(geoPos.getLongitude());
        long defaultY = Math.round(geoPos.getLatitude());
        commandHandler.triggerInsertAt(defaultX, defaultY);
    }

    /**
     * Обновление надписей
     */
    private void updateTexts() {
        setTitle(LocalizationManager.getString("app_title"));
        userLabel.setText(LocalizationManager.getString("current_user") + currentUsername);
        logoutBtn.setText(LocalizationManager.getString("logout_btn"));

        tabbedPane.setTitleAt(0, LocalizationManager.getString("table_tab"));
        tabbedPane.setTitleAt(1, LocalizationManager.getString("vis_tab"));

        filterLabel.setText(LocalizationManager.getString("filter_lbl"));

        String currentSelection = getFilterColumn();

        updateColumnFilterComboBoxItems();

        if (currentSelection != null) {
            columnFilterComboBox.setSelectedItem(currentSelection);
        } else {
            columnFilterComboBox.setSelectedIndex(0);
        }

        bandTable.getTableHeader().repaint();

        setCmdTexts();

        bandTable.refreshDataViews();
    }

    public Integer getSelectedKey() { return selectedKey; }

    public void setSelectedKey(Integer selectedKey) {
        this.selectedKey = selectedKey;
    }

    /**
     * @param selectedKey
     * Изменение выбранного ключа
     */
    public void changeSelection(Integer selectedKey) {
        setSelectedKey(selectedKey);

        bandTable.repaint();
        visualizationPanel.repaint();

        if (selectedKey != null) {
            for (int r = 0; r < bandTable.getRowCount(); r++) {
                BandTableModel.BandRow row = bandTable.getTableModel().getRowAt(r);
                if (row != null && row.getKey() == selectedKey.intValue()) {
                    bandTable.setRowSelectionInterval(r, r);
                    bandTable.scrollRectToVisible(bandTable.getCellRect(r, 0, true));
                    break;
                }
            }
        } else {
            bandTable.clearSelection();
        }
    }

    /**
     * @param option
     * Вызов триггера изменения или удаления
     */
    public void handleOptionTrigger(int option) {
        if (option == 1) {
            commandHandler.triggerEditSelected();
        } else if (option == 2) {
            commandHandler.triggerDeleteSelected();
        }
    }

    /**
     * Добавление локалей
     */
    private void addLanguageItems() {
        languageComboBox.addItem(new LocaleItem(new Locale("en", "NZ"), "English (New Zealand)"));
        languageComboBox.addItem(new LocaleItem(new Locale("ru", "RU"), "Русский (Россия)"));
        languageComboBox.addItem(new LocaleItem(new Locale("cs", "CZ"), "Čeština (Česko)"));
        languageComboBox.addItem(new LocaleItem(new Locale("da", "DK"), "Dansk (Danmark)"));
    }

    /**
     * Установка индексов для локалей
     */
    private void setLocaleIndex() {
        for (int i = 0; i < languageComboBox.getItemCount(); i++) {
            if (languageComboBox.getItemAt(i).getLocale().getLanguage().equals(LocalizationManager.getCurrentLocale().getLanguage())) {
                languageComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Обработка смены локали
     */
    private void addSetLocaleListener() {
        languageComboBox.addActionListener(e -> {
            LocaleItem item = (LocaleItem) languageComboBox.getSelectedItem();
            if (item != null) {
                LocalizationManager.setLocale(item.getLocale());
                updateTexts();
            }
        });
    }

    /**
     * Инициализация главной панели
     */
    private void initHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        headerPanel.add(userLabel);
        headerPanel.add(languageComboBox);
        headerPanel.add(logoutBtn);
        add(headerPanel, BorderLayout.NORTH);
    }

    /**
     * Инициализация панели с таблицей
     */
    private void initTableTabPanel() {
        JPanel tableTabPanel = new JPanel(new BorderLayout(5, 5));
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.add(filterLabel);
        filterPanel.add(filterField);
        filterPanel.add(columnFilterComboBox);
        tableTabPanel.add(filterPanel, BorderLayout.NORTH);

        tableTabPanel.add(new JScrollPane(bandTable), BorderLayout.CENTER);

        tabbedPane.addTab("", tableTabPanel);
    }

    /**
     * Инициализация боковой панели
     */
    private void initSideBarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Dimension btnSize = new Dimension(220, 26);

        for (JButton b : getSideButtons()) {
            b.setMaximumSize(btnSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebarPanel.add(b);
            sidebarPanel.add(Box.createVerticalStrut(6));
        }

        add(new JScrollPane(sidebarPanel), BorderLayout.WEST);
    }

    /**
     * Добавление обработки триггеров по нажатиям на кнопки
     */
    private void addCmdListeners() {
        cmdHelp.addActionListener(e -> commandHandler.handleCommand("help", "", null));
        cmdInfo.addActionListener(e -> commandHandler.handleCommand("info", "", null));
        cmdAdd.addActionListener(e -> commandHandler.triggerInsert());
        cmdUpdate.addActionListener(e -> commandHandler.triggerEditSelected());
        cmdDelete.addActionListener(e -> commandHandler.triggerDeleteSelected());
        cmdClear.addActionListener(e -> commandHandler.triggerClear());
        cmdExecute.addActionListener(e -> commandHandler.triggerExecuteScript());
        cmdCountGreater.addActionListener(e -> commandHandler.triggerCountGreater());
        cmdRemoveGreater.addActionListener(e -> commandHandler.triggerRemoveGreater());
        cmdRemoveGreaterKey.addActionListener(e -> commandHandler.triggerRemoveGreaterKey());
        cmdReplaceGreater.addActionListener(e -> commandHandler.triggerReplaceIfGreater(currentUsername));
        cmdPrintAscending.addActionListener(e -> commandHandler.triggerShowAscending());
        cmdPrintDescPart.addActionListener(e -> commandHandler.handleCommand("print_field_descending_number_of_participants", "", null));
        logoutBtn.addActionListener(e -> {
            dispose();
            System.exit(0);
        });
    }

    /**
     * Обновление фильтров
     */
    private void updateColumnFilterComboBoxItems() {
        columnFilterComboBox.removeAllItems();
        columnFilterComboBox.addItem(LocalizationManager.getString("select_col"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_key"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_id"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_name"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_x"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_y"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_date"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_participants"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_genre"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_label_name"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_label_bands"));
        columnFilterComboBox.addItem(LocalizationManager.getString("col_owner"));
    }

    /**
     * Установка названий команд
     */
    private void setCmdTexts() {
        cmdHelp.setText(LocalizationManager.getString("cmd_help"));
        cmdInfo.setText(LocalizationManager.getString("cmd_info"));
        cmdAdd.setText(LocalizationManager.getString("cmd_add"));
        cmdUpdate.setText(LocalizationManager.getString("cmd_update"));
        cmdDelete.setText(LocalizationManager.getString("cmd_delete"));
        cmdClear.setText(LocalizationManager.getString("cmd_clear"));
        cmdExecute.setText(LocalizationManager.getString("cmd_execute"));
        cmdCountGreater.setText(LocalizationManager.getString("cmd_count_greater"));
        cmdRemoveGreater.setText(LocalizationManager.getString("cmd_remove_greater"));
        cmdRemoveGreaterKey.setText(LocalizationManager.getString("cmd_remove_greater_key"));
        cmdReplaceGreater.setText(LocalizationManager.getString("cmd_replace_greater"));
        cmdPrintAscending.setText(LocalizationManager.getString("cmd_print_ascending"));
        cmdPrintDescPart.setText(LocalizationManager.getString("cmd_print_desc_part"));
    }

    /**
     * @param editItem
     * Добавление триггера изменения по нажатию на кнопку
     */
    public void addEditItemTrigger(JMenuItem editItem) {
        editItem.addActionListener(al -> commandHandler.triggerEditSelected());
    }

    /**
     * @param deleteItem
     * Добавление триггера удаления по нажатию на кнопку
     */
    public void addDeleteItemTrigger(JMenuItem deleteItem) {
        deleteItem.addActionListener(al -> commandHandler.triggerDeleteSelected());
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getFilterColumn() {
        return (String) columnFilterComboBox.getSelectedItem();
    }

    public String getFilterText() {
        return filterField.getText();
    }

    private JButton[] getSideButtons() {
        return new JButton[] {
                cmdHelp, cmdInfo, cmdAdd, cmdUpdate, cmdDelete, cmdClear, cmdExecute,
                cmdCountGreater, cmdRemoveGreater, cmdRemoveGreaterKey, cmdReplaceGreater,
                cmdPrintAscending, cmdPrintDescPart
        };
    }
}