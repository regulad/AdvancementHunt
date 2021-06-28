package xyz.regulad.advancementhunt.config;

import xyz.regulad.advancementhunt.AdvancementHunt;

public class Messages extends AbstractFile {

    public Messages() {
        super(AdvancementHunt.getInstance(), "messages.yml", "", true);
    }
}
