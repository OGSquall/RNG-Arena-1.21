package net.squall.rngarena.registry;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.util.math.random.Random;

/**
 * Central list of weapons used by RNG Arena round loadouts.
 */
public final class RNGWeaponRegistry {
	public static final List<Item> RNG_WEAPONS = List.of(
			ModItems.CACTUS_STAFF);

	private RNGWeaponRegistry() {
	}

	public static Item getRandomWeapon(Random random) {
		if (RNG_WEAPONS.isEmpty()) {
			throw new IllegalStateException("RNG_WEAPONS cannot be empty");
		}
		return RNG_WEAPONS.get(random.nextInt(RNG_WEAPONS.size()));
	}
}
