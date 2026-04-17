package net.squall.rngarena.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.squall.rngarena.RNGArena;
import net.squall.rngarena.item.CactusStaffItem;

public final class ModItems {
	public static final Item CACTUS_STAFF = register("cactus_staff", new CactusStaffItem(
			ToolMaterials.WOOD, new Item.Settings().maxCount(1)));

	private ModItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(CACTUS_STAFF));
	}

	private static Item register(String path, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(RNGArena.MOD_ID, path), item);
	}
}
