package net.squall.rngarena.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

public class RNGArenaWorldState extends PersistentState {
	private static final String KEY_PLATFORM_PLACED = "spawn_platform_placed";

	private boolean spawnPlatformPlaced;

	public static RNGArenaWorldState create() {
		return new RNGArenaWorldState();
	}

	public static RNGArenaWorldState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		RNGArenaWorldState state = new RNGArenaWorldState();
		state.spawnPlatformPlaced = nbt.getBoolean(KEY_PLATFORM_PLACED);
		return state;
	}

	public boolean isSpawnPlatformPlaced() {
		return this.spawnPlatformPlaced;
	}

	public void setSpawnPlatformPlaced(boolean value) {
		this.spawnPlatformPlaced = value;
		this.markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.putBoolean(KEY_PLATFORM_PLACED, this.spawnPlatformPlaced);
		return nbt;
	}
}
