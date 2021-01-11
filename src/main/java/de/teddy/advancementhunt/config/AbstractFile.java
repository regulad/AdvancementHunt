package de.teddy.advancementhunt.config;

import de.teddy.advancementhunt.AdvancementHunt;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class AbstractFile {
    private final File file;
    protected AdvancementHunt plugin;
    protected FileConfiguration configuration;

    public AbstractFile(AdvancementHunt main, String filename, String datafolder, Boolean save) {
        this.plugin = main;
        File file1 = new File(main.getDataFolder() + datafolder);

        if (!file1.exists()) {
            file1.mkdirs();
        }

        file = new File(file1, filename);
        if (!file.exists()) {
            if (save) {
                AdvancementHunt.getInstance().saveResource(filename, false);
                configuration = YamlConfiguration.loadConfiguration(file);
                return;
            }

            try {
                file.createNewFile();
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }

        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            AdvancementHunt.getInstance().getLogger().info("Unable to Save Config File!");
        }
    }

    public FileConfiguration getConfig() {
        return configuration;
    }

    public void reload() {
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void delete() {
        file.delete();
    }
}
