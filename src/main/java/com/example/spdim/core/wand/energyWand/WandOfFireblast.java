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

    // parameters of the fireblast
    private final double HEIGHT = 10;
    private final double RADIUS = 10;
    private final double STEP_HEIGHT = 1;
    private final double STEP_RADIUS = 1;

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

        Vec3 pointOne = origin.add(forward.scale(HEIGHT)).add(right.scale(RADIUS));
        Vec3 pointTwo = origin.add(forward.scale(HEIGHT)).subtract(right.scale(RADIUS));
        Vec3 pointThree = origin.add(forward.scale(HEIGHT)).add(up.scale(RADIUS));
        Vec3 pointFour = origin.add(forward.scale(HEIGHT)).subtract(up.scale(RADIUS));

        double minX = Math.min(Math.min(Math.min(Math.min(origin.x, pointOne.x), pointTwo.x), pointThree.x), pointFour.x);
        double minY = Math.min(Math.min(Math.min(Math.min(origin.y, pointOne.y), pointTwo.y), pointThree.y), pointFour.y);
        double minZ = Math.min(Math.min(Math.min(Math.min(origin.z, pointOne.z), pointTwo.z), pointThree.z), pointFour.z);

        double maxX = Math.max(Math.max(Math.max(Math.max(origin.x, pointOne.x), pointTwo.x), pointThree.x), pointFour.x);
        double maxY = Math.max(Math.max(Math.max(Math.max(origin.y, pointOne.y), pointTwo.y), pointThree.y), pointFour.y);
        double maxZ = Math.max(Math.max(Math.max(Math.max(origin.z, pointOne.z), pointTwo.z), pointThree.z), pointFour.z);

        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        // Detect for living entities
        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> {
                    if (e == player ||
                            Untargetable.isUntargetable(e)) {
                        return false;
                    }
                    Vec3 vectorFromOriginToEntity = e.position().subtract(origin);
                    double projectionLength = vectorFromOriginToEntity.dot(forward);
                    if (projectionLength <= 0 || projectionLength > HEIGHT) {
                        return false;
                    }
                    double verticalLengthFromEntityToProjectionSqr = vectorFromOriginToEntity.lengthSqr() - (projectionLength * projectionLength);
                    double radiusAtSamePlane = projectionLength;
                    return (verticalLengthFromEntityToProjectionSqr <= radiusAtSamePlane * radiusAtSamePlane);
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

        for (double d = 0; d <= HEIGHT; d += STEP_HEIGHT) {
            double radius = d;
            double radiusSqr = radius * radius;
            for (double x = -radius; x <= radius; x += STEP_RADIUS) {
                for (double y = -radius; y <= radius; y += STEP_RADIUS) {

                    if (radius == 0 || x * x + y * y > radiusSqr) {
                        continue;
                    }

                    Vec3 pos = origin.add(forward.scale(d)).add(right.scale(x)).add(up.scale(y));

                    // Spawn fire particles
                    spawnFireParticles(world, pos);

                    // Ignite any block surface
                    BlockPos blockPos = BlockPos.containing(pos);

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
                1,
                0.12, 0.12, 0.12,
                0.02
        );
    }

}
