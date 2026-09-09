package ectotech.ui;

import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import ectotech.game.EctoTeams;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Tex;
import mindustry.ui.Styles;

import static mindustry.Vars.*;

public final class EctoTeamsUI {

    private EctoTeamsUI(){}

    public static void install(){
        installHudTeams();
        installEditorTeams();
    }


    /** HUD "редактировать в игре": пересобираем ряд команд с нашей командой. */
    private static void installHudTeams(){
        Team team = EctoTeams.ectorumTeam;
        if (team == null) return;

        if (!(ui.hudGroup.find("editor") instanceof Table editorHud)) return;
        if (!(editorHud.find("teams") instanceof Table outer)) return;
        if (!(outer.parent instanceof Table parent)) return;

        if (outer.getChildren().isEmpty() || !(outer.getChildren().first() instanceof Table inner)) return;

        if (inner.find("ectotech-awakened-hud") != null) return;
        if (inner.getCells().isEmpty()) return;

        var panelCell = parent.getCell(outer);
        var arrowCell = inner.getCells().peek();

        if (panelCell == null || !(arrowCell.get() instanceof ImageButton arrow)) return;

        ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNoneTogglei);
        button.name = "ectotech-awakened-hud";
        button.resizeImage(33f);
        button.margin(6f);
        button.getImageCell().grow();
        button.getStyle().imageUpColor = team.color;
        button.clicked(() -> Call.setPlayerTeamEditor(player, team));
        button.update(() -> button.setChecked(player.team() == team));

        arrowCell.setElement(button).tooltip(team.coloredName());
        inner.add(arrow).size(45f);

        inner.left();
        outer.left();
        inner.invalidateHierarchy();

        panelCell.width(outer.getPrefWidth() / Scl.scl(1f)).left();

        outer.invalidateHierarchy();
    }

    /** Редактор карт: кнопка drawTeam после каждого build(). */
    private static void installEditorTeams(){
        ui.editor.shown(() -> {
            Element anchor = findTeamAnchor(ui.editor);
            if (anchor == null || !(anchor.parent instanceof Table tools)) return;

            Team team = EctoTeams.ectorumTeam;
            ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNoneTogglei);
            button.margin(4f);
            button.getImageCell().grow();
            button.getStyle().imageUpColor = team.color;
            button.clicked(() -> editor.drawTeam = team);
            button.update(() -> button.setChecked(editor.drawTeam == team));
            tools.add(button);
        });
    }

    /** Кнопка команды blue — последняя из baseTeams; её родитель — таблица tools. */
    private static @Nullable Element findTeamAnchor(Group group){
        for(Element e : group.getChildren()){
            if(e instanceof ImageButton b && b.getStyle().imageUpColor == Team.blue.color) return e;
            if(e instanceof Group g){
                Element found = findTeamAnchor(g);
                if (found != null) return found;
            }
        }
        return null;
    }

}
