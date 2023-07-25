package net.minecraft.client.gui.components;

import net.minecraft.network.chat.Component;

public class CommonButtons {
   public static TextAndImageButton languageTextAndImage(Button.OnPress button_onpress) {
      return TextAndImageButton.builder(Component.translatable("options.language"), Button.WIDGETS_LOCATION, button_onpress).texStart(3, 109).offset(65, 3).yDiffTex(20).usedTextureSize(14, 14).textureSize(256, 256).build();
   }

   public static TextAndImageButton accessibilityTextAndImage(Button.OnPress button_onpress) {
      return TextAndImageButton.builder(Component.translatable("options.accessibility.title"), Button.ACCESSIBILITY_TEXTURE, button_onpress).texStart(3, 2).offset(65, 2).yDiffTex(20).usedTextureSize(14, 16).textureSize(32, 64).build();
   }
}
