package dev.emi.chime.armor;

import java.util.List;
import java.util.Map;

import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ChimeArmor {
	public static Map<Identifier, List<ModelOverride>> armorOverrides;
	
	public static <T extends LivingEntity> Identifier getArmorIdentifier(Identifier id, ItemStack stack, T entity) {
		/*
		if (armorOverrides.containsKey(id)) {
			for (ModelOverride override : armorOverrides.get(id)) {
				if (((ModelOverrideAccessor) override).invokeMatches(stack, (ClientWorld) entity.getEntityWorld(), entity)) {
					return override.getModelId();
				}
			}
		}*/
		return null;
	}
}
