package xyz.regulad.advancementhunt.config;

import xyz.regulad.advancementhunt.AdvancementHunt;

public class AdvancementSeed extends AbstractFile {

    public AdvancementSeed() {
        super(AdvancementHunt.getInstance(), "nosql.yml", "", true);
    }
}
