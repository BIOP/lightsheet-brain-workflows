package ch.epfl.biop.lbw;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.io.IOException;

@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Lightsheet Brain Workflows>LBW - Create YML Settings From Base File")
public class LBWSettingsCreateCommand implements Command {

    @Parameter
    File base_yaml;

    @Override
    public void run() {
        try {
            Config config  = Config.loadFromFile(base_yaml);
            new YamlGUI().myDialog(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
