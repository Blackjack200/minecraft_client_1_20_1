package net.minecraft.client;

import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

public enum NarratorStatus {
   OFF(0, "options.narrator.off"),
   ALL(1, "options.narrator.all"),
   CHAT(2, "options.narrator.chat"),
   SYSTEM(3, "options.narrator.system");

   private static final IntFunction<NarratorStatus> BY_ID = ByIdMap.continuous(NarratorStatus::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
   private final int id;
   private final Component name;

   private NarratorStatus(int i, String s) {
      this.id = i;
      this.name = Component.translatable(s);
   }

   public int getId() {
      return this.id;
   }

   public Component getName() {
      return this.name;
   }

   public static NarratorStatus byId(int i) {
      return BY_ID.apply(i);
   }

   public boolean shouldNarrateChat() {
      return this == ALL || this == CHAT;
   }

   public boolean shouldNarrateSystem() {
      return this == ALL || this == SYSTEM;
   }
}
