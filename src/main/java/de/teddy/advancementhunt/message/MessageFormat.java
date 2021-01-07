package de.teddy.advancementhunt.message;

public class MessageFormat {
    private String regex;
    private String replace;

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
