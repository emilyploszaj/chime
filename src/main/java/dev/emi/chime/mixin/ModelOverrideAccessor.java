package dev.emi.chime.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Mixin(ModelOverride.class)
public interface ModelOverrideAccessor {
	
	@Invoker("matches")
	public boolean invokeMatches(ItemStack stack, ClientWorld world, LivingEntity entity);
}
