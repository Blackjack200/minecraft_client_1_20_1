package net.minecraft.gametest.framework;

class ExhaustedAttemptsException extends Throwable {
   public ExhaustedAttemptsException(int i, int j, GameTestInfo gametestinfo) {
      super("Not enough successes: " + j + " out of " + i + " attempts. Required successes: " + gametestinfo.requiredSuccesses() + ". max attempts: " + gametestinfo.maxAttempts() + ".", gametestinfo.getError());
   }
}
