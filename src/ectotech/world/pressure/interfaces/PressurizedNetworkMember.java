package ectotech.world.pressure.interfaces;

import arc.struct.Seq;
import ectotech.world.geometry.BlockContactGeometry;
import ectotech.world.pressure.utils.PressureModule;
import ectotech.world.pressure.utils.PressureNetworkModule;
import mindustry.gen.Building;

public interface PressurizedNetworkMember {

    Building self();
    PressureModule pressureModule();

    /** Если не пусто, блок является частью сети данного string-типа */
    String pressureNetworkType();

    /** Ссылка на сеть (null если не сетевой) */
    default PressureNetworkModule pressureNetwork() {
        return pressureModule().network;
    }

    default void getPressureConnections(Seq<PressurizedNetworkMember> out) {
        out.clear();
        if (pressureNetworkType() == null) return;

        for (Building neighbour : self().proximity) {
            if (!(neighbour instanceof PressurizedNetworkMember other)) continue;
            if (!pressureNetworkType().equals(other.pressureNetworkType())) continue;
            if (!BlockContactGeometry.hasEdgeContact(self(), neighbour)) continue;

            int sideToOther = self().relativeTo(neighbour);
            int sideFromOther = (sideToOther + 2) & 3;

            boolean iCanSend =
                    canPressureOutputTo(neighbour, sideToOther) &&
                    other.canPressureInputFrom(self(), sideFromOther);

            boolean otherCanSend =
                    other.canPressureOutputTo(self(), sideFromOther) &&
                    canPressureInputFrom(neighbour, sideToOther);

            if (iCanSend || otherCanSend) {
                out.add(other);
            }
        }
    }

    // Могу ли вывести давление в блок
    default boolean canPressureOutputTo(Building target, int side) {
        return true;
    }

    // Могу ли принять давление из блока
    default boolean canPressureInputFrom(Building source, int side) {
        return true;
    }

    default void rebuildNetwork() {
        if (pressureNetworkType() == null) return;

        PressureNetworkModule net = pressureNetwork();
        if (net == null) {
            new PressureNetworkModule(pressureNetworkType(), pressureModule().pressure).addMember(this);
        } else {
            net.split();
        }
    }

    default void disconnectNetwork() {
        PressureNetworkModule net = pressureNetwork();
        if (net == null) return;

        net.removeMember(this);
    }
}