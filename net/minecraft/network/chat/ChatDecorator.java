package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ChatDecorator {
   ChatDecorator PLAIN = (serverplayer, component) -> CompletableFuture.completedFuture(component);

   CompletableFuture<Component> decorate(@Nullable ServerPlayer serverplayer, Component component);
}
