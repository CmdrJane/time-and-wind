package ru.aiefu.timeandwind.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwind.VillagerChecker;

@Mixin(Entity.class)
public class EntityMixins {
    @Shadow public World level;

    @Inject(method = "changeDimension(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/entity/Entity;", at =@At(value = "INVOKE", target = "net/minecraft/world/server/ServerWorld.resetEmptyTime()V", ordinal = 1))
    private void reloadVillagerBrain(ServerWorld p_241206_1_, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir){
        if(this instanceof VillagerChecker){
            ((VillagerEntity)(Object)this).refreshBrain((ServerWorld) this.level);
        }
    }
}
