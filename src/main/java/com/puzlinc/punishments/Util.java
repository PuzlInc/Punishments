/*
* Punishments
* Copyright (C) 2014 Puzl Inc.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.puzlinc.punishments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public static String timestampToString(long time) {
        return time != -1 ? "until " + formatTimestamp(time) : "forever";
    }

    public static String formatTimestamp(long time) {
        return DATE_FORMAT.format(new Date(time));
    }

    public static long lengthToSeconds(String string) {
        if (string.equals("0") || string.equals("")) return 0;
        String[] lifeMatch = new String[]{ "d", "h", "m", "s" };
        int[] lifeInterval = new int[]{ 86400, 3600, 60, 1 };
        long seconds = 0L;

        for (int i=0;i<lifeMatch.length;i++) {
            Matcher matcher = Pattern.compile("([0-9]*)" + lifeMatch[i]).matcher(string);
            while (matcher.find()) {
                seconds += Integer.parseInt(matcher.group(1)) * lifeInterval[i];
            }

        }
        return seconds;
    }

    public static long lengthToMiliseconds(String string) {
        return lengthToSeconds(string) * 1000;
    }

    public static String argsToString(String[] args, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for (int i=start;i<end;i++) {
            builder.append(args);
            builder.append(" ");
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
}
