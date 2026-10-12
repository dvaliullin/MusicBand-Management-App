package ru.itmo.lab8.client.gui.components.main;

import ru.itmo.lab8.client.gui.util.LocalizationManager;
import ru.itmo.lab8.common.data.MusicBand;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Модель таблицы объектов
 */
public class BandTableModel extends AbstractTableModel {
    private final MainWindow mainWindow;
    private final List<BandRow> allData = new ArrayList<>();
    private List<BandRow> displayData = new ArrayList<>();
    private final int columnCount = 12;

    /**
     * Вложенный класс для строк таблицы
     */
    public static class BandRow {
        private final int key;
        private final MusicBand band;

        public BandRow(int key, MusicBand band) {
            this.key = key;
            this.band = band;
        }

        public int getKey() {
            return key;
        }

        public MusicBand getBand() {
            return band;
        }
    }

    public BandTableModel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * @param map
     * Обновление данных
     */
    public synchronized void updateData(Map<Integer, MusicBand> map) {
        allData.clear();
        if (map != null) {
            map.forEach((k, v) -> allData.add(new BandRow(k, v)));
        }
    }

    /**
     * @param filterText
     * @param filterColumn
     * @param sortCol
     * @param ascending
     * Обработка фильтра и сортировка
     */
    public synchronized void applyFilterAndSort(String filterText, String filterColumn, int sortCol, boolean ascending) {
        String query = filterText.toLowerCase().trim();

        setDisplayData(getFilteredData(query, filterColumn));

        if (sortCol >= 1) {
            Comparator<BandRow> comp = getComparatorForColumn(sortCol);
            if (comp != null) {
                if (!ascending) comp = comp.reversed();
                setDisplayData(displayData.stream().sorted(comp).collect(Collectors.toList()));
            }
        }

        fireTableDataChanged();
    }

    /**
     * @param query
     * @param filterColumn
     * @return отсортированные данные
     */
    private List<BandRow> getFilteredData(String query, String filterColumn) {
        return allData.stream()
                .filter(row -> {
                    if (query.isEmpty()) return true;
                    if (filterColumn.equals(LocalizationManager.getString("select_col")) || filterColumn.equals("All Columns")) {
                        return rowMatchesQuery(row, query);
                    } else {
                        return singleColMatches(row, filterColumn, query);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * @param row
     * @param q
     * @return true, если найдена совпадающая строка
     */
    private boolean rowMatchesQuery(BandRow row, String q) {
        MusicBand b = row.getBand();
        return String.valueOf(row.getKey()).contains(q)
                || String.valueOf(b.getId()).contains(q)
                || b.getName().toLowerCase().contains(q)
                || String.valueOf(b.getCoordinates().getX()).contains(q)
                || String.valueOf(b.getCoordinates().getY()).contains(q)
                || LocalizationManager.formatDate(b.getCreationDate()).toLowerCase().contains(q)
                || (b.getNumberOfParticipants() != null && String.valueOf(b.getNumberOfParticipants()).contains(q))
                || (b.getGenre() != null && b.getGenre().name().toLowerCase().contains(q))
                || (b.getLabel() != null && b.getLabel().getName().toLowerCase().contains(q))
                || (b.getLabel() != null && String.valueOf(b.getLabel().getBands()).contains(q))
                || (b.getOwner() != null && b.getOwner().toLowerCase().contains(q));
    }

    /**
     * @param row
     * @param colName
     * @param q
     * @return true, если найден совпадающий столбец
     */
    private boolean singleColMatches(BandRow row, String colName, String q) {
        MusicBand b = row.getBand();
        if (colName.equals(LocalizationManager.getString("col_key"))) return String.valueOf(row.getKey()).contains(q);
        if (colName.equals(LocalizationManager.getString("col_id"))) return String.valueOf(b.getId()).contains(q);
        if (colName.equals(LocalizationManager.getString("col_name"))) return b.getName().toLowerCase().contains(q);
        if (colName.equals(LocalizationManager.getString("col_x")))
            return String.valueOf(b.getCoordinates().getX()).contains(q);
        if (colName.equals(LocalizationManager.getString("col_y")))
            return String.valueOf(b.getCoordinates().getY()).contains(q);
        if (colName.equals(LocalizationManager.getString("col_date")))
            return LocalizationManager.formatDate(b.getCreationDate()).toLowerCase().contains(q);
        if (colName.equals(LocalizationManager.getString("col_participants")))
            return b.getNumberOfParticipants() != null && String.valueOf(b.getNumberOfParticipants()).contains(q);
        if (colName.equals(LocalizationManager.getString("col_genre")))
            return b.getGenre() != null && b.getGenre().name().toLowerCase().contains(q);
        if (colName.equals(LocalizationManager.getString("col_label_name")))
            return b.getLabel() != null && b.getLabel().getName().toLowerCase().contains(q);
        if (colName.equals(LocalizationManager.getString("col_label_bands")))
            return b.getLabel() != null && String.valueOf(b.getLabel().getBands()).contains(q);
        if (colName.equals(LocalizationManager.getString("col_owner")))
            return b.getOwner() != null && b.getOwner().toLowerCase().contains(q);
        return false;
    }

    /**
     * @param col
     * @return фильтр для столбца
     */
    private Comparator<BandRow> getComparatorForColumn(int col) {
        return switch (col) {
            case 1 -> Comparator.comparingInt(BandRow::getKey);
            case 2 -> Comparator.comparing(r -> r.getBand().getId());
            case 3 -> Comparator.comparing(r -> r.getBand().getName().toLowerCase());
            case 4 -> Comparator.comparingInt(r -> r.getBand().getCoordinates().getX());
            case 5 -> Comparator.comparing(r -> r.getBand().getCoordinates().getY());
            case 6 -> Comparator.comparing(r -> r.getBand().getCreationDate());
            case 7 -> Comparator.comparing(r -> r.getBand().getNumberOfParticipants(), Comparator.nullsLast(Comparator.naturalOrder()));
            case 8 -> Comparator.comparing(r -> r.getBand().getGenre() != null ? r.getBand().getGenre().name() : "");
            case 9 -> Comparator.comparing(r -> r.getBand().getLabel() != null ? r.getBand().getLabel().getName().toLowerCase() : "");
            case 10 -> Comparator.comparingLong(r -> r.getBand().getLabel() != null ? r.getBand().getLabel().getBands() : 0L);
            case 11 -> Comparator.comparing(r -> r.getBand().getOwner() != null ? r.getBand().getOwner().toLowerCase() : "");
            default -> null;
        };
    }

    @Override
    public int getRowCount() {
        return displayData.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Boolean.class;
        }
        return Object.class;
    }

    /**
     * @param column the column being queried
     * @return название столбца
     */
    @Override
    public String getColumnName(int column) {
        if (column == 0) return "";
        return switch (column) {
            case 1 -> LocalizationManager.getString("col_key");
            case 2 -> LocalizationManager.getString("col_id");
            case 3 -> LocalizationManager.getString("col_name");
            case 4 -> LocalizationManager.getString("col_x");
            case 5 -> LocalizationManager.getString("col_y");
            case 6 -> LocalizationManager.getString("col_date");
            case 7 -> LocalizationManager.getString("col_participants");
            case 8 -> LocalizationManager.getString("col_genre");
            case 9 -> LocalizationManager.getString("col_label_name");
            case 10 -> LocalizationManager.getString("col_label_bands");
            case 11 -> LocalizationManager.getString("col_owner");
            default -> "";
        };
    }

    /**
     * @param rowIndex    the row whose value is to be queried
     * @param columnIndex the column whose value is to be queried
     * @return значение по номеру строки и номеру столбца
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= displayData.size()) return null;
        BandRow row = displayData.get(rowIndex);
        MusicBand b = row.getBand();

        if (columnIndex == 0) {
            return mainWindow.getSelectedKey() != null && mainWindow.getSelectedKey().equals(row.getKey());
        }

        return getValueOfColumn(columnIndex, row, b);
    }

    /**
     * @param columnIndex
     * @param row
     * @param b
     * @return значение в столбце
     */
    private Object getValueOfColumn(int columnIndex, BandRow row, MusicBand b) {
        return switch (columnIndex) {
            case 1 -> LocalizationManager.formatNumber(row.getKey());
            case 2 -> b.getId();
            case 3 -> b.getName();
            case 4 -> LocalizationManager.formatNumber(b.getCoordinates().getX());
            case 5 -> LocalizationManager.formatNumber(b.getCoordinates().getY());
            case 6 -> LocalizationManager.formatDate(b.getCreationDate());
            case 7 -> b.getNumberOfParticipants() != null ? LocalizationManager.formatNumber(b.getNumberOfParticipants()) : "";
            case 8 -> b.getGenre() != null ? b.getGenre().name() : "";
            case 9 -> b.getLabel() != null ? b.getLabel().getName() : "";
            case 10 -> b.getLabel() != null ? LocalizationManager.formatNumber(b.getLabel().getBands()) : "";
            case 11 -> b.getOwner();
            default -> null;
        };
    }

    /**
     * @param rowIndex
     * @return строку по номеру
     */
    public synchronized BandRow getRowAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= displayData.size()) return null;
        return displayData.get(rowIndex);
    }

    public void setDisplayData(List<BandRow> displayData) {
        this.displayData = displayData;
    }
}