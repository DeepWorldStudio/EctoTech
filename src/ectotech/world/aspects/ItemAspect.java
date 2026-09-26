package ectotech.world.aspects;

import arc.graphics.Color;
import ectotech.world.blocks.production.BalancedCrafter;
import ectotech.world.blocks.production.BalancedCrafter.BalancedCrafterBuild;
import mindustry.type.Item;
import mindustry.world.meta.StatUnit;

/** По запасу предмета в буфере постройки. */
public class ItemAspect extends BuildAspect {
    public final Item item;

    public ItemAspect(Item item, float full, float powerReduction){
        this(item, 0f, full, powerReduction);
    }

    public ItemAspect(Item item, float start, float full, float powerReduction){
        super(start, full, powerReduction);
        this.item = item;
    }

    @Override public String name(){ return item.localizedName; }
    @Override public StatUnit unit(){ return StatUnit.items; }
    @Override public Color color(){ return item.color; }

    @Override
    public float value(BalancedCrafterBuild build){
        return build.items == null ? 0f : build.items.get(item);
    }

    @Override
    public void attachTo(BalancedCrafter block){
        block.hasItems = true;
    }
}