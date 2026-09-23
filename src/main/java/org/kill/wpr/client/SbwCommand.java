package org.kill.wpr.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import xox.labvorty.shaderite.shader.PostShaderRunnerHandler;

@EventBusSubscriber(modid = "wpr", value = Dist.CLIENT)
public class SbwCommand {

    private static final ResourceLocation SBW_EFFECT =
            ResourceLocation.fromNamespaceAndPath("wpr", "shaders/post/sbw.json");

    private static boolean active = false;
    private static Integer previousFov;
    private static BlockPos lightPosition;
    private static boolean replacedAir;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!active) {
            removePlayerLight();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        BlockPos newPosition = BlockPos.containing(minecraft.player.getEyePosition());
        if (newPosition.equals(lightPosition)) {
            return;
        }

        removePlayerLight();

        if (minecraft.level.getBlockState(newPosition).isAir()) {
            minecraft.level.setBlock(
                newPosition,
                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15),
                3
            );
            lightPosition = newPosition;
            replacedAir = true;

        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        if (active) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (active) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            net.minecraft.commands.Commands.literal("sbw")
                .then(net.minecraft.commands.Commands.literal("toggle")
                    .executes(ctx -> {
                        setActive(!active);
                        ctx.getSource().sendSuccess(
                            () -> Component.literal(""),
                            false
                        );
                        return 1;
                    }))
                .then(net.minecraft.commands.Commands.literal("on")
                    .executes(ctx -> {
                        setActive(true);
                        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
                        return 1;
                    }))
                .then(net.minecraft.commands.Commands.literal("off")
                    .executes(ctx -> {
                        setActive(false);
                        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
                        return 1;
                    }))
        );
    }

    private static void setActive(boolean value) {
        if (active == value) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        active = value;
        PostShaderRunnerHandler.setShader(SBW_EFFECT);
        PostShaderRunnerHandler.setRun(value);

        if (value) {
            previousFov = minecraft.options.fov().get();
            minecraft.options.fov().set(45);
        } else {
            removePlayerLight();
            if (previousFov != null) {
                minecraft.options.fov().set(previousFov);
                previousFov = null;
            }
        }
    }

    private static void removePlayerLight() {
        if (!replacedAir || lightPosition == null) {
            lightPosition = null;
            replacedAir = false;
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null
            && minecraft.level.getBlockState(lightPosition).is(Blocks.LIGHT)) {
            minecraft.level.setBlock(lightPosition, Blocks.AIR.defaultBlockState(), 3);
        }
        lightPosition = null;
        replacedAir = false;
    }
}
