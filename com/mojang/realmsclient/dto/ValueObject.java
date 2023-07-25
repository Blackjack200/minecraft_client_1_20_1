package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class ValueObject {
   public String toString() {
      StringBuilder stringbuilder = new StringBuilder("{");

      for(Field field : this.getClass().getFields()) {
         if (!isStatic(field)) {
            try {
               stringbuilder.append(getName(field)).append("=").append(field.get(this)).append(" ");
            } catch (IllegalAccessException var7) {
            }
         }
      }

      stringbuilder.deleteCharAt(stringbuilder.length() - 1);
      stringbuilder.append('}');
      return stringbuilder.toString();
   }

   private static String getName(Field field) {
      SerializedName serializedname = field.getAnnotation(SerializedName.class);
      return serializedname != null ? serializedname.value() : field.getName();
   }

   private static boolean isStatic(Field field) {
      return Modifier.isStatic(field.getModifiers());
   }
}
