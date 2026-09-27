package org.kill.wpr.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.lwjgl.glfw.GLFW;
import org.kill.wpr.entity.Undying;
import org.kill.wpr.network.SpawnUndyingPayload;
import org.kill.wpr.sound.ModSounds;
import xox.labvorty.shaderite.shader.PostShaderRunnerHandler;

@EventBusSubscriber(modid = "wpr", value = Dist.CLIENT)
public class SbwCommand {

    private static final ResourceLocation SBW_EFFECT =
            ResourceLocation.fromNamespaceAndPath("wpr", "shaders/post/sbw.json");
    private static final ResourceLocation INFESTED_DIMENSION =
            ResourceLocation.fromNamespaceAndPath("wpr", "infested");

    private static boolean active = false;
    private static boolean debugUndyingGlow;
    private static int countdownTicks = -1;
    private static boolean developerUnlocked = false;
    private static boolean wasInInfestedDimension = false;
    private static boolean fullscreenBeforeActive;
    private static boolean countdownWindowEffectActive;
    private static boolean originalWindowMaximized;
    private static int windowShakeAmplitude;
    private static int windowShakeInterval;
    private static int windowShakeTickCounter;
    private static int windowShrinkDelayTicks;
    private static int windowShrinkTicks;
    private static int originalWindowWidth;
    private static int originalWindowHeight;
    private static int originalWindowX;
    private static int originalWindowY;
    private static int windowBaseX;
    private static int windowBaseY;
    private static Integer previousFov;
    private static BlockPos lightPosition;
    private static boolean replacedAir;
    private static SoundInstance ambientSound;

    @SubscribeEvent
    public static void onSelectMusic(SelectMusicEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null
                && minecraft.level.dimension().location().equals(INFESTED_DIMENSION)) {
            event.setMusic(null);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        updateUndyingDebugGlow(minecraft);

        boolean inInfestedDimension = minecraft.level != null
                && minecraft.level.dimension().location().equals(INFESTED_DIMENSION);
        if (countdownSound != null
                && !minecraft.getSoundManager().isActive(countdownSound)) {
            countdownSound = null;
            beginEndCountdownWindowEffect(minecraft);
            if (inInfestedDimension) {
                startAmbientSound(minecraft);
                if (minecraft.getConnection() != null) {
                    SpawnUndyingPayload.sendToServer();
                }
            }
        }

        boolean enteredInfestedDimension = inInfestedDimension && !wasInInfestedDimension;
        wasInInfestedDimension = inInfestedDimension;
        updateAmbientSound(minecraft, inInfestedDimension);

        if (!inInfestedDimension) {
            developerUnlocked = false;
            countdownTicks = -1;
            restoreCountdownWindow(minecraft);
        } else if (enteredInfestedDimension) {
            countdownTicks = 40;
        } else if (ModKeybinds.consumeDeveloperUnlock()) {
            developerUnlocked = !developerUnlocked;
        }

        boolean shouldBeActive = inInfestedDimension && !developerUnlocked;
        if (active != shouldBeActive) {
            setActive(shouldBeActive);
        }
        if (enteredInfestedDimension) {
            beginCountdownWindowEffect(minecraft);
        }

        if (countdownTicks > 0 && --countdownTicks == 0) {
            if (minecraft.player != null) {
                minecraft.getSoundManager().play(
                        countdownSound = SimpleSoundInstance.forUI(
                                ModSounds.INFESTED_COUNTDOWN.get(), 1.0F
                        )
                );
            }
            countdownTicks = -1;
        }
        updateWindowShake(minecraft);

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

    private static void updateUndyingDebugGlow(Minecraft minecraft) {
        if (ModKeybinds.consumeUndyingGlowToggle()) {
            debugUndyingGlow = !debugUndyingGlow;
        }

        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        AABB searchArea = minecraft.player.getBoundingBox().inflate(512.0);
        for (Entity entity : minecraft.level.getEntities(
                minecraft.player, searchArea, entity -> entity instanceof Undying)) {
            entity.setGlowingTag(debugUndyingGlow);
        }
    }

    private static SoundInstance countdownSound;

    private static final ResourceLocation SLOW_ID =
            ResourceLocation.fromNamespaceAndPath("wpr", "infested_slow");

    private static final ResourceLocation FAST_ID =
            ResourceLocation.fromNamespaceAndPath("wpr", "infested_fast");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        boolean inInfestedDimension = player.level().dimension().location().equals(INFESTED_DIMENSION);
        if (!inInfestedDimension) {
            speed.removeModifier(SLOW_ID);
            speed.removeModifier(FAST_ID);
            return;
        }

        boolean slower = countdownSound != null;

        if (slower) {
            speed.removeModifier(FAST_ID);
            speed.addOrUpdateTransientModifier(new AttributeModifier(
                    SLOW_ID, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else {
            speed.removeModifier(SLOW_ID);
            speed.addOrUpdateTransientModifier(new AttributeModifier(
                    FAST_ID, 1.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    private static void updateAmbientSound(Minecraft minecraft, boolean inInfestedDimension) {
        if (!inInfestedDimension) {
            if (ambientSound != null) {
                minecraft.getSoundManager().stop(ambientSound);
                ambientSound = null;
            }
        }
    }

    private static void startAmbientSound(Minecraft minecraft) {
        if (ambientSound != null && minecraft.getSoundManager().isActive(ambientSound)) {
            return;
        }

        ambientSound = new SimpleSoundInstance(
                ModSounds.INFESTED_AMBIENCE.get().getLocation(),
                SoundSource.AMBIENT,
                1.0F,
                1.0F,
                RandomSource.create(),
                true,
                0,
                SoundInstance.Attenuation.NONE,
                0.0,
                0.0,
                0.0,
                true
        );
        minecraft.getSoundManager().play(ambientSound);
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

        Minecraft minecraft = Minecraft.getInstance();
        active = value;
        PostShaderRunnerHandler.setShader(SBW_EFFECT);
        PostShaderRunnerHandler.setRun(value);

        if (value) {
            fullscreenBeforeActive = minecraft.getWindow().isFullscreen();
            if (fullscreenBeforeActive) {
                minecraft.options.fullscreen().set(false);
            }
            previousFov = minecraft.options.fov().get();
            minecraft.options.fov().set(30);
            minecraft.options.bobView().set(true);
        } else {
            removePlayerLight();
            restoreCountdownWindow(minecraft);
            if (previousFov != null) {
                minecraft.options.fov().set(previousFov);
                previousFov = null;
            }
            if (fullscreenBeforeActive) {
                minecraft.options.fullscreen().set(true);
                fullscreenBeforeActive = false;
            }
        }
        if (!value) {
            countdownTicks = -1;
        }
    }

    private static void beginCountdownWindowEffect(Minecraft minecraft) {
        var window = minecraft.getWindow();
        if (window.isFullscreen()) {
            return;
        }

        originalWindowWidth = window.getScreenWidth();
        originalWindowHeight = window.getScreenHeight();
        int[] xPosition = new int[1];
        int[] yPosition = new int[1];
        GLFW.glfwGetWindowPos(window.getWindow(), xPosition, yPosition);
        originalWindowX = xPosition[0];
        originalWindowY = yPosition[0];
        originalWindowMaximized = GLFW.glfwGetWindowAttrib(window.getWindow(), GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
        if (originalWindowMaximized) {
            GLFW.glfwRestoreWindow(window.getWindow());
        }
        countdownWindowEffectActive = true;

        int smallWidth = Math.max(320, originalWindowWidth / 2);
        int smallHeight = Math.max(240, originalWindowHeight / 2);
        resizeCountdownWindow(window, smallWidth, smallHeight);
        windowShakeAmplitude = 6;
        windowShakeInterval = 4;
        windowShakeTickCounter = 0;
    }

    private static void beginEndCountdownWindowEffect(Minecraft minecraft) {
        if (!countdownWindowEffectActive) {
            return;
        }

        var window = minecraft.getWindow();
        if (window.isFullscreen()) {
            restoreCountdownWindow(minecraft);
            return;
        }

        resizeCountdownWindow(window, originalWindowWidth, originalWindowHeight);
        windowShrinkDelayTicks = 4;
        windowShrinkTicks = 4;
        windowShakeAmplitude = 12;
        windowShakeInterval = 1;
        windowShakeTickCounter = 0;
    }

    private static void resizeCountdownWindow(com.mojang.blaze3d.platform.Window window, int width, int height) {
        windowBaseX = originalWindowX + (originalWindowWidth - width) / 2;
        windowBaseY = originalWindowY + (originalWindowHeight - height) / 2;
        window.setWindowed(width, height);
        GLFW.glfwSetWindowPos(window.getWindow(), windowBaseX, windowBaseY);
    }

    private static void updateWindowShake(Minecraft minecraft) {
        if (!countdownWindowEffectActive) {
            return;
        }

        if (minecraft.getWindow().isFullscreen()) {
            restoreCountdownWindow(minecraft);
            return;
        }

        var window = minecraft.getWindow();
        if (windowShrinkDelayTicks > 0) {
            windowShrinkDelayTicks--;
        } else if (windowShrinkTicks > 0) {
            int elapsedTicks = 9 - windowShrinkTicks;
            int targetWidth = originalWindowWidth * 3 / 4;
            int targetHeight = originalWindowHeight * 3 / 4;
            int width = originalWindowWidth
                    - (originalWindowWidth - targetWidth) * elapsedTicks / 8;
            int height = originalWindowHeight
                    - (originalWindowHeight - targetHeight) * elapsedTicks / 8;
            resizeCountdownWindow(window, width, height);
            windowShrinkTicks--;
        }

        if (++windowShakeTickCounter < windowShakeInterval) {
            return;
        }
        windowShakeTickCounter = 0;

        long handle = minecraft.getWindow().getWindow();
        GLFW.glfwSetWindowPos(
                handle,
                windowBaseX + (int)(Math.random() * (windowShakeAmplitude * 2 + 1)) - windowShakeAmplitude,
                windowBaseY + (int)(Math.random() * (windowShakeAmplitude * 2 + 1)) - windowShakeAmplitude
        );
    }

    private static void restoreCountdownWindow(Minecraft minecraft) {
        if (!countdownWindowEffectActive) {
            return;
        }

        var window = minecraft.getWindow();
        if (!window.isFullscreen()) {
            window.setWindowed(originalWindowWidth, originalWindowHeight);
            if (originalWindowMaximized) {
                GLFW.glfwMaximizeWindow(window.getWindow());
            } else {
                GLFW.glfwSetWindowPos(window.getWindow(), originalWindowX, originalWindowY);
            }
        }
        countdownWindowEffectActive = false;
        originalWindowMaximized = false;
        windowShakeInterval = 0;
        windowShakeTickCounter = 0;
        windowShrinkDelayTicks = 0;
        windowShrinkTicks = 0;
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
