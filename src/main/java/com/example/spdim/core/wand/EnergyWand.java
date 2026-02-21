package com.example.spdim.core.wand;

import com.example.spdim.core.Wand;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Energy wand class
public abstract class EnergyWand extends Wand {
    protected final int maxEnergy;
    protected final int energyCost;
    protected final int maxCooldown;

    protected Component name;

    public EnergyWand(Properties properties, int maxEnergy, int energyCost, int maxCooldown, Component name) {
        super(properties);
        this.maxEnergy = maxEnergy;
        this.energyCost = energyCost;
        this.maxCooldown = maxCooldown;
        this.name = name;
    }

    // Data are based on NBT (except name) instead of class variables
    public int getCurrentEnergy(ItemStack stack) {
        return stack.getOrCreateTag().getInt("CurrentEnergy");
    }

    public void setCurrentEnergy(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt("CurrentEnergy", value);
    }

    public int getCurrentMaxEnergy(ItemStack stack) {
        return stack.getOrCreateTag().getInt("CurrentMaxEnergy");
    }

    public void setCurrentMaxEnergy(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt("CurrentMaxEnergy", value);
    }

    public int getEnergyCost(ItemStack stack) {
        return stack.getOrCreateTag().getInt("EnergyCost");
    }

    public void setEnergyCost(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt("EnergyCost", value);
    }

    public int getMaxCooldown(ItemStack stack) {
        return stack.getOrCreateTag().getInt("MaxCooldown");
    }

    public void setMaxCooldown(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt("MaxCooldown", value);
    }

    public Component getName(ItemStack stack) {
        return name;
    }

    public void setName(Component name) {
        this.name = name;
    }

    // Plus one energy every time the cooldown finishes.
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClientSide) {
            return;
        }

        if (!stack.hasTag()) {
            initializeNBT(stack, world);
        }

        CompoundTag tag = stack.getOrCreateTag();
        long lastTime = tag.getLong("LastChargeTime");
        long now = world.getGameTime();

        if (getCurrentEnergy(stack) < getCurrentMaxEnergy(stack)) {
            if (now - lastTime >= maxCooldown) {
                setCurrentEnergy(stack, getCurrentEnergy(stack) + 1);
                tag.putLong("LastChargeTime", now);
            }
        }
    }

    // Initialize NBT
    protected void initializeNBT(ItemStack stack, Level world) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("CurrentEnergy", maxEnergy);
        tag.putInt("CurrentMaxEnergy", maxEnergy);
        tag.putInt("EnergyCost", energyCost);
        tag.putInt("MaxCooldown", maxCooldown);
        tag.putLong("LastChargeTime", world.getGameTime());
    }

    protected abstract void cast(Level world, Player player, ItemStack stack);

    public abstract ItemStack getItem();

}