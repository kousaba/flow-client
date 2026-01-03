package net.flowclient.event.impl;

import net.flowclient.event.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class ItemTooltipEvent extends Event {
    private final ItemStack stack;
    private final List<Text> tooltip;
    private final TooltipType context;

    public ItemTooltipEvent(ItemStack stack, List<Text> tooltip, TooltipType context){
        this.stack = stack;
        this.tooltip = tooltip;
        this.context = context;
    }

    public ItemStack getStack(){
        return stack;
    }

    public List<Text> getTooltip(){
        return tooltip;
    }

    public boolean isAdvanced(){
        return context.isAdvanced();
    }

    public void add(String text){
        tooltip.add(Text.of(text));
    }
}
