package org.kill.wpr.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
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
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.kill.wpr.sound.ModSounds;
import xox.labvorty.shaderite.shader.PostShaderRunnerHandler;

@EventBusSubscriber(modid = "wpr", value = Dist.CLIENT)
public class SbwCommand {

    private static final ResourceLocation SBW_EFFECT =
            ResourceLocation.fromNamespaceAndPath("wpr", "shaders/post/sbw.json");
    private static final ResourceLocation INFESTED_DIMENSION =
            ResourceLocation.fromNamespaceAndPath("wpr", "infested");

    private static boolean active = false;
    private static int countdownTicks = -1;
    private static boolean developerUnlocked = false;
    private static Integer previousFov;
    private static BlockPos lightPosition;
    private static boolean replacedAir;
    private static SoundInstance ambientSound;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (countdownSound != null
                && !minecraft.getSoundManager().isActive(countdownSound)) {
            countdownSound = null; // It has ended (or was stopped)
        }

        boolean inInfestedDimension = minecraft.level != null
                && minecraft.level.dimension().location().equals(INFESTED_DIMENSION);
        updateAmbientSound(minecraft, inInfestedDimension);

        if (!inInfestedDimension) {
            developerUnlocked = false;
        } else if (ModKeybinds.consumeDeveloperUnlock()) {
            developerUnlocked = !developerUnlocked;
        }

        if (countdownTicks > 0 && --countdownTicks == 0) {
            if (minecraft.player != null) {
                minecraft.getSoundManager().play(
                        countdownSound = SimpleSoundInstance.forUI(
                                ModSounds.INFESTED_COUNTDOWN.get(), 1.0F
                        )
                );
                minecraft.getSoundManager().play(countdownSound);

            }
            countdownTicks = -1;
        }

        boolean shouldBeActive = inInfestedDimension && !developerUnlocked;
        if (active != shouldBeActive) {
            setActive(shouldBeActive);
        }

        if (!active) {
            removePlayerLight();
            return;
        }

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

    private static SoundInstance countdownSound;

    private static final ResourceLocation SLOW_ID =
            ResourceLocation.fromNamespaceAndPath("wpr", "infested_slow");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        boolean inInfested = player.level().dimension().location()
                .equals(ResourceLocation.fromNamespaceAndPath("wpr", "infested"));

        if (inInfested) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(
                    SLOW_ID, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else {
            speed.removeModifier(SLOW_ID);
        }
    }

    private static void updateAmbientSound(Minecraft minecraft, boolean inInfestedDimension) {
        if (!inInfestedDimension) {
            if (ambientSound != null) {
                // minecraft.getSoundManager().stop(ambientSound);
                ambientSound = null;
            }
            return;
        }
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean inInfestedDimension = minecraft.level != null
                && minecraft.level.dimension().location().equals(INFESTED_DIMENSION);
        if (inInfestedDimension) {
            minecraft.getSoundManager().resume();
            updateAmbientSound(minecraft, true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (active) {
            event.setCanceled(true);
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
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (active && (event.getNewScreen() instanceof PauseScreen
                || event.getNewScreen() instanceof ChatScreen
                || event.getNewScreen() instanceof AbstractContainerScreen)) {
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

        boolean justActivated = value && !active;
        Minecraft minecraft = Minecraft.getInstance();
        active = value;
        PostShaderRunnerHandler.setShader(SBW_EFFECT);
        PostShaderRunnerHandler.setRun(value);

        if (value) {
            previousFov = minecraft.options.fov().get();
            minecraft.options.fov().set(30);
            minecraft.options.bobView().set(true);
        } else {
            removePlayerLight();
            if (previousFov != null) {
                minecraft.options.fov().set(previousFov);
                previousFov = null;
            }
        }
        if (justActivated) {
            countdownTicks = 40; // about 2 seconds at 20 ticks per second
        } else if (!value) {
            countdownTicks = -1;
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
