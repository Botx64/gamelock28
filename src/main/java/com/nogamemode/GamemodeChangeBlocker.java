package com.nogamemode;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * net.minecraft.server.level.ServerPlayer#setGameMode(GameType) は
 *   - /gamemode コマンド
 *   - F3+F4 のゲームモード切替画面（ServerboundChangeGameModePacket）
 *   - LAN公開ダイアログでのゲームモード選択
 * など、ゲームモードを変更するあらゆる経路の最終的な入口になっている。
 *
 * NeoForge はこの入口で PlayerEvent.PlayerChangeGameModeEvent
 * (キャンセル可能) を発火するため、ここで一律キャンセルすることで
 * 経路を問わずゲームモード変更を無効化できる。
 */
public final class GamemodeChangeBlocker {

    private GamemodeChangeBlocker() {
    }

    @SubscribeEvent
    public static void onChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        // currentGameMode が null の場合はプレイヤーの初回参加時の初期割り当てであり、
        // 「変更」ではないためブロックしない（これを塞ぐとワールドに参加できなくなる）。
        if (event.getCurrentGameMode() == null) {
            return;
        }

        // 実質的な変化がない場合は何もしない
        if (event.getCurrentGameMode() == event.getNewGameMode()) {
            return;
        }

        event.setCanceled(true);

        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal(NoGamemodeMod.DENY_MESSAGE));
        }
    }
}
