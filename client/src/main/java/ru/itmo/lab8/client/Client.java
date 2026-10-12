package ru.itmo.lab8.client;

import ru.itmo.lab8.client.gui.components.auth.AuthDialog;
import ru.itmo.lab8.client.gui.components.main.MainWindow;
import ru.itmo.lab8.client.network.ConnectionManager;
import ru.itmo.lab8.client.network.Requester;

import javax.swing.*;

public class Client {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        String host = System.getenv("HOST");
        String port = System.getenv("PORT");
        if (host == null || host.isEmpty()) host = "localhost";
        if (port == null || port.isEmpty()) port = "8888";

        final String finalHost = host;
        final String finalPort = port;

        SwingUtilities.invokeLater(() -> {
            ConnectionManager connectionManager = new ConnectionManager(finalHost, finalPort);
            Requester requester = new Requester(connectionManager);

            AuthDialog loginDialog = new AuthDialog(null, requester);
            loginDialog.setVisible(true);

            if (loginDialog.isSucceeded()) {
                String username = loginDialog.getUsername();
                MainWindow mainWindow = new MainWindow(requester, username);
                mainWindow.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}