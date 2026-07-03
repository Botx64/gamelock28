package com.nogamemode;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * このワールドでの /gamemode コマンド・F3+F4・その他あらゆる経路での
 * ゲームモード変更を完全に無効化する軽量Mod。
 */
@Mod(NoGamemodeMod.MOD_ID)
public class NoGamemodeMod {

    public static final String MOD_ID = "nogamemode";

    /** プレイヤーに表示する拒否メッセージ */
    public static final String DENY_MESSAGE = "このワールドでは無効です";

    public NoGamemodeMod(IEventBus modEventBus) {
        // /gamemode コマンドの無効化（RegisterCommandsEvent はゲームイベントバスに登録）
        NeoForge.EVENT_BUS.register(GamemodeCommandBlocker.class);

        // F3+F4 やその他あらゆる経路でのゲームモード変更を無効化
        NeoForge.EVENT_BUS.register(GamemodeChangeBlocker.class);
    }
}
