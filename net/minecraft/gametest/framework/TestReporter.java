package net.minecraft.gametest.framework;

public interface TestReporter {
   void onTestFailed(GameTestInfo gametestinfo);

   void onTestSuccess(GameTestInfo gametestinfo);

   default void finish() {
   }
}
