package ru.itmo.lab8.server.network;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Класс для получения запросов
 */
public class Receiver {
    private final SocketChannel socketChannel;
    private static final int BUFFER_SIZE = 65535;
    private ByteArrayOutputStream pendingData = new ByteArrayOutputStream();
    private boolean eof = false;

    public Receiver(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    /**
     * @return полученный объект
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object receiveObject() throws IOException, ClassNotFoundException {
        byte[] data = readBuffer();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Object obj = ois.readObject();
            pendingData.reset();
            return obj;
        } catch (EOFException | StreamCorruptedException | OptionalDataException e) {
            return null;
        }
    }

    /**
     * @return массив байтов из буфера
     * @throws IOException
     */
    public byte[] readBuffer() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead = socketChannel.read(buffer);

        if (bytesRead == -1) {
            setEof(true);
            return null;
        }

        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            pendingData.write(data);
        }

        byte[] allData = pendingData.toByteArray();
        return allData.length == 0 ? null : allData;
    }

    /**
     * Отправить объект
     * @param obj
     * @throws IOException
     */
    public void sendObject(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();

        byte[] data = bos.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(data);

        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    public boolean isEof() {
        return eof;
    }

    public void setEof(boolean eof) {
        this.eof = eof;
    }
}