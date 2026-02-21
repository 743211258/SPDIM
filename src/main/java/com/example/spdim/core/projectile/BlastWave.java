package com.example.spdim.core.projectile;

import java.util.List;

import com.example.spdim.core.mechanic.Untargetable;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.ClipContext;

import com.example.spdim.ExampleMod;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class BlastWave extends ThrowableProjectile implements ItemSupplier{

    private double explodeRadius;
    private static final EntityDataAccessor<Float> EXPLODE_RADIUS = SynchedEntityData.defineId(BlastWave.class, EntityDataSerializers.FLOAT);


    public BlastWave(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected float getGravity() {
        // Not effected by gravity.
        return 0.0f;
    }

    public void sendShockwaveParticles() {
        if (this.level().isClientSide) {
            return;
        }
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        Vec3 center = this.position();
        double r = this.explodeRadius;
        // Number of points scales with radius.
        int points = (int) (72 * r);

        // Fibonacci sphere
        for (int i = 0; i < points; i++) {
            double phi = Math.acos(1 - 2 * (i + 0.5) / points);
            double theta = Math.PI * (1 + Math.sqrt(5)) * i;

            double x = Math.sin(phi) * Math.cos(theta);
            double y = Math.cos(phi);
            double z = Math.sin(phi) * Math.sin(theta);

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FIREWORK,
                    center.x + x * r,
                    center.y + y * r,
                    center.z + z * r,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(EXPLODE_RADIUS, (float) explodeRadius);
    }

    public double getExplodeRadius() {
        return explodeRadius;
    }


    public void setExplodeRadius(double radius) {
        this.explodeRadius = radius;
    }


    @Override
    public void tick() {
        super.tick();

        // Maximum distance is 100 blocks
        if (this.tickCount > 100) {
            explode(this.position());
        }

        // Explode when it touches liquid
        if (this.isInWaterOrBubble() || this.isInLava()) {
            explode(this.position());
        }

        // Projectile collision detection
        AABB area = this.getBoundingBox().inflate(0.5);

        List<Projectile> projectiles = this.level().getEntitiesOfClass(Projectile.class, area,
                p -> p != this);
        if (!projectiles.isEmpty()) {
            for (Projectile proj : projectiles) {
                if (proj instanceof BlastWave wave) {
                    explode(wave.position());
                }

                // Remove all projectile involved.
                proj.discard();
            }
            explode(this.position());
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            explode(this.position());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            explode(this.position());
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) {
            explode(this.position());
        }
    }
    
    public void explode(Vec3 pos) {
        if (this.level().isClientSide) {
            return;
        }

        AABB box = this.getBoundingBox().inflate(explodeRadius);
        // Get a list of all targetable entities within the exploding radius.
        List<Entity> candidates = this.level().getEntitiesOfClass(
                Entity.class,
                box,
                e -> e instanceof LivingEntity living && !Untargetable.isUntargetable(living)
        );

        for (Entity entity : candidates) {
            // Ignore the blast wave itself
            if (entity == this) {
                continue;
            }

            DamageSource source = entity.level().damageSources().explosion(this, getOwner());
            entity.hurt(source, 2);

            Vec3 direction = entity.position().subtract(pos);
            double radius = direction.length();
            if (radius > explodeRadius) {
                continue;
            }
            if (radius < 0.0001) {
                direction = new Vec3(0, 1, 0);
                radius = 0.0001;
            }


            double factor = Math.max(0, 1.0 - ((radius / explodeRadius) * (radius / explodeRadius)));
            Vec3 pushForce = direction.normalize().scale(4 * factor);
            if (isBlockedByBlock(pos, entity)) {
                pushForce = pushForce.scale(0.2);
            }

            if (entity instanceof ServerPlayer sp) {
                if (isBlockedByShield(this, sp)) {
                    pushForce = pushForce.scale(0.33);
                }
                sp.push(pushForce.x, pushForce.y, pushForce.z); // 强制推动
                sp.connection.send(new ClientboundSetEntityMotionPacket(sp)); // 同步客户端
                continue;
            } else {
                entity.setDeltaMovement(entity.getDeltaMovement().add(pushForce));
            }
        }
        sendShockwaveParticles();
        this.discard();
    }
    @Override
    public ItemStack getItem() {
        return new ItemStack(ExampleMod.BLAST_WAVE_ITEM.get());
    }

    private boolean isBlockedByBlock(Vec3 from, Entity entity) {
        Level level = entity.level();

        Vec3 to = entity.getBoundingBox().getCenter();

        BlockHitResult hit = level.clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));

        return hit.getType() == HitResult.Type.BLOCK;
    }

    private boolean isBlockedByShield(Entity source, Player target) {
        ItemStack mainHand = target.getMainHandItem();
        ItemStack offHand  = target.getOffhandItem();
        if (!((mainHand.getItem() instanceof ShieldItem || offHand.getItem() instanceof ShieldItem) && target.isUsingItem())) {
            return false;
        }

        // Block is effective if it is facing the blast wave.
        Vec3 direction = source.position().subtract(target.position()).normalize();
        Vec3 look = target.getLookAngle().normalize();
        double dot = direction.dot(look);
        return dot > 0.5;
    }
}
