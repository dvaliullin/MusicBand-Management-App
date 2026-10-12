package ru.itmo.lab8.client.gui.components.main;

import ru.itmo.lab8.client.gui.util.LocalizationManager;
import ru.itmo.lab8.client.gui.util.Validator;
import ru.itmo.lab8.common.data.*;
import ru.itmo.lab8.common.data.Label;

import javax.swing.*;
import java.awt.*;

/**
 * Класс для заполнения полей объекта MusicBand
 */
public class BandFormDialog extends JDialog {
    private boolean saved = false;
    private Integer resultKey;
    private MusicBand resultBand;
    private final Validator validator = new Validator();

    private final JLabel keyLabel = new JLabel();
    public final JTextField keyField = new JTextField(15);

    private final JLabel nameLabel = new JLabel();
    private final JTextField nameField = new JTextField(15);

    private final JLabel xLabel = new JLabel();
    private final JTextField xField = new JTextField(15);

    private final JLabel yLabel = new JLabel();
    private final JTextField yField = new JTextField(15);

    private final JLabel participantsLabel = new JLabel();
    private final JTextField participantsField = new JTextField(15);

    private final JLabel genreLabel = new JLabel();
    private final JComboBox<MusicGenre> genreComboBox = new JComboBox<>();

    private final JLabel labelNameLabel = new JLabel();
    private final JTextField labelNameField = new JTextField(15);

    private final JLabel labelBandsLabel = new JLabel();
    private final JTextField labelBandsField = new JTextField(15);

    private final JButton cancelBtn = new JButton();
    private final JButton saveBtn = new JButton();

    public BandFormDialog(Window parent, Integer existingKey, MusicBand existingBand, Integer defaultX, Long defaultY) {
        super(parent, ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout(10, 10));
        initMainPanel();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        add(btnPanel, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> validateAndSave(existingBand != null));

        if (existingBand != null) {
            setEditMode(existingKey, existingBand);
        } else {
            if (defaultX != null) xField.setText(defaultX.toString());
            if (defaultY != null) yField.setText(defaultY.toString());
        }

        updateTexts();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * @param isEditMode
     * Валидация и сохранение объекта
     */
    private void validateAndSave(boolean isEditMode) {
        try {
            Integer keyVal = null;
            if (!isEditMode) {
                keyVal = validator.validateKey(keyField);
            }

            String name = validator.validateName(nameField);
            Coordinates coordinates = validator.validateCoordinates(xField, yField);
            Integer participants = validator.validateParticipants(participantsField);
            MusicGenre genre = (MusicGenre) genreComboBox.getSelectedItem();
            Label label = validator.validateLabel(labelNameField, labelBandsField);

            setResultKey(keyVal);
            setResultBand(new MusicBand(name, coordinates, participants, genre, label));
            setSaved(true);
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    LocalizationManager.getString("number_format_exception"),
                    LocalizationManager.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    LocalizationManager.getString("invalid_input") + " (" + e.getMessage() + ")",
                    LocalizationManager.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Обновление надписей
     */
    private void updateTexts() {
        setTitle(LocalizationManager.getString("dialog_band_title"));
        keyLabel.setText(LocalizationManager.getString("label_key"));
        nameLabel.setText(LocalizationManager.getString("label_name_field"));
        xLabel.setText("X (int):");
        yLabel.setText("Y (Long):");
        participantsLabel.setText(LocalizationManager.getString("label_participants_field"));
        genreLabel.setText(LocalizationManager.getString("label_genre_field"));
        labelNameLabel.setText(LocalizationManager.getString("label_label_name"));
        labelBandsLabel.setText(LocalizationManager.getString("label_label_bands"));
        cancelBtn.setText(LocalizationManager.getString("cancel_btn"));
        saveBtn.setText(LocalizationManager.getString("save_btn"));
    }

    /**
     * Добавление выбора MusicGenre
     */
    private void addMusicGenreItems() {
        genreComboBox.addItem(null);
        for (MusicGenre g : MusicGenre.values()) {
            genreComboBox.addItem(g);
        }
    }

    /**
     * Инициализация главной панели
     */
    private void initMainPanel() {
        BandFormPanel panel = new BandFormPanel(7);
        addMusicGenreItems();
        panel.addComponent(keyLabel, keyField);
        panel.addComponent(nameLabel, nameField);
        panel.addComponent(xLabel, xField);
        panel.addComponent(yLabel, yField);
        panel.addComponent(participantsLabel, participantsField);
        panel.addComponent(genreLabel, genreComboBox);
        panel.addComponent(labelNameLabel, labelNameField);
        panel.addComponent(labelBandsLabel, labelBandsField);
        add(panel, BorderLayout.CENTER);
    }

    /**
     * @param existingKey
     * @param existingBand
     * Установить режим редактирования
     */
    private void setEditMode(Integer existingKey, MusicBand existingBand) {
        keyField.setText(existingKey.toString());
        keyField.setEnabled(false);
        nameField.setText(existingBand.getName());
        xField.setText(String.valueOf(existingBand.getCoordinates().getX()));
        yField.setText(String.valueOf(existingBand.getCoordinates().getY()));
        participantsField.setText(existingBand.getNumberOfParticipants() != null ? existingBand.getNumberOfParticipants().toString() : "");
        genreComboBox.setSelectedItem(existingBand.getGenre());
        if (existingBand.getLabel() != null) {
            labelNameField.setText(existingBand.getLabel().getName());
            labelBandsField.setText(String.valueOf(existingBand.getLabel().getBands()));
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Integer getResultKey() {
        return resultKey;
    }

    public MusicBand getResultBand() {
        return resultBand;
    }

    public void setResultKey(Integer resultKey) {
        this.resultKey = resultKey;
    }

    public void setResultBand(MusicBand resultBand) {
        this.resultBand = resultBand;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

}