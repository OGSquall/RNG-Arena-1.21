package net.squall.rngarena.registry;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.squall.rngarena.RNGArena;
import net.squall.rngarena.effect.PrickedStatusEffect;

public final class ModEffects {
	public static final RegistryEntry<StatusEffect> PRICKED = register("pricked", new PrickedStatusEffect());

	private ModEffects() {
	}

	public static void register() {
		// Triggers static initialization.
	}

	private static RegistryEntry<StatusEffect> register(String path, StatusEffect effect) {
		return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(RNGArena.MOD_ID, path), effect);
	}
}
