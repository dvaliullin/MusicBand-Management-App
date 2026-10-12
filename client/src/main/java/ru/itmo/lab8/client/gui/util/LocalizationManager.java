package ru.itmo.lab8.client.gui.util;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Класс для локализации
 */
public final class LocalizationManager {
    private static Locale currentLocale = new Locale("en", "NZ");
    private static ResourceBundle currentBundle = ResourceBundle.getBundle("ru.itmo.lab8.client.localization.ResourceBundle", currentLocale);

    public static Locale getCurrentLocale() { return currentLocale; }

    /**
     * @param locale
     * Установить локаль
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        Locale.setDefault(locale);
        currentBundle = ResourceBundle.getBundle("ru.itmo.lab8.client.localization.ResourceBundle", currentLocale);
    }

    /**
     * @param key
     * @return строку локали по ключу
     */
    public static String getString(String key) {
        try {
            return currentBundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    /**
     * @param number
     * @return отформатированное число
     */
    public static String formatNumber(Number number) {
        if (number == null) {
            return "";
        }
        return NumberFormat.getInstance(currentLocale).format(number);
    }

    /**
     * @param dateTime
     * @return отформатированную дату
     */
    public static String formatDate(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(currentLocale);
        return dateTime.format(formatter);
    }
}