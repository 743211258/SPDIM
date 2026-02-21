package com.example.spdim.core.wand.energyWand;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.spdim.core.mechanic.Untargetable;
import com.example.spdim.core.wand.EnergyWand;
import com.example.spdim.ExampleMod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WandOfFireblast extends EnergyWand {

    public WandOfFireblast(Properties properties, int maxEnergy, int energyCost, int cooldown, Component name) {
        super(properties, maxEnergy, energyCost, cooldown, name);
    }

    @Override
    protected void cast(Level world, Player player, ItemStack stack) {
        // Only execute at server side.
        if (world.isClientSide) {
            return;
        }

        // Cast only when the wand is charged.
        if (getCurrentEnergy(stack) <= 0) {
            return;
        }

        Vec3 origin = player.getEyePosition(1.0F);

        // Build a Lookat matrix
        Vec3 forward = player.getLookAngle().normalize();
        Vec3 worldUp = Math.abs(forward.y) > 0.99
                ? new Vec3(1, 0, 0)
                : new Vec3(0, 1, 0);
        Vec3 right = forward.cross(worldUp).normalize();
        Vec3 up = right.cross(forward).normalize();

        // parameters of the blastwave
        double maxRange = 10;
        double stepForward = 1;
        double stepRadius = 1;

        // Avoid duplicate spawning
        Set<BlockPos> ignitedBlocks = new HashSet<>();
        Set<Integer> lavaSpawnedEntities = new HashSet<>();

        for (double d = 0; d <= maxRange; d += stepForward) {
            double radius = d;
            double radiusSqr = radius * radius;
            for (double x = -radius; x <= radius; x += stepRadius) {
                for (double y = -radius; y <= radius; y += stepRadius) {

                    if (radius == 0 || x * x + y * y > radiusSqr) {
                        continue;
                    }

                    Vec3 pos = origin.add(forward.scale(d)).add(right.scale(x)).add(up.scale(y));

                    // Spawn fire particles
                    spawnFireParticles(world, pos);

                    // Detect for living entities
                    List<LivingEntity> entities = world.getEntitiesOfClass(
                            LivingEntity.class,
                            new AABB(
                                    pos.x - 0.5, pos.y - 0.5, pos.z - 0.5,
                                    pos.x + 0.5, pos.y + 0.5, pos.z + 0.5
                            ),
                            e -> {
                                if (e == player ||
                                        Untargetable.isUntargetable(e)) {
                                    return false;
                                }
                                Vec3 vectorFromOriginToEntity = e.position().subtract(origin);
                                double projectionLength = vectorFromOriginToEntity.dot(forward);
                                if (projectionLength <= 0 || projectionLength > maxRange) {
                                    return false;
                                }
                                double verticalLengthFromEntityToProjectionSqr = vectorFromOriginToEntity.lengthSqr() - (projectionLength * projectionLength);
                                double radiusAtSamePlane = projectionLength * 0.5;
                                return (verticalLengthFromEntityToProjectionSqr <= radiusAtSamePlane * radiusAtSamePlane && lavaSpawnedEntities.add(e.getId()));
                            }
                    );

                    // Spawn lava on any entity
                    for (LivingEntity entity : entities) {
                        BlockPos lavaPos = entity.blockPosition();
                        world.setBlock(
                                lavaPos,
                                Blocks.LAVA.defaultBlockState(),
                                11
                        );
                    }

                    // Ignite any block surface
                    BlockPos blockPos = BlockPos.containing(pos);

                    // Check for duplicates.
                    if (!ignitedBlocks.add(blockPos)) {
                        continue;
                    }

                    BlockState state = world.getBlockState(blockPos);

                    // Air is not ignitable.
                    if (state.isAir()) {
                        continue;
                    }

                    tryIgniteSurface(world, blockPos);
                }
            }
        }
        if (getCurrentEnergy(stack) == getCurrentMaxEnergy(stack)) {
            stack.getOrCreateTag();
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong("LastChargeTime", player.level().getGameTime());
        }
        setCurrentEnergy(stack, getCurrentEnergy(stack) - 1);
    }

    private void tryIgniteSurface(Level world, BlockPos pos) {
        // Get all surfaces of a block
        Direction[] dirs = Direction.values();

        for (Direction dir : dirs) {
            BlockPos firePos = pos.relative(dir);
            if (world.isEmptyBlock(firePos)) {
                world.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
            }
        }
    }

    private void spawnFireParticles(Level world, Vec3 pos) {
        if (!(world instanceof ServerLevel server)) return;

        server.sendParticles(
                ParticleTypes.FLAME,
                pos.x, pos.y, pos.z,
                2,
                0.12, 0.12, 0.12,
                0.02
        );
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ExampleMod.BLAST_WAVE_ITEM.get());
    }
}
