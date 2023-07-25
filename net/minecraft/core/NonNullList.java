package net.minecraft.core;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class NonNullList<E> extends AbstractList<E> {
   private final List<E> list;
   @Nullable
   private final E defaultValue;

   public static <E> NonNullList<E> create() {
      return new NonNullList<>(Lists.newArrayList(), (E)null);
   }

   public static <E> NonNullList<E> createWithCapacity(int i) {
      return new NonNullList<>(Lists.newArrayListWithCapacity(i), (E)null);
   }

   public static <E> NonNullList<E> withSize(int i, E object) {
      Validate.notNull(object);
      Object[] aobject = new Object[i];
      Arrays.fill(aobject, object);
      return new NonNullList<>(Arrays.asList((E[])aobject), object);
   }

   @SafeVarargs
   public static <E> NonNullList<E> of(E object, E... aobject) {
      return new NonNullList<>(Arrays.asList(aobject), object);
   }

   protected NonNullList(List<E> list, @Nullable E object) {
      this.list = list;
      this.defaultValue = object;
   }

   @Nonnull
   public E get(int i) {
      return this.list.get(i);
   }

   public E set(int i, E object) {
      Validate.notNull(object);
      return this.list.set(i, object);
   }

   public void add(int i, E object) {
      Validate.notNull(object);
      this.list.add(i, object);
   }

   public E remove(int i) {
      return this.list.remove(i);
   }

   public int size() {
      return this.list.size();
   }

   public void clear() {
      if (this.defaultValue == null) {
         super.clear();
      } else {
         for(int i = 0; i < this.size(); ++i) {
            this.set(i, this.defaultValue);
         }
      }

   }
}
