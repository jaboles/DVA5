package jb.common;

import java.util.List;
import java.util.regex.Pattern;

public class StringUtilities
{
    public static String join(String delimiter, String[] strings)
    {
        StringBuilder sb = new StringBuilder();
        if (strings.length > 0)
        {
            sb.append(strings[0]);
            if (strings.length > 1)
            {
                for (int i = 1; i < strings.length; i++)
                {
                    sb.append(delimiter);
                    sb.append(strings[i]);
                }
            }
        }
        return sb.toString();
    }

    public static String join(String delimiter, List<String> strings)
    {
        StringBuilder sb = new StringBuilder();
        if (strings.size() > 0)
        {
            sb.append(strings.get(0));
            if (strings.size() > 1)
            {
                for (int i = 1; i < strings.size(); i++)
                {
                    sb.append(delimiter);
                    sb.append(strings.get(i));
                }
            }
        }
        return sb.toString();
    }

    public static boolean containsIgnoreCase(String haystack, String needle)
    {
        return Pattern.compile(Pattern.quote(needle), Pattern.CASE_INSENSITIVE).matcher(haystack).find();
    }
}
