package com.regulad.advancementhunt.config;

import com.regulad.advancementhunt.AdvancementHunt;

public class AdvancementSeed extends AbstractFile {

    public AdvancementSeed() {
        super(AdvancementHunt.getInstance(), "nosql.yml","", true);
    }
}
