package dev.emi.chime.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.chime.armor.ChimeArmor;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>>
		extends FeatureRenderer<T, M> {
	@Unique
	private T cachedEntity;
	@Unique
	private ItemStack cachedStack;

	public ArmorFeatureRendererMixin(FeatureRendererContext<T, M> context) {
		super(context);
	}
	
	@Inject(at = @At("HEAD"), method = "renderArmor")
	private void renderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T livingEntity, EquipmentSlot equipmentSlot, int i,
			A bipedEntityModel, CallbackInfo info) {
		cachedEntity = livingEntity;
		cachedStack = livingEntity.getEquippedStack(equipmentSlot);
	}

	@Inject(at = @At("RETURN"), method = "getArmorTexture", cancellable = true)
	private void getArmorTexture(ArmorItem armorItem, boolean bl, String string, CallbackInfoReturnable<Identifier> info) {
		info.setReturnValue(ChimeArmor.getArmorIdentifier(info.getReturnValue(), cachedStack, cachedEntity));
	}
}
