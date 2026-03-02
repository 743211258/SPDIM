package com.example.spdim.core.wand.energyWand;

import com.example.spdim.core.wand.EnergyWand;
import com.example.spdim.core.projectile.BlastWave;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.chat.Component;
import com.example.spdim.ExampleMod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;


public class WandOfBlastWave extends EnergyWand{

    private final float EXPLODING_RADIUS = 5.0F;
    private final float SPEED = 1.0F;

    public WandOfBlastWave(Properties properties, int maxEnergy, int energyCost, int cooldown, Component name) {
        super(properties, maxEnergy, energyCost, cooldown, name);
    }

    @Override
    protected void cast(Level world, Player player, ItemStack stack) {
        if (!world.isClientSide) {
            // Cast happens only if the wand is charged
            if (getCurrentEnergy(stack) > 0) {
                // Generate a blast wave entity at eye level
                BlastWave wave = new BlastWave(ExampleMod.BLAST_WAVE.get(), world);

                // Set the initial position, direction, and explosion radius
                Vec3 spawnPos = player.getEyePosition(1.0F);
                wave.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                wave.setExplodeRadius(EXPLODING_RADIUS);

                // Set the owner for mob AI (Mobs will attack the caster)
                wave.setOwner(player);

                // Set the speed
                wave.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, SPEED,0.0F);
                wave.hasImpulse = true;

                // Add the wave to the player's world
                world.addFreshEntity(wave);

                // Additional condition to prevent bugs.
                if (getCurrentEnergy(stack) == getCurrentMaxEnergy(stack)) {
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putLong("LastChargeTime", player.level().getGameTime());
                }
                setCurrentEnergy(stack, getCurrentEnergy(stack) - 1);
            }
        }
    }
}
