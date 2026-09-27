package org.kill.wpr.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.kill.wpr.init.EntityInit;

public final class SpawnUndyingHandler {
    private static final int SPAWN_RADIUS = 10;
    private static final int MAX_ATTEMPTS = 32;
    private static final ResourceLocation INFESTED_DIMENSION =
            ResourceLocation.fromNamespaceAndPath("wpr", "infested");

    private SpawnUndyingHandler() {
    }

    public static void handle(SpawnUndyingPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || !level.dimension().location().equals(INFESTED_DIMENSION)) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int offsetX = level.random.nextInt(-SPAWN_RADIUS, SPAWN_RADIUS + 1);
            int offsetZ = level.random.nextInt(-SPAWN_RADIUS, SPAWN_RADIUS + 1);
            if (offsetX * offsetX + offsetZ * offsetZ > SPAWN_RADIUS * SPAWN_RADIUS
                    || offsetX == 0 && offsetZ == 0) {
                continue;
            }

            int x = playerPos.getX() + offsetX;
            int z = playerPos.getZ() + offsetZ;
            if (!level.hasChunkAt(new BlockPos(x, playerPos.getY(), z))) {
                continue;
            }

            BlockPos spawnPos = findSpawnPosition(level, player, x, z);
            if (spawnPos == null) {
                continue;
            }

            var undying = EntityInit.UNDYING.get().create(
                    level, null, spawnPos, MobSpawnType.EVENT, false, false
            );
            if (undying == null) {
                return;
            }
            if (!level.noCollision(undying) || !undying.checkSpawnObstruction(level)) {
                undying.discard();
                continue;
            }

            level.addFreshEntityWithPassengers(undying);
            undying.setTarget(player);
            return;
        }
    }

    private static BlockPos findSpawnPosition(ServerLevel level, ServerPlayer player, int x, int z) {
        int y = player.blockPosition().getY();
        BlockPos spawnPos = new BlockPos(x, y, z);
        double dx = x + 0.5 - player.getX();
        double dy = y - player.getY();
        double dz = z + 0.5 - player.getZ();
        if (dx * dx + dy * dy + dz * dz > SPAWN_RADIUS * SPAWN_RADIUS
                || y < level.getMinBuildHeight()
                || y + 2 >= level.getMaxBuildHeight()
                || !level.isEmptyBlock(spawnPos)
                || !level.isEmptyBlock(spawnPos.above())
                || !level.getBlockState(spawnPos.below())
                        .isFaceSturdy(level, spawnPos.below(), Direction.UP)) {
            return null;
        }
        return spawnPos;
    }
}
