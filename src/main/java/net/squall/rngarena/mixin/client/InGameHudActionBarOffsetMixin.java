package net.squall.rngarena.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

/**
 * Nudges the action bar (overlay message) slightly toward the hotbar for our lobby line only.
 * Vanilla does not expose a server-side position for this HUD text.
 */
@Mixin(InGameHud.class)
public class InGameHudActionBarOffsetMixin {
	/** Extra pixels downward (screen Y increases downward) — sits closer to the hotbar, less in the center of view. */
	@Unique
	private static final int RNGARENA_LOBBY_ACTION_BAR_OFFSET_Y = 10;

	@Shadow
	private int overlayRemaining;

	@Shadow
	private Text overlayMessage;

	@Unique
	private boolean rngarena$pushedLobbyBarOffset;

	@Inject(method = "renderOverlayMessage", at = @At("HEAD"))
	private void rngarena$pushLobbyBarOffset(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		this.rngarena$pushedLobbyBarOffset = false;
		if (this.overlayRemaining <= 0 || this.overlayMessage == null) {
			return;
		}
		if (!rngarena$isLobbyWaitingBar(this.overlayMessage)) {
			return;
		}
		context.getMatrices().push();
		context.getMatrices().translate(0.0F, RNGARENA_LOBBY_ACTION_BAR_OFFSET_Y, 0.0F);
		this.rngarena$pushedLobbyBarOffset = true;
	}

	@Inject(method = "renderOverlayMessage", at = @At("RETURN"))
	private void rngarena$popLobbyBarOffset(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if (!this.rngarena$pushedLobbyBarOffset) {
			return;
		}
		context.getMatrices().pop();
		this.rngarena$pushedLobbyBarOffset = false;
	}

	@Unique
	private static boolean rngarena$isLobbyWaitingBar(Text text) {
		TextContent content = text.getContent();
		return content instanceof TranslatableTextContent translatable
			&& "rng-arena.lobby.welcome.action_bar".equals(translatable.getKey());
	}
}
