package net.squall.rngarena.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.squall.rngarena.RNGArena;

public final class ModArmorMaterials {
	public static final RegistryEntry<ArmorMaterial> HELM_OF_DARKNESS = register("helm_of_darkness",
			createHelmetMaterial(3, 18, 2.0F, 0.0F));

	private ModArmorMaterials() {
	}

	private static ArmorMaterial createHelmetMaterial(int helmetProtection, int enchantability, float toughness,
			float knockbackResistance) {
		Map<ArmorItem.Type, Integer> protectionMap = new EnumMap<>(ArmorItem.Type.class);
		protectionMap.put(ArmorItem.Type.BOOTS, 0);
		protectionMap.put(ArmorItem.Type.LEGGINGS, 0);
		protectionMap.put(ArmorItem.Type.CHESTPLATE, 0);
		protectionMap.put(ArmorItem.Type.HELMET, helmetProtection);
		protectionMap.put(ArmorItem.Type.BODY, 0);

		Identifier textureId = Identifier.of(RNGArena.MOD_ID, "helm_of_darkness");
		return new ArmorMaterial(protectionMap, enchantability, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
				() -> Ingredient.EMPTY, List.of(new ArmorMaterial.Layer(textureId)), toughness, knockbackResistance);
	}

	private static RegistryEntry<ArmorMaterial> register(String id, ArmorMaterial material) {
		return Registry.registerReference(Registries.ARMOR_MATERIAL, Identifier.of(RNGArena.MOD_ID, id), material);
	}
}
