package com.mojang.realmsclient.gui;

import net.minecraft.network.chat.Component;

public interface ErrorCallback {
   void error(Component component);

   default void error(String s) {
      this.error(Component.literal(s));
   }
}
