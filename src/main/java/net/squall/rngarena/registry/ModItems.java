package net.squall.rngarena.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.squall.rngarena.RNGArena;
import net.squall.rngarena.item.CactusStaffItem;
import net.squall.rngarena.item.HelmOfDarknessItem;

public final class ModItems {
	public static final Item CACTUS_STAFF = register("cactus_staff", new CactusStaffItem(
			ToolMaterials.WOOD, new Item.Settings().maxCount(1)));
	public static final Item HELM_OF_DARKNESS = register("helm_of_darkness",
			new HelmOfDarknessItem(ModArmorMaterials.HELM_OF_DARKNESS, ArmorItem.Type.HELMET, new Item.Settings().maxCount(1)));

	private ModItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
			entries.add(CACTUS_STAFF);
			entries.add(HELM_OF_DARKNESS);
		});
	}

	private static Item register(String path, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(RNGArena.MOD_ID, path), item);
	}
}
