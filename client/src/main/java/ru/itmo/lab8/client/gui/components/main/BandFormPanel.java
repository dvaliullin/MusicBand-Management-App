package ru.itmo.lab8.client.gui.components.main;

import javax.swing.*;
import java.awt.*;

/**
 * Панель с формой для заполнения полей объекта MusicBand
 */
public class BandFormPanel extends JPanel {
    private final GridBagConstraints gbc = new GridBagConstraints();
    private int row = 0;
    private final int rowsNumber;

    public BandFormPanel(int rowsNumber) {
        this.rowsNumber = rowsNumber;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    /**
     * @param label
     * @param component
     * Добавление компонента
     */
    public void addComponent(JLabel label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        add(label, gbc);
        gbc.gridx = 1;
        add(component, gbc);
        if (row < rowsNumber) {
            row++;
        }
    }
}
