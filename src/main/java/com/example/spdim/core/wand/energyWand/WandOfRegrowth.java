package com.example.spdim.core.wand.energyWand;

import com.example.spdim.ExampleMod;
import com.example.spdim.core.mechanic.Rooted;
import com.example.spdim.core.mechanic.Untargetable;
import com.example.spdim.core.wand.EnergyWand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;


public class WandOfRegrowth extends EnergyWand {

    public static final Map<LivingEntity, Set<BlockPos>> BLOCKS = new HashMap<>();

    public WandOfRegrowth(Properties properties, int maxEnergy, int energyCost, int cooldown, Component name) {
        super(properties, maxEnergy, energyCost, cooldown, name);
    }

    @Override
    protected void cast(Level world, Player player, ItemStack stack) {
        if (world.isClientSide) return;
        if (getCurrentEnergy(stack) <= 0) return;
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

        for (double d = 0; d <= maxRange; d += stepForward) {
            double radius = d;
            for (double x = -radius; x <= radius; x += stepRadius) {
                for (double y = -radius; y <= radius; y += stepRadius) {

                    if (x * x + y * y > radius * radius) {
                        continue;
                    }

                    Vec3 pos = origin.add(forward.scale(d)).add(right.scale(x)).add(up.scale(y));

                    // Spawn particles
                    spawnBlockParticles(world, pos);

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
                                return (verticalLengthFromEntityToProjectionSqr <= radiusAtSamePlane * radiusAtSamePlane);
                            }
                    );

                    for (LivingEntity entity : entities) {

                        Rooted.root(entity, 60);
                        AABB box = entity.getBoundingBox();
                        int minX = Mth.floor(box.minX);
                        int minY = Mth.floor(box.minY);
                        int minZ = Mth.floor(box.minZ);
                        int maxX = Mth.floor(box.maxX);
                        int maxY = Mth.floor(box.maxY);
                        int maxZ = Mth.floor(box.maxZ);
                        for (int x2 = minX; x2 <= maxX; x2++) {
                            for (int y2 = minY; y2 <= maxY; y2++) {
                                for (int z2 = minZ; z2 <= maxZ; z2++) {

                                    Set<BlockPos> set = WandOfRegrowth.BLOCKS.computeIfAbsent(
                                            entity, k -> new HashSet<>()
                                    );

                                    BlockPos woodPos = new BlockPos(x2, y2, z2);


                                    world.destroyBlock(woodPos, false);
                                    world.setBlock(woodPos, Blocks.OAK_WOOD.defaultBlockState(), 11);


                                    set.add(woodPos.immutable());
                                    Rooted.LOCKED.put(entity, entity.position());
                                }
                            }
                        }
                    }
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

    public ItemStack getItem() {
        return new ItemStack(ExampleMod.BLAST_WAVE_ITEM.get());
    }

    private void spawnBlockParticles(Level world, Vec3 pos) {
        if (!(world instanceof ServerLevel server)) return;

        // generate a random number
        double r = Math.random();

        // 50% to be grass particles
        if (r < 0.5) {
            server.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRASS.defaultBlockState()),
                    pos.x, pos.y, pos.z,
                    2,
                    0.12, 0.12, 0.12,
                    0.02
            );
        // 30% to be leaf particles
        } else if (r < 0.8) {
            server.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LEAVES.defaultBlockState()),
                    pos.x, pos.y, pos.z,
                    2,
                    0.12, 0.12, 0.12,
                    0.02
            );
        // 20% to be log particles
        } else {
            server.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()),
                    pos.x, pos.y, pos.z,
                    1,
                    0.12, 0.12, 0.12,
                    0.02
            );
        }
    }
}
