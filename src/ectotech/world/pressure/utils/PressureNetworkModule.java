package ectotech.world.pressure.utils;

import arc.struct.Seq;
import ectotech.EctoVars;
import ectotech.world.pressure.interfaces.PressurizedNetworkMember;

public class PressureNetworkModule {

    public final String networkType;
    public Seq<PressurizedNetworkMember> members = new Seq<>();
    public float pressure;

    public PressureNetworkModule(String networkType, float initialPressure) {
        this.networkType = networkType;
        this.pressure = Math.max(EctoVars.absMinPressure, initialPressure);
    }

    public void sync() {
        if (members.isEmpty()) return;

        float sum = 0f;
        float totalWeight = 0f;

        for (PressurizedNetworkMember target : members) {
            float weight = target.self().block.size * target.self().block.size;

            sum += target.pressureModule().pressure * weight;
            totalWeight += weight;
        }

        if (totalWeight <= 0f) return;

        pressure = Math.max(EctoVars.absMinPressure, sum / totalWeight);

        for (PressurizedNetworkMember target : members) {
            target.pressureModule().pressure = pressure;
        }
    }

    public void addMember(PressurizedNetworkMember start) {
        addMember(start, null);
    }

    private void addMember(PressurizedNetworkMember start, PressurizedNetworkMember excluded) {
        if (start == null || start == excluded) return;
        if (!networkType.equals(start.pressureNetworkType())) return;

        Seq<PressurizedNetworkMember> queue = new Seq<>();
        Seq<PressurizedNetworkMember> connections = new Seq<>();

        queue.add(start);

        while (queue.any()) {
            PressurizedNetworkMember current = queue.pop();

            if (current == excluded) continue;
            if (members.contains(current, true)) continue;
            if (!networkType.equals(current.pressureNetworkType())) continue;

            members.add(current);

            PressureNetworkModule oldNet = current.pressureModule().network;

            if (oldNet != null && oldNet != this) {
                oldNet.members.remove(current, true);
            }

            current.pressureModule().network = this;

            connections.clear();
            current.getPressureConnections(connections);

            for (PressurizedNetworkMember other : connections) {
                if (other == excluded) continue;
                if (!networkType.equals(other.pressureNetworkType())) continue;
                if (queue.contains(other, true)) continue;
                if (members.contains(other, true)) continue;

                queue.add(other);
            }
        }

        sync();
    }

    public void split() {
        removeMember(null);
    }

    public void removeMember(PressurizedNetworkMember removed) {
        if (removed != null && !members.contains(removed, true)) return;

        float savedPressure = pressure;
        Seq<PressurizedNetworkMember> formerMembers = new Seq<>(members);

        if (removed != null) {
            formerMembers.remove(removed, true);
        }

        for (PressurizedNetworkMember m : members) {
            m.pressureModule().pressure = pressure;

            if (m.pressureModule().network == this) {
                m.pressureModule().network = null;
            }
        }

        members.clear();

        if (removed != null) {
            removed.pressureModule().network = null;
        }

        for (PressurizedNetworkMember m : formerMembers) {
            if (m.pressureModule().network == null) {
                new PressureNetworkModule(networkType, savedPressure).addMember(m, removed);
            }
        }
    }
}