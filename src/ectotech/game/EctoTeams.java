package ectotech.game;

import arc.graphics.Color;
import arc.util.Log;
import mindustry.game.Team;

public final class EctoTeams {

    public static Team ectorumTeam;
    public static final String ectorumTeamName = "awakened";

    private EctoTeams(){}

    public static void init(){
        ectorumTeam = findOrClaimTeam();

        // Палитра команды (3 оттенка)
        ectorumTeam.setPalette(
                Color.valueOf("9FC7DD"),
                Color.valueOf("78A8C4"),
                Color.valueOf("5C80A2")
        );

        Log.info("Registered Awakened team at id/index: @", ectorumTeam.id);
    }

    private static Team findOrClaimTeam(){
        Team existing = findTeamByName();
        if(existing != null) return existing;

        Team team = findFirstNumberedPlaceholderTeam();
        team.name = EctoTeams.ectorumTeamName;
        return team;
    }

    private static Team findTeamByName(){
        for(Team t : Team.all){
            if(t != null && EctoTeams.ectorumTeamName.equals(t.name)) return t;
        }
        return null;
    }

    private static Team findFirstNumberedPlaceholderTeam(){
        for(int i = 6; i < Team.all.length; i++){
            Team team = Team.all[i];
            if(team != null && ("team#" + i).equals(team.name)){
                return team;
            }
        }
        throw new IllegalStateException("EctoTech: no free placeholder Team slots (team#*) found.");
    }
}