package ru.itmo.lab8.client.gui.components.auth;

import javax.swing.*;
import java.awt.*;

/**
 * Панель для аутентификации
 */
public class AuthPanel extends JPanel {
    private final GridBagConstraints gbc = new GridBagConstraints();

    public AuthPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    /**
     * @param titleLabel
     * Добавить заголовок
     */
    public void addTitle(JLabel titleLabel) {
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);
    }

    /**
     * @param usernameLabel
     * @param usernameField
     * Добавить поле для ввода имени пользователя
     */
    public void addUsernameField(JLabel usernameLabel, JTextField usernameField) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(usernameLabel, gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);
    }

    /**
     * @param passwordLabel
     * @param passwordField
     * Добавить поле для ввода пароля
     */
    public void addPasswordField(JLabel passwordLabel, JTextField passwordField) {
        gbc.gridx = 0; gbc.gridy = 2;
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);
    }

    /**
     * @param languageLabel
     * @param languageComboBox
     * Добавить поле для выбора языка
     */
    public void addLanguageField(JLabel languageLabel, JComboBox languageComboBox) {
        gbc.gridx = 0; gbc.gridy = 3;
        add(languageLabel, gbc);
        gbc.gridx = 1;
        add(languageComboBox, gbc);
    }

    /**
     * @param btnPanel
     * Добавить панель с кнопками
     */
    public void addBtnPanel(JPanel btnPanel) {
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(btnPanel, gbc);
    }
}
