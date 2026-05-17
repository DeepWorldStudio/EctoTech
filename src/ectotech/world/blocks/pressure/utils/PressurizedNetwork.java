package ectotech.world.blocks.pressure.utils;

import arc.struct.Seq;
import ectotech.EctoVars;
import ectotech.world.blocks.pressure.interfaces.Pressurized;
import mindustry.gen.Building;

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

        float membersPressureSum = 0f;
        float membersWeightSum = 0f;

        for (Pressurized target : members) {
            float totalWeight = target.self().block.size * target.self().block.size;
            membersPressureSum += target.pressureModule().pressure * totalWeight;
            membersWeightSum += totalWeight;
        }

        pressure = Math.max(EctoVars.absMinPressure, membersPressureSum / membersWeightSum);

        for (Pressurized target : members) {
            target.pressureModule().pressure = pressure;
        }
    }

    // Добавить / удалить участника

    public void addMember(Pressurized member) {
        if (member == null) return;
        if (!member.pressureNetworkType().equals(networkType)) return;
        if (members.contains(member, true)) return;

        members.add(member);
        member.pressureModule().network = this;

        sync();
    }

    public void removeMember(Pressurized member) {
        if (member == null) return;

        members.remove(member, true);

        if (member.pressureModule().network == this) member.pressureModule().network = null;
        if (members.size < 2) {
            deleteNetwork();
            return;
        }

        sync();
    }

    // Создать / удалить сеть

    public static PressurizedNetwork createNetwork(Pressurized firstBuild, Pressurized secondBuild) {
        if (firstBuild == null || secondBuild == null) return null;
        if (!firstBuild.pressureNetworkType().equals(secondBuild.pressureNetworkType())) return null;
        if (firstBuild.pressureNetwork() != null || secondBuild.pressureNetwork() != null) return null;

        PressurizedNetwork newNetwork = new PressurizedNetwork(firstBuild.pressureNetworkType(), firstBuild.pressure());

        newNetwork.members.add(firstBuild);
        newNetwork.members.add(secondBuild);

        firstBuild.pressureModule().network = newNetwork;
        secondBuild.pressureModule().network = newNetwork;

        newNetwork.sync();
        return newNetwork;
    }

    public void deleteNetwork() {
        for (Pressurized member : members) {
            member.pressureModule().pressure = pressure;
            if (member.pressureModule().network == this) member.pressureModule().network = null;
        }

        members.clear();
    }

    // Слить / разбить сеть

    public void merge(PressurizedNetwork other) {
        if (other == null || other == this) return;
        if (!other.networkType.equals(networkType)) return;

        for (Pressurized otherMember : other.members) {
            if (!members.contains(otherMember, true)) {
                members.add(otherMember);
                otherMember.pressureModule().network = this;
            }
        }

        other.members.clear();
        sync();
    }

    public Seq<PressurizedNetwork> split() {
        float savedPressure = pressure;
        Seq<Pressurized> formerMembers = new Seq<>(members);
        deleteNetwork();

        Seq<PressurizedNetwork> splitResult = new Seq<>();

        while (formerMembers.any()) {
            Pressurized start = formerMembers.first();
            Seq<Pressurized> component = collectComponent(start, formerMembers);


            formerMembers.removeAll(component, true);

            if (component.size < 2) {
                component.first().pressureModule().pressure = savedPressure;
                continue;
            }

            PressurizedNetwork newNetwork = new PressurizedNetwork(networkType, savedPressure);

            // Добавляем в Seq и синхронизируем
            newNetwork.members.addAll(component);
            for (Pressurized m : component) {
                m.pressureModule().network = newNetwork;
            }

            // Добавляем в Seq и синхронизируем
            newNetwork.sync();
            splitResult.add(newNetwork);
        }

        return splitResult;
    }

    private Seq<Pressurized> collectComponent(Pressurized start, Seq<Pressurized> formerMembers) {
        Seq<Pressurized> component = new Seq<>();
        Seq<Pressurized> queue = new Seq<>();

        queue.add(start);

        while (queue.any()) {
            Pressurized currentBuild = queue.pop();
            if (component.contains(currentBuild, true)) continue;

            component.add(currentBuild);

            for (Building neighbour : currentBuild.self().proximity) {
                if (!(neighbour instanceof Pressurized pressurized)) continue;
                if (!formerMembers.contains(pressurized, true)) continue;
                if (component.contains(pressurized, true)) continue;

                queue.add(pressurized);
            }

        }

        return component;
    }


}
