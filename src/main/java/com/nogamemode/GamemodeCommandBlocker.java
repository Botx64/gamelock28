package com.nogamemode;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * サーバーにバニラの /gamemode コマンドが登録された直後にそのノードを取り除き、
 * 「常に無効メッセージを返すだけ」の /gamemode コマンドで置き換える。
 *
 * これにより /gamemode, /gamemode creative, /gamemode survival <player> など
 * 引数の有無・種類を問わず全てのバリエーションが無効化される。
 */
public final class GamemodeCommandBlocker {

    private static final Logger LOGGER = Logger.getLogger(NoGamemodeMod.MOD_ID);

    private GamemodeCommandBlocker() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 1. バニラ(および他Modが登録した)の gamemode ノードを削除する
        removeCommand(dispatcher.getRoot(), "gamemode");

        // 2. 常に無効メッセージだけを返す gamemode コマンドを登録する
        //    引数なし / 任意の引数(greedy string) の両方をカバーする
        dispatcher.register(
                Commands.literal("gamemode")
                        .executes(GamemodeCommandBlocker::denyExecution)
                        .then(Commands.argument("args", StringArgumentType.greedyString())
                                .executes(GamemodeCommandBlocker::denyExecution))
        );
    }

    private static int denyExecution(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendFailure(Component.literal(NoGamemodeMod.DENY_MESSAGE));
        return 0;
    }

    /**
     * Brigadier の CommandNode には子ノードを取り除く公式APIが存在しないため、
     * リフレクションで内部の children / literals / arguments マップから直接除去する。
     */
    @SuppressWarnings("unchecked")
    private static void removeCommand(RootCommandNode<CommandSourceStack> root, String name) {
        try {
            Field childrenField = CommandNode.class.getDeclaredField("children");
            Field literalsField = CommandNode.class.getDeclaredField("literals");
            Field argumentsField = CommandNode.class.getDeclaredField("arguments");
            childrenField.setAccessible(true);
            literalsField.setAccessible(true);
            argumentsField.setAccessible(true);

            ((Map<String, CommandNode<CommandSourceStack>>) childrenField.get(root)).remove(name);
            ((Map<String, CommandNode<CommandSourceStack>>) literalsField.get(root)).remove(name);
            ((Map<String, CommandNode<CommandSourceStack>>) argumentsField.get(root)).remove(name);
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove vanilla /gamemode command node. "
                    + "The command may still be partially functional.", e);
        }
    }
}
