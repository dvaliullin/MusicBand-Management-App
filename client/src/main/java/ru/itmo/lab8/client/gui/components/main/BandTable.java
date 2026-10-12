package ru.itmo.lab8.client.gui.components.main;

import ru.itmo.lab8.client.gui.components.visualization.VisualizationPanel;
import ru.itmo.lab8.common.data.MusicBand;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

/**
 * Класс для управления таблицей объектов
 */
public class BandTable extends JTable {
    private final MainWindow mainWindow;
    private final VisualizationPanel visualizationPanel;
    private final BandTableModel tableModel;
    private boolean isRefreshing = false;
    private int currentSortColumn = -1;
    private boolean ascending = true;
    private Map<Integer, MusicBand> elementsMap = new TreeMap<>();

    public BandTable(MainWindow mainWindow, VisualizationPanel visualizationPanel) {
        this.mainWindow = mainWindow;
        tableModel = new BandTableModel(mainWindow);
        this.visualizationPanel = visualizationPanel;
        setModel(tableModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTableHeader().setReorderingAllowed(false);
        setSelectionBackground(getBackground());
        setSelectionForeground(getForeground());

        getColumnModel().getColumn(0).setPreferredWidth(30);
        getColumnModel().getColumn(0).setMaxWidth(35);
        getColumnModel().getColumn(0).setMinWidth(25);

        addSelectingListener();
        addClickListener();
        addHeaderListener();
    }

    /**
     * Обработка выбора объекта в таблице
     */
    private void addSelectingListener() {
        getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isRefreshing) {
                int row = getSelectedRow();
                if (row >= 0) {
                    BandTableModel.BandRow rowData = tableModel.getRowAt(row);
                    if (rowData != null) {
                        mainWindow.setSelectedKey(rowData.getKey());
                        repaint();
                        visualizationPanel.repaint();
                    }
                } else {
                    mainWindow.setSelectedKey(null);
                    repaint();
                    visualizationPanel.repaint();
                }
            }
        });
    }

    /**
     * Обработка нажатия на объект в таблице
     */
    public void addClickListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row >= 0) {
                    BandTableModel.BandRow rowData = tableModel.getRowAt(row);
                    if (rowData != null) {
                        if (mainWindow.getSelectedKey() != null && mainWindow.getSelectedKey().equals(rowData.getKey())) {
                            SwingUtilities.invokeLater(() -> {
                                clearSelection();
                                mainWindow.setSelectedKey(null);
                            });
                        }
                    }
                } else {
                    clearSelection();
                    mainWindow.setSelectedKey(null);
                }
            }
        });
    }

    /**
     * Обработка сортировки по конкретному столбцу
     */
    public void addHeaderListener() {
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = columnAtPoint(e.getPoint());
                if (col == currentSortColumn) {
                    setAscending(!ascending);
                } else {
                    setCurrentSortColumn(col);
                    setAscending(true);
                }
                refreshDataViews();
            }
        });
    }

    /**
     * Обновление таблицы для получения актуальных данных
     */
    public synchronized void refreshDataViews() {
        setRefreshing(true);
        try {
            tableModel.updateData(elementsMap);

            String filterText = mainWindow.getFilterText();
            String filterColumn = mainWindow.getFilterColumn();
            if (filterColumn == null) {
                filterColumn = "All Columns";
            }

            tableModel.applyFilterAndSort(filterText, filterColumn, currentSortColumn, ascending);

            restoreTableSelection();
        } finally {
            setRefreshing(false);
        }

        visualizationPanel.setElements(elementsMap);
    }

    /**
     * Восстановить выделение строки таблицы
     */
    private void restoreTableSelection() {
        if (mainWindow.getSelectedKey() != null) {
            int rowToSelect = findRowWithMatch();
            if (rowToSelect >= 0) {
                restoreEDTSelection(rowToSelect);
            } else {
                mainWindow.setSelectedKey(null);
            }
        }
    }

    /**
     * @return номер выделенной строки
     */
    private int findRowWithMatch() {
        int rowToSelect = -1;
        for (int r = 0; r < getRowCount(); r++) {
            BandTableModel.BandRow row = tableModel.getRowAt(r);
            if (row != null && row.getKey() == mainWindow.getSelectedKey().intValue()) {
                rowToSelect = r;
                break;
            }
        }
        return rowToSelect;
    }

    /**
     * @param finalRow
     * Восстановить выделение строки в потоке EDT
     */
    private void restoreEDTSelection(int finalRow) {
        SwingUtilities.invokeLater(() -> {
            if (getSelectedRow() != finalRow) {
                setRowSelectionInterval(finalRow, finalRow);
            }
        });
    }

    public void setElementsMap(Map<Integer, MusicBand> elementsMap) {
        this.elementsMap = elementsMap;
    }

    /**
     * @return объект с выбранным ключом
     */
    public MusicBand getBySelectedKey() {
        return elementsMap.get(mainWindow.getSelectedKey());
    }

    public void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public void setCurrentSortColumn(int currentSortColumn) {
        this.currentSortColumn = currentSortColumn;
    }

    public BandTableModel getTableModel() {
        return tableModel;
    }
}
