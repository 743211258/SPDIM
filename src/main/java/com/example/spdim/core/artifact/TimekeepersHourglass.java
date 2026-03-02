package com.example.spdim.core.artifact;

import com.example.spdim.core.Artifact;

import com.example.spdim.core.mechanic.TickFreeze;
import com.example.spdim.core.mechanic.Untargetable;
import com.example.spdim.core.network.FreezeOthersPacket;
import com.example.spdim.core.network.FreezeSelfPacket;
import com.example.spdim.core.network.MyModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TimekeepersHourglass extends Artifact {
    protected final int maxCooldown = 900;

    public TimekeepersHourglass(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isApplicable(ItemStack stack, Level world) {
        CompoundTag tag = stack.getOrCreateTag();
        long lastTime = tag.getLong("LastChargeTime");
        return world.getGameTime() - lastTime >= maxCooldown;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        stack.setHoverName(Component.translatable("item.spdim.timekeepers_hourglass"));

        if (world.isClientSide) {
            return;
        }

        initializeNBT(stack, world);
    }

    protected void initializeNBT(ItemStack stack, Level world) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("LastChargeTime")) {
            tag.putLong("LastChargeTime", world.getGameTime() - 900);
        }
    }

    public void FreezeOthersClient(Level world, Player player, ItemStack stack) {
        if (world.isClientSide) {
            MyModNetwork.CHANNEL.sendToServer(new FreezeOthersPacket());
        }
    }

    public void FreezeOthersServerSide(ServerPlayer player) {
        ItemStack stack = player.getOffhandItem();
        stack.getOrCreateTag();
        CompoundTag tag = stack.getOrCreateTag();
        if (!isApplicable(stack, player.level())) {
            return;
        }

        tag.putLong("LastChargeTime", player.level().getGameTime());
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        double range = 50.0;
        Vec3 end = start.add(look.scale(range));

        var entityHit = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                start,
                end,
                player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0),
                e -> e instanceof LivingEntity && e != player
        );
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target && !Untargetable.isUntargetable(target)) {
            TickFreeze.freeze(target, 100);
        }
    }

    public void FreezeMyselfClient(Level world, Player player, ItemStack stack) {
        if (world.isClientSide) {
            MyModNetwork.CHANNEL.sendToServer(new FreezeSelfPacket());
            return;
        }
    }

    public void FreezeMyselfServerSide(ServerPlayer player) {
        ItemStack stack = player.getOffhandItem();
        stack.getOrCreateTag();
        CompoundTag tag = stack.getOrCreateTag();
        if (!isApplicable(stack, player.level())) {
            return;
        }

        tag.putLong("LastChargeTime", player.level().getGameTime());
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, false, false));
        Untargetable.activate(player, 200);
    }
}
