package net.minecraft.advancements;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum FrameType {
   TASK("task", 0, ChatFormatting.GREEN),
   CHALLENGE("challenge", 26, ChatFormatting.DARK_PURPLE),
   GOAL("goal", 52, ChatFormatting.GREEN);

   private final String name;
   private final int texture;
   private final ChatFormatting chatColor;
   private final Component displayName;

   private FrameType(String s, int i, ChatFormatting chatformatting) {
      this.name = s;
      this.texture = i;
      this.chatColor = chatformatting;
      this.displayName = Component.translatable("advancements.toast." + s);
   }

   public String getName() {
      return this.name;
   }

   public int getTexture() {
      return this.texture;
   }

   public static FrameType byName(String s) {
      for(FrameType frametype : values()) {
         if (frametype.name.equals(s)) {
            return frametype;
         }
      }

      throw new IllegalArgumentException("Unknown frame type '" + s + "'");
   }

   public ChatFormatting getChatColor() {
      return this.chatColor;
   }

   public Component getDisplayName() {
      return this.displayName;
   }
}
