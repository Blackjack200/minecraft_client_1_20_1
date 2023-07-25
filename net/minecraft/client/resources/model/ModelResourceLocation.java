package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class ModelResourceLocation extends ResourceLocation {
   @VisibleForTesting
   static final char VARIANT_SEPARATOR = '#';
   private final String variant;

   private ModelResourceLocation(String s, String s1, String s2, @Nullable ResourceLocation.Dummy resourcelocation_dummy) {
      super(s, s1, resourcelocation_dummy);
      this.variant = s2;
   }

   public ModelResourceLocation(String s, String s1, String s2) {
      super(s, s1);
      this.variant = lowercaseVariant(s2);
   }

   public ModelResourceLocation(ResourceLocation resourcelocation, String s) {
      this(resourcelocation.getNamespace(), resourcelocation.getPath(), lowercaseVariant(s), (ResourceLocation.Dummy)null);
   }

   public static ModelResourceLocation vanilla(String s, String s1) {
      return new ModelResourceLocation("minecraft", s, s1);
   }

   private static String lowercaseVariant(String s) {
      return s.toLowerCase(Locale.ROOT);
   }

   public String getVariant() {
      return this.variant;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object instanceof ModelResourceLocation && super.equals(object)) {
         ModelResourceLocation modelresourcelocation = (ModelResourceLocation)object;
         return this.variant.equals(modelresourcelocation.variant);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return 31 * super.hashCode() + this.variant.hashCode();
   }

   public String toString() {
      return super.toString() + "#" + this.variant;
   }
}
