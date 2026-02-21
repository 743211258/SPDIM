package com.example.spdim.core.renderer;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import com.example.spdim.core.projectile.BlastWave;

public class BlastWaveRenderer extends ThrownItemRenderer<BlastWave> {
    public BlastWaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
