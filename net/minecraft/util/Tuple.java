package net.minecraft.util;

public class Tuple<A, B> {
   private A a;
   private B b;

   public Tuple(A object, B object1) {
      this.a = object;
      this.b = object1;
   }

   public A getA() {
      return this.a;
   }

   public void setA(A object) {
      this.a = object;
   }

   public B getB() {
      return this.b;
   }

   public void setB(B object) {
      this.b = object;
   }
}
