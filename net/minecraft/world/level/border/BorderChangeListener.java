package net.minecraft.world.level.border;

public interface BorderChangeListener {
   void onBorderSizeSet(WorldBorder worldborder, double d0);

   void onBorderSizeLerping(WorldBorder worldborder, double d0, double d1, long i);

   void onBorderCenterSet(WorldBorder worldborder, double d0, double d1);

   void onBorderSetWarningTime(WorldBorder worldborder, int i);

   void onBorderSetWarningBlocks(WorldBorder worldborder, int i);

   void onBorderSetDamagePerBlock(WorldBorder worldborder, double d0);

   void onBorderSetDamageSafeZOne(WorldBorder worldborder, double d0);

   public static class DelegateBorderChangeListener implements BorderChangeListener {
      private final WorldBorder worldBorder;

      public DelegateBorderChangeListener(WorldBorder worldborder) {
         this.worldBorder = worldborder;
      }

      public void onBorderSizeSet(WorldBorder worldborder, double d0) {
         this.worldBorder.setSize(d0);
      }

      public void onBorderSizeLerping(WorldBorder worldborder, double d0, double d1, long i) {
         this.worldBorder.lerpSizeBetween(d0, d1, i);
      }

      public void onBorderCenterSet(WorldBorder worldborder, double d0, double d1) {
         this.worldBorder.setCenter(d0, d1);
      }

      public void onBorderSetWarningTime(WorldBorder worldborder, int i) {
         this.worldBorder.setWarningTime(i);
      }

      public void onBorderSetWarningBlocks(WorldBorder worldborder, int i) {
         this.worldBorder.setWarningBlocks(i);
      }

      public void onBorderSetDamagePerBlock(WorldBorder worldborder, double d0) {
         this.worldBorder.setDamagePerBlock(d0);
      }

      public void onBorderSetDamageSafeZOne(WorldBorder worldborder, double d0) {
         this.worldBorder.setDamageSafeZone(d0);
      }
   }
}
