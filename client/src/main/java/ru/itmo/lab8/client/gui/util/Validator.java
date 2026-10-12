package ru.itmo.lab8.client.gui.util;

import ru.itmo.lab8.common.data.Coordinates;
import ru.itmo.lab8.common.data.Label;

import javax.swing.*;

/**
 * Класс для валидации полей объекта
 */
public class Validator {
    /**
     * @param keyField
     * @return проверенный ключ
     */
    public Integer validateKey(JTextField keyField) {
        String strKey = keyField.getText().trim();
        if (strKey.isEmpty()) {
            throw new IllegalArgumentException(LocalizationManager.getString("empty_key_error"));
        }
        Integer keyVal = Integer.parseInt(strKey);
        if (keyVal <= 0) {
            throw new IllegalArgumentException(LocalizationManager.getString("key_input_error"));
        }
        return keyVal;
    }

    /**
     * @param nameField
     * @return проверенное имя
     */
    public String validateName(JTextField nameField) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException(LocalizationManager.getString("name_input_error"));
        }
        return name;
    }

    /**
     * @param xField
     * @param yField
     * @return проверенные координаты
     */
    public Coordinates validateCoordinates(JTextField xField, JTextField yField) {
        int x = Integer.parseInt(xField.getText().trim());
        Long y = Long.parseLong(yField.getText().trim());
        return new Coordinates(x, y);
    }

    /**
     * @param labelNameField
     * @param labelBandsField
     * @return проверенный лейбл
     */
    public Label validateLabel(JTextField labelNameField, JTextField labelBandsField) {
        String labelName = labelNameField.getText().trim();
        long labelBands = Long.parseLong(labelBandsField.getText().trim());
        if (labelBands < 0) {
            throw new IllegalArgumentException(LocalizationManager.getString("bands_input_error"));
        }
        return new Label(labelName, labelBands);
    }

    /**
     * @param participantsField
     * @return проверенное число участников
     */
    public Integer validateParticipants(JTextField participantsField) {
        String partText = participantsField.getText().trim();
        Integer participants = null;
        if (!partText.isEmpty()) {
            participants = Integer.parseInt(partText);
            if (participants <= 0) {
                throw new IllegalArgumentException(LocalizationManager.getString("participants_input_error"));
            }
        }
        return participants;
    }
}
