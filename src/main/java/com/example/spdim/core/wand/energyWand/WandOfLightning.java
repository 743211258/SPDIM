package com.example.spdim.core.wand.energyWand;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.spdim.core.mechanic.Untargetable;
import com.example.spdim.core.wand.EnergyWand;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class WandOfLightning extends EnergyWand{

    public WandOfLightning(Properties properties, int maxEnergy, int energyCost, int cooldown, Component name) {
        super(properties, maxEnergy, energyCost, cooldown, name);
    }

    @Override
    protected void cast(Level world, Player player, ItemStack stack) {
        if (world.isClientSide) {
            return;
        }

        if (getCurrentEnergy(stack) <= 0) {
            return;
        }

        Vec3 start = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        double range = 50.0;
        Vec3 end = start.add(look.scale(range));

        // Search for blocks
        BlockHitResult blockHit = world.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        // Search for entities
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                world,
                player,
                start,
                end,
                player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0),
                e -> e instanceof LivingEntity living && e != player && !Untargetable.isUntargetable(living)
        );

        // Calculate the coordinate of first collision
        Vec3 hitPos;
        double blockDist = blockHit.getType() == HitResult.Type.BLOCK
                ? blockHit.getLocation().distanceTo(start)
                : Double.MAX_VALUE;
        double entityDist = entityHit != null
                ? entityHit.getLocation().distanceTo(start)
                : Double.MAX_VALUE;

        // Hashmap to prevent duplicates
        Set<LivingEntity> hitEntities = new HashSet<>();

        // Calculate destination
        if (entityDist < blockDist && entityHit != null) {
            hitPos = entityHit.getLocation();
            if (entityHit.getEntity() instanceof LivingEntity living) {
                hitEntities.add(living);
            }
        } else if (blockDist < Double.MAX_VALUE) {
            hitPos = blockHit.getLocation();
        } else {
            hitPos = end;
        }

        // Generate lightning at the chosen location.
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            BlockPos blockPos = getBlockAbove(world, hitPos, player);
            if (blockPos == null) {
                lightning.moveTo(hitPos);
            } else {
                lightning.moveTo(Vec3.atCenterOf(blockPos).add(0, 0, 0));
            }
            if (player instanceof ServerPlayer serverPlayer) {
                lightning.setCause(serverPlayer);
            }
            world.addFreshEntity(lightning);
        }

        // Recursive find entities within the radius and spawn lightnings on them.
        spawnChainLightning(world, hitPos, player, 5.0, 3, hitEntities);
        if (getCurrentEnergy(stack) == getCurrentMaxEnergy(stack)) {
            stack.getOrCreateTag();
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong("LastChargeTime", player.level().getGameTime());
        }
        setCurrentEnergy(stack, getCurrentEnergy(stack) - 1);
    }

    private void spawnChainLightning(Level world, Vec3 center, Player player, double radius, int maxDepth, Set<LivingEntity> hitEntities) {
        if (maxDepth <= 0) {
            return;
        }

        double radiusSqr = radius * radius;
        // Find all entities within the radius
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius),
                e -> e != player && !hitEntities.contains(e) && !Untargetable.isUntargetable(e) && e.position().distanceToSqr(center) <= radiusSqr);

        for (LivingEntity e : entities) {
            // Add entities to the hashmap
            hitEntities.add(e);

            // Generate lightnings on entities.
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                BlockPos blockPos = getBlockAbove(world, e.position(), player);
                if (blockPos == null) {
                    lightning.moveTo(e.position());
                } else {
                    lightning.moveTo(Vec3.atCenterOf(blockPos).add(0, 0.5, 0));
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    lightning.setCause(serverPlayer);
                }
                world.addFreshEntity(lightning);
            }

            // Recursively search for other entities until the maxDepth is negative.
            spawnChainLightning(world, e.position(), player, radius, maxDepth - 1, hitEntities);
        }
    }

    @Nullable
    private BlockPos getBlockAbove(Level level, Vec3 pos, Player player) {
        // Calculate the starting point
        Vec3 start = new Vec3(pos.x, Mth.floor(level.getMaxBuildHeight()) + 1, pos.z);

        Vec3 end = new Vec3(pos.x, Mth.floor(pos.y + 1), pos.z);

        // Use clipContext to find the highest block above the player.
        BlockHitResult hit = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getBlockPos();
        }
        // Return the upmost collidable block above the starting point.
        return null;
    }

}
