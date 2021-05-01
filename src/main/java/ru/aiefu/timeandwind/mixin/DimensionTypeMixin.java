package ru.aiefu.timeandwind.mixin;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.aiefu.timeandwind.IDimType;

import java.util.OptionalLong;

@Mixin(DimensionType.class)
public class DimensionTypeMixin implements IDimType {
	@Mutable
	@Final
	@Shadow
	private final OptionalLong fixedTime;

	protected double dayDuration = 12000;
	protected double nightDuration = 12000;
	protected double cycleTime = dayDuration + nightDuration;
	protected double prevAngle = 0;

	public DimensionTypeMixin(OptionalLong fixedTime) {
		this.fixedTime = fixedTime;
	}

	@Inject(method = "getMoonPhase", at =@At("HEAD"), cancellable = true)
	private void patchMoonPhase(long time, CallbackInfoReturnable<Integer> cir){
		cir.setReturnValue((int)(time / this.cycleTime % 8L + 8L) % 8);
	}

	@Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true)
	private void calculateCelestialAngle(long time, CallbackInfoReturnable<Float> cir){
		if(!this.fixedTime.isPresent()){
			cir.setReturnValue((float) syncTime(dayDuration, nightDuration, time));
		}
	}

	private double syncTime(double dayD, double nightD, double time){
		double mod = time % (dayD + nightD);
		double d;
		if(mod > dayD){
			mod -= dayD;
			d = 0.5D / nightD * mod + 0.5D;
		}
		else {
			d = 0.5 / dayD * mod;
		}
		d -= 0.25D;
		if(d < 0)
			++d;
		this.prevAngle = d;
		return d;
	}

	@Override
	public void setCycleDuration(double day, double night) {
		this.dayDuration = day;
		this.nightDuration = night;
		this.cycleTime = day + night;
	}

	@Override
	public double getDayDuration() {
		return this.dayDuration;
	}

	@Override
	public double getNightDuration() {
		return this.nightDuration;
	}

	@Override
	public double getCycleDuration() {
		return this.cycleTime;
	}

	@Override
	public float getPrevAngle() {
		return (float) this.prevAngle;
	}

}
