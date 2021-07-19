package me.brennan.divvy.utils;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class Logger {
    private static String timestamp() {
        String time = new SimpleDateFormat("[HH:mm:ss]").format(new Date());
        return "[" + Ansi.colorize(time, Attribute.BOLD()) + "] - ";
    }

    public static void success(String message) {
        message = Ansi.colorize(message, Attribute.GREEN_TEXT());
        System.out.println(timestamp() + message);
    }

    public static void warning(String message) {
        message = Ansi.colorize(message, Attribute.YELLOW_TEXT());
        System.out.println(timestamp() + message);
    }

    public static void info(String message) {
        message = Ansi.colorize(message, Attribute.BLUE_TEXT());
        System.out.println(timestamp() + message);
    }

    public static void error(String message) {
        message = Ansi.colorize(message, Attribute.RED_TEXT());
        System.out.println(timestamp() + message);
    }
}
