package net.minecraft.gametest.framework;

public interface GameTestListener {
   void testStructureLoaded(GameTestInfo gametestinfo);

   void testPassed(GameTestInfo gametestinfo);

   void testFailed(GameTestInfo gametestinfo);
}
