package net.minecraft.gametest.framework;

public class GlobalTestReporter {
   private static TestReporter DELEGATE = new LogTestReporter();

   public static void replaceWith(TestReporter testreporter) {
      DELEGATE = testreporter;
   }

   public static void onTestFailed(GameTestInfo gametestinfo) {
      DELEGATE.onTestFailed(gametestinfo);
   }

   public static void onTestSuccess(GameTestInfo gametestinfo) {
      DELEGATE.onTestSuccess(gametestinfo);
   }

   public static void finish() {
      DELEGATE.finish();
   }
}
