package ectotech.type;

import mindustry.type.Planet;
import mindustry.type.SectorPreset;

public class EctorumSectorPreset extends SectorPreset {

    public EctorumSectorPreset(String name, Planet planet, int sector) {
        super(name, planet, sector);
        fixedLaunch();
    }

    public void fixedLaunch() {
        overrideLaunchDefaults = true;
        allowLaunchLoadout = false;
        allowLaunchSchematics = false;

        /*
         * У Ectorum на уровне планеты allowLaunchLoadout = true.
         * Поэтому это необходимо, чтобы содержимое rules.loadout
         * действительно оказалось в ядре сюжетного сектора.
         */
        addStartingItems = true;
    }

    public void configurableResources() {
        overrideLaunchDefaults = true;
        allowLaunchLoadout = true;
        allowLaunchSchematics = false;
        addStartingItems = false;
    }

    public void configurableLaunch() {
        overrideLaunchDefaults = true;
        allowLaunchLoadout = true;
        allowLaunchSchematics = true;
        addStartingItems = false;
    }
}
