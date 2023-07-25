package net.minecraft.client.renderer;

public class Rect2i {
   private int xPos;
   private int yPos;
   private int width;
   private int height;

   public Rect2i(int i, int j, int k, int l) {
      this.xPos = i;
      this.yPos = j;
      this.width = k;
      this.height = l;
   }

   public Rect2i intersect(Rect2i rect2i) {
      int i = this.xPos;
      int j = this.yPos;
      int k = this.xPos + this.width;
      int l = this.yPos + this.height;
      int i1 = rect2i.getX();
      int j1 = rect2i.getY();
      int k1 = i1 + rect2i.getWidth();
      int l1 = j1 + rect2i.getHeight();
      this.xPos = Math.max(i, i1);
      this.yPos = Math.max(j, j1);
      this.width = Math.max(0, Math.min(k, k1) - this.xPos);
      this.height = Math.max(0, Math.min(l, l1) - this.yPos);
      return this;
   }

   public int getX() {
      return this.xPos;
   }

   public int getY() {
      return this.yPos;
   }

   public void setX(int i) {
      this.xPos = i;
   }

   public void setY(int i) {
      this.yPos = i;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void setWidth(int i) {
      this.width = i;
   }

   public void setHeight(int i) {
      this.height = i;
   }

   public void setPosition(int i, int j) {
      this.xPos = i;
      this.yPos = j;
   }

   public boolean contains(int i, int j) {
      return i >= this.xPos && i <= this.xPos + this.width && j >= this.yPos && j <= this.yPos + this.height;
   }
}
