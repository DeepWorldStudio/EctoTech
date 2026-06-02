package ectotech.world.pressure.utils;

import arc.struct.Seq;
import ectotech.EctoVars;
import ectotech.world.pressure.interfaces.Pressurized;

public class PressurizedNetwork {

    public final String networkType;
    public Seq<Pressurized> members = new Seq<>();
    public float pressure;

    public PressurizedNetwork(String networkType, float initialPressure) {
        this.networkType = networkType;
        this.pressure = Math.max(EctoVars.absMinPressure, initialPressure);
    }

    public void sync() {
        if (members.isEmpty()) return;

        float sum = 0f;
        float totalWeight = 0f;

        for (Pressurized target : members) {
            float weight = target.self().block.size * target.self().block.size;
            sum += target.pressureModule().pressure * weight;
            totalWeight += weight;
        }

        pressure = Math.max(EctoVars.absMinPressure, sum / totalWeight);

        for (Pressurized target : members) {
            target.pressureModule().pressure = pressure;
        }
    }

    public void addMember(Pressurized start) {
        addMember(start, null);
    }

    private void addMember(Pressurized start, Pressurized excluded) {
        if (start == null || start == excluded) return;
        if (!networkType.equals(start.pressureNetworkType())) return;

        Seq<Pressurized> queue = new Seq<>();
        Seq<Pressurized> connections = new Seq<>();

        queue.add(start);

        while (queue.any()) {
            Pressurized current = queue.pop();

            if (current == excluded) continue;
            if (members.contains(current, true)) continue;
            if (!networkType.equals(current.pressureNetworkType())) continue;

            members.add(current);

            PressurizedNetwork oldNet = current.pressureModule().network;
            if (oldNet != null && oldNet != this) {
                oldNet.members.remove(current, true);
            }

            current.pressureModule().network = this;

            connections.clear();
            current.getPressureConnections(connections);

            for (Pressurized other : connections) {
                if (other == excluded) continue;
                if (!networkType.equals(other.pressureNetworkType())) continue;
                if (members.contains(other, true)) continue;

                queue.add(other);
            }
        }

        sync();
    }

    public void split() {
        removeMember(null);
    }

    public void removeMember(Pressurized removed) {
        if (removed != null && !members.contains(removed, true)) return;

        float savedPressure = pressure;
        Seq<Pressurized> formerMembers = new Seq<>(members);

        if (removed != null) {
            formerMembers.remove(removed, true);
        }

        for (Pressurized m : members) {
            m.pressureModule().pressure = pressure;
            if (m.pressureModule().network == this) m.pressureModule().network = null;
        }
        members.clear();

        if (removed != null) removed.pressureModule().network = null;

        for (Pressurized m : formerMembers) {
            if (m.pressureModule().network == null) {
                new PressurizedNetwork(networkType, savedPressure).addMember(m, removed);
            }
        }
    }
}