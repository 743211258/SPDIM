package com.example.spdim.core;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;

// Wand class
public abstract class Wand extends Item {

    public Wand(Properties properties) {
        super(properties);
    }

    // Right click is the default way of using wands.
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            cast(level, player, stack);
        }

        return InteractionResultHolder.success(stack);
    }

    // Wand interacts with the environment.
    protected abstract void cast(Level world, Player player, ItemStack stack);

    public abstract ItemStack getItem();

}