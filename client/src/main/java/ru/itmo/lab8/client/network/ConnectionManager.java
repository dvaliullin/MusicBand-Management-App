package ru.itmo.lab8.client.network;

import ru.itmo.lab8.common.util.Checker;

import java.io.*;
import java.net.Socket;

/**
 * Класс для управления соединением с сервером
 */
public class ConnectionManager {
    private final String host;
    private final String port;
    private Socket socket;

    public ConnectionManager(String host, String port) {
        this.host = host;
        this.port = port;
    }

    /**
     * @return true, если соединение установлено, иначе false
     */
    public boolean connect() {
        try {
            setSocket(new Socket());
            socket.connect(Checker.checkAddress(host, port), 5000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String getHost() {
        return this.host;
    }

    public String getPort() {
        return this.port;
    }

    /**
     * Запись объекта
     * @param obj
     * @throws IOException
     */
    public void writeObject(Object obj) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(obj);
        oos.flush();
    }

    /**
     * @return прочитанный объект
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object readObject() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        return ois.readObject();
    }

    /**
     * Закрытие соединения
     */
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}