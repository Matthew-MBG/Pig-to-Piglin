package me.matthew_mbg.pig_to_piglin;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.entity.LightningEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TransformHandler {
    public static final Logger LOGGER = LoggerFactory.getLogger("pig_to_piglin");

    public static void registerEvents() {
        ServerTickEvents.END_WORLD_TICK.register((ServerWorld world) -> {
            for (LightningEntity lightning : world.getEntitiesByType(EntityType.LIGHTNING_BOLT, entity -> true)) {
                LOGGER.info("Lightning strike detected at " + lightning.getBlockPos());
                transformNearbyZombifiedPiglins(world, lightning);
            }
        });
    }

    private static void transformNearbyZombifiedPiglins(ServerWorld world, LightningEntity lightning) {
        BlockPos lightningPos = lightning.getBlockPos();
        int searchRadius = 5;

        List<ZombifiedPiglinEntity> zombifiedPiglins = world.getEntitiesByType(
                EntityType.ZOMBIFIED_PIGLIN,
                new Box(lightningPos).expand(searchRadius),
                zombifiedPiglin -> true
        );

        LOGGER.info("Found " + zombifiedPiglins.size() + " zombified piglins near the lightning strike");

        for (ZombifiedPiglinEntity zombifiedPiglin : zombifiedPiglins) {
            LOGGER.info("Attempting to transform zombified piglin at " + zombifiedPiglin.getBlockPos());
            PiglinEntity piglin = EntityType.PIGLIN.create(world);
            if (piglin != null) {
                piglin.refreshPositionAndAngles(zombifiedPiglin.getX(), zombifiedPiglin.getY(), zombifiedPiglin.getZ(), zombifiedPiglin.getYaw(), zombifiedPiglin.getPitch());
                piglin.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 255));

                zombifiedPiglin.remove(net.minecraft.entity.Entity.RemovalReason.KILLED);
                LOGGER.info("Zombified piglin removed");

                if (world.spawnEntity(piglin)) {
                    LOGGER.info("Piglin spawned successfully");
                } else {
                    LOGGER.warn("Failed to spawn Piglin");
                }
            } else {
                LOGGER.warn("Failed to create Piglin entity");
            }
        }
    }
}