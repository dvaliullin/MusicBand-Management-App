package ru.itmo.lab8.client.gui.components.auth;

import ru.itmo.lab8.client.gui.util.LocalizationManager;
import ru.itmo.lab8.client.network.Requester;
import ru.itmo.lab8.common.util.AuthRequest;
import ru.itmo.lab8.common.util.AuthResponse;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * Класс для обработки аутентификации
 */
public class AuthDialog extends JDialog {
    private final Requester requester;
    private String username;
    private boolean succeeded = false;

    private final JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel usernameLabel = new JLabel();
    private final JLabel passwordLabel = new JLabel();
    private final JLabel languageLabel = new JLabel();

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JComboBox<LocaleItem> languageComboBox = new JComboBox<>();

    private final JButton loginBtn = new JButton();
    private final JButton registerBtn = new JButton();

    /**
     * Вложенный класс для выбора локали
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

    public AuthDialog(Frame parent, Requester requester) {
        super(parent, true);
        this.requester = requester;

        initLanguageComboBox();

        addLanguageChangeListener();

        languageComboBox.setSelectedIndex(0);

        setLayout(new BorderLayout(10, 10));

        initContentPanel();

        loginBtn.addActionListener(e -> handleAuth(false));
        registerBtn.addActionListener(e -> handleAuth(true));

        updateTexts();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * @param isRegister
     * Обработка аутентификации
     */
    private void handleAuth(boolean isRegister) {
        String user = usernameField.getText().trim();
        String pwd = new String(passwordField.getPassword()).trim();

        if (user.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    LocalizationManager.getString("invalid_input"),
                    LocalizationManager.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        processAuth(user, pwd, isRegister);
    }

    /**
     * Обновление надписей
     */
    private void updateTexts() {
        setTitle(LocalizationManager.getString("login_title"));
        titleLabel.setText(LocalizationManager.getString("login_title"));
        usernameLabel.setText(LocalizationManager.getString("username") + ":");
        passwordLabel.setText(LocalizationManager.getString("password") + ":");
        languageLabel.setText(LocalizationManager.getString("search_lbl"));
        loginBtn.setText(LocalizationManager.getString("login_btn"));
        registerBtn.setText(LocalizationManager.getString("register_btn"));
    }

    /**
     * Инициализация доступных локалей
     */
    private void initLanguageComboBox() {
        languageComboBox.addItem(new LocaleItem(new Locale("en", "NZ"), "English (New Zealand)"));
        languageComboBox.addItem(new LocaleItem(new Locale("ru", "RU"), "Русский (Россия)"));
        languageComboBox.addItem(new LocaleItem(new Locale("cs", "CZ"), "Čeština (Česko)"));
        languageComboBox.addItem(new LocaleItem(new Locale("da", "DK"), "Dansk (Danmark)"));
    }

    /**
     * Изменение языка при смене локали
     */
    private void addLanguageChangeListener() {
        languageComboBox.addActionListener(e -> {
            LocaleItem selected = (LocaleItem) languageComboBox.getSelectedItem();
            if (selected != null) {
                LocalizationManager.setLocale(selected.getLocale());
                updateTexts();
            }
        });
    }

    /**
     * Инициализация главной панели
     */
    private void initContentPanel() {
        AuthPanel contentPanel = new AuthPanel();

        contentPanel.addTitle(titleLabel);
        contentPanel.addUsernameField(usernameLabel, usernameField);
        contentPanel.addPasswordField(passwordLabel, passwordField);
        contentPanel.addLanguageField(languageLabel, languageComboBox);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        contentPanel.addBtnPanel(btnPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * @param user
     * @param pwd
     * @param isRegister
     * Создание запроса аутентификации
     */
    private void processAuth(String user, String pwd, boolean isRegister) {
        try {
            AuthRequest request = new AuthRequest(user, pwd, isRegister);
            AuthResponse response = requester.sendAuthRequest(request);

            if (response.isSuccess()) {
                handleSuccess(user);
            } else {
                handleError(response, user);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    LocalizationManager.getString("cannot_connect") + ": " + e.getMessage(),
                    LocalizationManager.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @param user
     * Обработка успешной авторизации
     */
    private void handleSuccess(String user) {
        setUsername(user);
        setSucceeded(true);
        JOptionPane.showMessageDialog(this,
                LocalizationManager.getString("successful_auth"),
                LocalizationManager.getString("success"),
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    /**
     * @param response
     * @param user
     * Обработка ошибок при авторизации
     */
    private void handleError(AuthResponse response, String user) {
        String errorKey = "auth_error";
        if (response.getMessage().equals("Ошибка регистрации: " + "Пользователь " + user + " уже существует")) {
            errorKey = "user_already_exists";
        }

        JOptionPane.showMessageDialog(this,
                LocalizationManager.getString(errorKey),
                LocalizationManager.getString("error"),
                JOptionPane.ERROR_MESSAGE);
    }

    public boolean isSucceeded() { return succeeded; }

    public String getUsername() { return username; }

    public void setUsername(String username) {
        this.username = username;
    }

    private void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }
}