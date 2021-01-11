package de.teddy.advancementhunt.message;

public class MessageFormat {
    private final String regex;
    private final String replace;

    public MessageFormat(String regex,String replace)
    {
        this.regex = regex;
        this.replace = replace;
    }

    public String getRegex()
    {
        return regex;
    }

    public String getReplace()
    {
        return replace;
    }
}
