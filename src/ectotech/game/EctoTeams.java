package ectotech.game;

import arc.graphics.Color;
import arc.util.Log;
import mindustry.game.Team;

public final class EctoTeams {

    public static Team ectoTeam;
    public static final String ectoTeamName = "awakened";

    private EctoTeams(){}

    public static void init(){
        ectoTeam = findOrClaimTeam();

        // Палитра команды (3 оттенка)
        ectoTeam.setPalette(
                Color.valueOf("a47ac4"),
                Color.valueOf("8a5aa4"),
                Color.valueOf("6a3a84")
        );

        Log.info("Registered Awakened team at id/index: @", ectoTeam.id);
    }

    private static Team findOrClaimTeam(){
        Team existing = findTeamByName();
        if(existing != null) return existing;

        Team team = findFirstNumberedPlaceholderTeam();
        team.name = EctoTeams.ectoTeamName;
        return team;
    }

    private static Team findTeamByName(){
        for(Team t : Team.all){
            if(t != null && EctoTeams.ectoTeamName.equals(t.name)) return t;
        }
        return null;
    }

    private static Team findFirstNumberedPlaceholderTeam(){
        for(int i = 7; i < Team.all.length; i++){
            Team team = Team.all[i];
            if(team != null && ("team#" + i).equals(team.name)){
                return team;
            }
        }
        throw new IllegalStateException("EctoTech: no free placeholder Team slots (team#*) found.");
    }
}