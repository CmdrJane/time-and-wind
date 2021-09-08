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

	protected long dayDuration = 12000;
	protected long nightDuration = 12000;
	protected long cycleTime = dayDuration + nightDuration;
	protected double prevAngle = 0;
	protected double unTweakedAngle;

	public DimensionTypeMixin(OptionalLong fixedTime) {
		this.fixedTime = fixedTime;
	}

	@Inject(method = "getMoonPhase", at =@At("HEAD"), cancellable = true)
	private void patchMoonPhase(long time, CallbackInfoReturnable<Integer> cir){
		cir.setReturnValue((int)(time / this.cycleTime % 8L + 8L) % 8);
		return;
	}

	@Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true)
	private void calculateCelestialAngle(long time, CallbackInfoReturnable<Float> cir){
		if(!this.fixedTime.isPresent()){
			cir.setReturnValue((float) syncTime(dayDuration, nightDuration, time));
			return;
		}
	}

	private double syncTime(double dayD, double nightD, double time){
		double mod = time % (dayD + nightD);
		double d;
		double f;
		if(mod > dayD){
			mod -= dayD;
			f = 0.5D / nightD;
			d = f * mod + 0.5D;
		}
		else {
			f = 0.5D / dayD;
			d = f * mod;
		}
		this.unTweakedAngle = d;
		d -= 0.25D;
		if(d < 0)
			++d;
		this.prevAngle = d - f;
		if(this.prevAngle < 0)
			this.prevAngle = 1.0D - f;
		return d;
	}

	public int calculateIrisWorldDayTime(long time){
		long dayD = this.dayDuration;
		long nightD = this.nightDuration;
		float timeOfDay = time % (dayD + nightD);
		if(timeOfDay > dayD){
			timeOfDay -= dayD;
			float r = timeOfDay / nightD * 100;
			return Math.round(12000.0F / 100 * r);
		} else {
			float r = timeOfDay / dayD * 100;
			return Math.round(12000.0F / 100 * r);
		}
	}

	@Override
	public void setCycleDuration(long day, long night) {
		this.dayDuration = day;
		this.nightDuration = night;
		this.cycleTime = day + night;
	}

	@Override
	public long getDayDuration() {
		return this.dayDuration;
	}

	@Override
	public long getNightDuration() {
		return this.nightDuration;
	}

	@Override
	public long getCycleDuration() {
		return this.cycleTime;
	}

	@Override
	public float getPrevAngle() {
		return (float) this.prevAngle;
	}

	@Override
	public float untweakedAngle() {
		return (float) this.unTweakedAngle;
	}
}
