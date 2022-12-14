package com.github.aursu.souvenirs;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;

public class InputReader {
    private Scanner input;

    public InputReader() {
        this.input = new Scanner(System.in).useLocale(Locale.US);
    }

    public int readNumber(String prompt) {
        return (int) readNumber(prompt, false, false);
    }

    public Number readNumber(String prompt, boolean isFloat) {
        return readNumber(prompt, isFloat, false);
    }

    public int readNumber(String prompt, int current) {
        Number newNmr = readNumber(String.format("%s [%s]: ", prompt, current), false,  true);
        if (newNmr == null) return current;
        return (int) newNmr;
    }

    public double readNumber(String prompt, double current) {
        Number newNmr = readNumber(String.format("%s [%s]: ", prompt, current), true,  true);
        if (newNmr == null) return current;
        return (double) newNmr;
    }

    public Number readNumber(String prompt, boolean isFloat, boolean acceptEmpty) {
        Number num;
        while (true) {
            System.out.print(prompt);
            String inp = input.nextLine();
            try {
                if (isFloat)
                    num = Double.parseDouble(inp);
                else
                    num = Integer.parseInt(inp);

                return num;
            } catch (NumberFormatException e) {
                if (acceptEmpty && inp.isEmpty()) return null;
            }
        }
    }

    public String readString(String prompt) {
        return readString(prompt, false);
    }

    /**
     * @param prompt  Prompt string (what value do we read)
     * @param current Current value of this string
     * @return Current value if input is empty, otherwise new value
     */
    public String readString(String prompt, String current) {
        String newStr = readString(String.format("%s [%s]: ", prompt, current), true);
        if (newStr.isEmpty())
            return current;
        return newStr;
    }

    /**
     * @param prompt      Prompt string (what value do we read)
     * @param acceptEmpty Whether we accept empty values or not
     * @return String value from standard input
     */
    public String readString(String prompt, boolean acceptEmpty) {
        String str;
        while (true) {
            System.out.print(prompt);
            str = input.nextLine();
            // either we accept empty strings or read value is not empty
            if (acceptEmpty || !str.isEmpty()) break;
        }
        return str;
    }

    private LocalDate readDate(String prompt, boolean acceptEmpty) {
        LocalDate dt = null;
        while (true) {
            System.out.printf(prompt);
            String str = input.nextLine();
            try {
                dt = LocalDate.parse(str, DateTimeFormatter.ISO_LOCAL_DATE);
                return dt;
            } catch (DateTimeParseException e) {}

            // either we accept empty strings or read value is not empty
            if (acceptEmpty && str.isEmpty()) break;
        }
        return dt;
    }

    public LocalDate readDate(String prompt) {
        return readDate(String.format("%s [yyyy-MM-dd]: ", prompt), false);
    }

    public LocalDate readDate(String prompt, LocalDate current) {
        LocalDate newDate = readDate(String.format("%s [%s]: ", prompt, DataObject.jsonDateTime(current)), true);
        if (newDate == null)
            return current;
        return newDate;
    }
}
