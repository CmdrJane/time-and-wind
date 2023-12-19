package ru.aiefu.timeandwindct;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class SleepStatus {
    private int activePlayers;
    private int sleepingPlayers;

    public boolean areEnoughSleeping(int requiredSleepPercentage) {
        return this.sleepingPlayers >= this.sleepersNeeded(requiredSleepPercentage);
    }

    public boolean areEnoughDeepSleeping(int requiredSleepPercentage, List<ServerPlayerEntity> sleepingPlayers) {
        int i = (int)sleepingPlayers.stream().filter(PlayerEntity::isSleepingLongEnough).count();
        return i >= this.sleepersNeeded(requiredSleepPercentage);
    }

    public int sleepersNeeded(int requiredSleepPercentage) {
        return Math.max(1, MathHelper.ceil((float)(this.activePlayers * requiredSleepPercentage) / 100.0F));
    }

    public void removeAllSleepers() {
        this.sleepingPlayers = 0;
    }

    public int amountSleeping() {
        return this.sleepingPlayers;
    }

    public boolean update(List<ServerPlayerEntity> players) {
        int i = this.activePlayers;
        int j = this.sleepingPlayers;
        this.activePlayers = 0;
        this.sleepingPlayers = 0;

        for (ServerPlayerEntity serverPlayer : players) {
            if (!serverPlayer.isSpectator()) {
                ++this.activePlayers;
                if (serverPlayer.isSleeping()) {
                    ++this.sleepingPlayers;
                }
            }
        }

        return (j > 0 || this.sleepingPlayers > 0) && (i != this.activePlayers || j != this.sleepingPlayers);
    }
}
