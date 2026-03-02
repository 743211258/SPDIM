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

    // parameters of the fireblast
    private final double HEIGHT = 10;
    private final double RADIUS = 10;
    private final double STEP_HEIGHT = 1;
    private final double STEP_RADIUS = 1;
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

        AABB collision_box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        // Detect for living entities
        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                collision_box,
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

        for (LivingEntity entity : entities) {

            Rooted.root(entity, 60);
            AABB box = entity.getBoundingBox();
            int blockMinX = Mth.floor(box.minX);
            int blockMinY = Mth.floor(box.minY);
            int blockMinZ = Mth.floor(box.minZ);
            int blockMaxX = Mth.floor(box.maxX);
            int blockMaxY = Mth.floor(box.maxY);
            int blockMaxZ = Mth.floor(box.maxZ);
            for (int x = blockMinX; x <= blockMaxX; x++) {
                for (int y = blockMinY; y <= blockMaxY; y++) {
                    for (int z = blockMinZ; z <= blockMaxZ; z++) {

                        Set<BlockPos> set = WandOfRegrowth.BLOCKS.computeIfAbsent(
                                entity, k -> new HashSet<>()
                        );

                        BlockPos woodPos = new BlockPos(x, y, z);


                        world.destroyBlock(woodPos, false);
                        world.setBlock(woodPos, Blocks.OAK_WOOD.defaultBlockState(), 11);


                        set.add(woodPos.immutable());
                        Rooted.LOCKED.put(entity, entity.position());
                    }
                }
            }
        }
        for (double d = 0; d <= HEIGHT; d += STEP_HEIGHT) {
            double radius = d;
            double radiusSqr = radius * radius;
            for (double x = -radius; x <= radius; x += STEP_RADIUS) {
                for (double y = -radius; y <= radius; y += STEP_RADIUS) {

                    if (x * x + y * y > radiusSqr) {
                        continue;
                    }

                    Vec3 pos = origin.add(forward.scale(d)).add(right.scale(x)).add(up.scale(y));

                    // Spawn particles
                    spawnBlockParticles(world, pos);
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
