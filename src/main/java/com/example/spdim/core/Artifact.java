package com.example.spdim.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class Artifact extends Item {
    public Artifact(Properties properties) {
        super(properties);
    }

    public abstract boolean isApplicable(ItemStack stack, Level world);
}
