package ectotech.world.blocks.units;

import arc.Events;
import arc.Graphics.Cursor;
import arc.Graphics.Cursor.SystemCursor;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.world.meta.EctoStatValues;
import mindustry.ai.UnitCommand;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.EventType.UnitCreateEvent;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.meta.Stat;

import static mindustry.Vars.*;

/**
 * Реконструктор, у которого один входной юнит может иметь несколько выходных.
 * Конфиги: UnitCommand (унаследован) — стартовая команда; UnitType / Integer — выбор ветки; Object[] — составной для копирования.
 */
public class BranchReconstructor extends Reconstructor{
    /** Заполняется в контенте через plans.addAll(...). Родительский upgrades синхронизируется в init(). */
    public Seq<UpgradePlan> plans = new Seq<>();

    /** Используется, когда plan.requirements == null. */
    public ItemStack[] defaultRequirements = {};
    /** Разрешать ли выбор стартовой команды. */
    public boolean commandConfigurable = true;
    public int selectionRows = 5, selectionColumns = 4;

    protected ObjectMap<UnitType, Seq<UpgradePlan>> byInput = new ObjectMap<>();

    public BranchReconstructor(String name){
        super(name);

        config(UnitType.class, BranchReconstructorBuild::selectOutput);
        config(Integer.class, BranchReconstructorBuild::selectPlan);

        config(Object[].class, (BranchReconstructorBuild b, Object[] objs) -> {
            for(Object o : objs){
                if(o instanceof UnitCommand cmd) b.command = cmd;
                else if(o instanceof Integer index) b.selectPlan(index);
            }
        });

        configClear((BranchReconstructorBuild b) -> {
            b.command = null;
            b.selections.clear();
        });
    }

    public void addUpgrade(UpgradePlan plan){
        plans.add(plan);
    }

    @Override
    public void addUpgrade(UnitType from, UnitType to){
        plans.add(new UpgradePlan(from, to));
    }

    @Override
    public void init(){
        byInput.clear();
        upgrades.clear();

        for (var plan : plans) {
            byInput.get(plan.input, Seq::new).add(plan);
            upgrades.add(new UnitType[]{plan.input, plan.output});
        }

        if (!consumeBuilder.contains(c -> c instanceof ConsumeItemDynamic)) {
            consume(new ConsumeItemDynamic(BranchReconstructorBuild::requirements));
        }

        float baseUsage = defaultPowerConsumption();

        if(consPower != null || plans.contains(p -> p.resolvePower(0f) > 0f)) {
            var dynamicPower = consumePowerDynamic((BranchReconstructorBuild b) -> b.shouldConsume() ? b.powerUse() : 0f);
            dynamicPower.usage = baseUsage;
        }

        super.init();
    }

    public float defaultPowerConsumption() {
        return consPower == null ? 0f : consPower.usage;
    }


    @Override
    public void initCapacities(){
        super.initCapacities();

        for(ItemStack stack : defaultRequirements){
            capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 2);
            itemCapacity = Math.max(itemCapacity, stack.amount * 2);
        }
        for(var plan : plans){
            if(plan.requirements == null) continue;
            for(ItemStack stack : plan.requirements){
                capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 2);
                itemCapacity = Math.max(itemCapacity, stack.amount * 2);
            }
        }
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.output);
        stats.remove(Stat.productionTime);
        stats.add(Stat.output, EctoStatValues.upgradePlans(plans, constructTime, defaultRequirements, defaultPowerConsumption()));
    }

    public class BranchReconstructorBuild extends ReconstructorBuild{
        /** Выбранный выход для каждого входного типа. */
        public ObjectMap<UnitType, UnitType> selections = new ObjectMap<>();
        /** Родительское поле constructing package-private, держим своё. */
        protected boolean constructingCache;

        public @Nullable UpgradePlan currentPlan(){
            return payload == null ? null : plan(payload.unit.type);
        }

        /** Выбор игрока (если он всё ещё валиден), иначе автовыбор. */
        public @Nullable UpgradePlan plan(UnitType input){
            var list = byInput.get(input);
            if(list == null || list.isEmpty()) return null;
            if(list.size == 1) return list.first();

            UnitType sel = selections.get(input);
            if(sel != null){
                var chosen = list.find(p -> p.output == sel && p.valid(team));
                if(chosen != null) return chosen;
            }

            var auto = list.find(p -> p.valid(team));
            return auto == null ? list.first() : auto;
        }

        public float constructTime(){
            var plan = currentPlan();
            return plan == null ? constructTime : plan.resolveTime(constructTime);
        }

        public ItemStack[] requirements(){
            var plan = currentPlan();
            return plan == null ? defaultRequirements : plan.resolveRequirements(defaultRequirements);
        }

        public float powerUse(){
            var plan = currentPlan();
            return plan == null ? 0f : plan.resolvePower(BranchReconstructor.this.defaultPowerConsumption());
        }

        /** Единственная точка входа для родительской логики (acceptPayload, hasUpgrade, unit()...). */
        @Override
        public UnitType upgrade(UnitType type){
            var plan = plan(type);
            return plan == null ? null : plan.output;
        }

        /** FIX #8: ванильный вариант проверяет бан у входа; проверяем и выход. */
        @Override
        public boolean hasUpgrade(UnitType type){
            UnitType t = upgrade(type);
            return t != null && (t.unlockedNowHost() || team.isAI()) && !type.isBanned() && !t.isBanned();
        }

        @Override
        public float fraction(){
            return progress / Math.max(constructTime(), 1f);
        }

        public void selectPlan(int index){
            if(index < 0 || index >= plans.size) return;
            var plan = plans.get(index);

            var list = byInput.get(plan.input);
            if(list == null || list.size <= 1) return; //выбор имеет смысл только при > 1 варианте
            if(selections.get(plan.input) == plan.output) return;

            selections.put(plan.input, plan.output);
            //смена ветки у юнита внутри — прогресс сбрасывается
            if(payload != null && payload.unit.type == plan.input){
                progress = 0f;
            }
        }

        /** Резолвит по-текущему payload, иначе — первый план с таким выходом. */
        public void selectOutput(UnitType output){
            UpgradePlan plan = null;
            if(payload != null){
                var list = byInput.get(payload.unit.type);
                if(list != null) plan = list.find(p -> p.output == output);
            }
            if(plan == null) plan = plans.find(p -> p.output == output);
            if(plan != null) selectPlan(plans.indexOf(plan, true));
        }

        public boolean canSelectOutput(){
            if(payload == null) return false;
            var list = byInput.get(payload.unit.type);
            return list != null && list.size > 1;
        }

        @Override
        public boolean canSetCommand(){
            var output = unit();

            return commandConfigurable && output != null && !output.isBanned() && output.allowChangeCommands && output.commands.size > 1;
        }

        /** Есть ли сейчас хотя бы одна доступная секция конфигурации. */
        public boolean hasConfigurationOptions() {
            return payload != null && (canSelectOutput() || canSetCommand());
        }

        @Override
        public boolean shouldShowConfigure(Player player){
            return player != null && block.configurable && interactable(player.team()) && hasConfigurationOptions();
        }

        @Override
        public Cursor getCursor(){
            return shouldShowConfigure(player) ? SystemCursor.hand : SystemCursor.arrow;
        }

        @Override
        public boolean shouldHideConfigure(Player player){
            return !shouldShowConfigure(player);
        }

        @Override
        public void buildConfiguration(Table table){
            if (payload == null) {
                deselect();
                return;
            }

            if (canSelectOutput()) {
                Seq<UnitType> outputs = byInput.get(payload.unit.type).select(p -> !p.output.isBanned()).map(p -> p.output);

                table.table(sel -> ItemSelection.buildTable(BranchReconstructor.this, sel, outputs,
                        () -> {
                            var plan = currentPlan();
                            return plan == null ? null : plan.output;
                        },
                        out -> {
                            if (out != null) configure(out);
                        },
                        false, selectionRows, selectionColumns
                )).row();
            }

            if (commandConfigurable) {
                table.table(Styles.black6, cmd -> {
                    UnitType[] last = {null};
                    cmd.update(() -> {
                        UnitType cur = unit();
                        if(cur != last[0]){
                            last[0] = cur;
                            rebuildCommands(cmd, cur);
                            table.pack();
                        }
                    });
                });
            }
        }

        protected void rebuildCommands(Table table, @Nullable UnitType unit){
            table.clearChildren();
            if(unit == null || unit.commands.size <= 1 || !unit.allowChangeCommands) return;

            var group = new ButtonGroup<ImageButton>();
            group.setMinCheckCount(0);
            int i = 0;

            for(var item : unit.commands){
                ImageButton button = table.button(item.getIcon(), Styles.clearNoneTogglei, 40f, () -> {
                    configure(item);
                    deselect();
                }).tooltip(item.localized()).group(group).get();

                button.update(() -> button.setChecked(command == item || (command == null && unit.defaultCommand == item)));

                if(++i % 4 == 0) table.row();
            }
        }

        /** ConsumeItemDynamic не заполняет itemFilter — принимаем всё, что есть хоть в одном плане. */
        @Override
        public boolean acceptItem(Building source, Item item){
            return capacities[item.id] > 0 && items.get(item) < getMaximumAccepted(item);
        }

        @Override
        public boolean shouldConsume(){
            return constructingCache && enabled && team.activateUnitFactories();
        }

        @Override
        public void updateTile(){
            constructingCache = constructing();
            boolean valid = false;

            if(payload != null){
                if (!hasUpgrade(payload.unit.type)) {
                    moveOutPayload();
                } else if(moveInPayload()) {
                    var plan = currentPlan();

                    if(efficiency > 0){
                        valid = true;
                        progress += edelta() * state.rules.unitBuildSpeed(team);
                    }

                    if (progress >= plan.resolveTime(constructTime)) {
                        consume();

                        payload.unit = plan.output.create(payload.unit.team());

                        if (payload.unit.isCommandable()) {
                            if (commandPos != null) {
                                payload.unit.command().commandPosition(commandPos);
                            }
                            payload.unit.command().command(command == null && payload.unit.type.defaultCommand != null ? payload.unit.type.defaultCommand : command);
                        }

                        createSound.at(this, 1f + Mathf.range(0.06f), createSoundVolume);
                        progress %= 1f;
                        Effect.shake(2f, 3f, this);
                        Fx.producesmoke.at(this);
                        Events.fire(new UnitCreateEvent(payload.unit, this));
                    }
                }
            }

            speedScl = Mathf.lerpDelta(speedScl, Mathf.num(valid), 0.05f);
            time += edelta() * speedScl * state.rules.unitBuildSpeed(team);
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);

            boolean fallback = true;
            for (int i = 0; i < 4; i++) {
                if(blends(i) && i != rotation){
                    Draw.rect(inRegion, x, y, (i * 90) - 180);
                    fallback = false;
                }
            }
            if(fallback) Draw.rect(inRegion, x, y, rotation * 90);

            Draw.rect(outRegion, x, y, rotdeg());

            if(constructing() && hasArrived()){
                Draw.draw(Layer.blockOver, () -> {
                    float f = Mathf.clamp(fraction());
                    Draw.alpha(1f - f);
                    Draw.rect(payload.unit.type.fullIcon, x, y, payload.rotation() - 90);
                    Draw.reset();
                    Drawf.construct(this, upgrade(payload.unit.type), payload.rotation() - 90f, f, speedScl, time);
                });
            }else{
                Draw.z(Layer.blockOver);
                drawPayload();
            }

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);
        }

        /** Для копирования/схем: [команда?, индексы выбранных планов...]. */
        @Override
        public Object config(){
            Seq<Object> out = new Seq<>();
            if(command != null) out.add(command);
            for(var e : selections){
                int idx = plans.indexOf(p -> p.input == e.key && p.output == e.value);
                if(idx != -1) out.add(idx);
            }
            return out.isEmpty() ? null : out.toArray(Object.class);
        }

        @Override
        public byte version(){
            return 4;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.s(selections.size);
            for (var e : selections) {
                write.s(e.key.id);
                write.s(e.value.id);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if (revision >= 4) {
                int amount = read.s();
                for (int i = 0; i < amount; i++) {
                    UnitType key = content.unit(read.s());
                    UnitType val = content.unit(read.s());
                    if (key != null && val != null) {
                        var list = byInput.get(key);
                        if (list != null && list.contains(p -> p.output == val)) selections.put(key, val);
                    }
                }
            }
        }
    }
}