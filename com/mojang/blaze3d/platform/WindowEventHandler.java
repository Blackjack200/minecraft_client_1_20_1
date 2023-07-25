package com.mojang.blaze3d.platform;

public interface WindowEventHandler {
   void setWindowActive(boolean flag);

   void resizeDisplay();

   void cursorEntered();
}
