package net.flowclient.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.flowclient.Flow;
import net.flowclient.event.impl.Render3DEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
}
