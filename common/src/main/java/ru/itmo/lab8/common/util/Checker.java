package ru.itmo.lab8.common.util;

import java.net.InetSocketAddress;
import java.nio.channels.UnresolvedAddressException;

/**
 * Класс для проверки адресов
 */
public final class Checker {
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    /**
     * Проверка порта
     * @param strPort
     * @return
     * @throws UnresolvedAddressException
     */
    public static int checkPort(String strPort) throws UnresolvedAddressException {
        try {
            final int port = Integer.parseInt(strPort);
            if (port < MIN_PORT || port > MAX_PORT) {
                return DEFAULT_PORT;
            } else {
                return port;
            }
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    /**
     * Проверка хоста
     * @param host
     * @return
     */
    public static String checkHost(String host) {
        return host == null ? DEFAULT_HOST : host;
    }

    /**
     * Проверка адреса
     * @param host
     * @param port
     * @return
     * @throws UnresolvedAddressException
     */
    public static InetSocketAddress checkAddress(String host, String port) throws UnresolvedAddressException {
        InetSocketAddress address = new InetSocketAddress(checkHost(host), checkPort(port));
        if (address.isUnresolved()) {
            throw new UnresolvedAddressException();
        }
        return address;
    }
}
