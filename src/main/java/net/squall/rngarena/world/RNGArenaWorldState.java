package net.squall.rngarena.world;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

public class RNGArenaWorldState extends PersistentState {
	private static final String KEY_LOBBY_PLACED = "spawn_lobby_placed";
	private static final String KEY_LOBBY_WELCOME_TEXT = "lobby_welcome_text_shown";

	private boolean spawnLobbyPlaced;
	private final Set<UUID> lobbyWelcomeTextShown = new HashSet<>();

	public static RNGArenaWorldState create() {
		return new RNGArenaWorldState();
	}

	public static RNGArenaWorldState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		RNGArenaWorldState state = new RNGArenaWorldState();
		state.spawnLobbyPlaced = nbt.getBoolean(KEY_LOBBY_PLACED);
		if (!state.spawnLobbyPlaced && nbt.getBoolean("spawn_platform_placed")) {
			state.spawnLobbyPlaced = true;
		}
		if (nbt.contains(KEY_LOBBY_WELCOME_TEXT, NbtElement.LIST_TYPE)) {
			NbtList list = nbt.getList(KEY_LOBBY_WELCOME_TEXT, NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < list.size(); i++) {
				NbtCompound entry = list.getCompound(i);
				if (entry.containsUuid("Id")) {
					state.lobbyWelcomeTextShown.add(entry.getUuid("Id"));
				}
			}
		}
		return state;
	}

	public boolean isSpawnLobbyPlaced() {
		return this.spawnLobbyPlaced;
	}

	public void setSpawnLobbyPlaced(boolean value) {
		this.spawnLobbyPlaced = value;
		this.markDirty();
	}

	public boolean hasShownLobbyWelcomeText(UUID playerId) {
		return this.lobbyWelcomeTextShown.contains(playerId);
	}

	public void markLobbyWelcomeTextShown(UUID playerId) {
		if (this.lobbyWelcomeTextShown.add(playerId)) {
			this.markDirty();
		}
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.putBoolean(KEY_LOBBY_PLACED, this.spawnLobbyPlaced);
		NbtList list = new NbtList();
		for (UUID id : this.lobbyWelcomeTextShown) {
			NbtCompound entry = new NbtCompound();
			entry.putUuid("Id", id);
			list.add(entry);
		}
		nbt.put(KEY_LOBBY_WELCOME_TEXT, list);
		return nbt;
	}
}
