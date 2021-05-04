package ru.aiefu.timeandwind.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwind.VillagerChecker;

@Mixin(Entity.class)
public class EntityMixins {
    @Shadow public World world;

    @Inject(method = "moveToWorld", at =@At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.resetIdleTimeout()V", ordinal = 1))
    private void reloadVillagerBrain(ServerWorld destination, CallbackInfoReturnable<Entity> cir){
        if(this instanceof VillagerChecker){
            ((VillagerEntity)(Object)this).reinitializeBrain((ServerWorld) this.world);
        }
    }
}
