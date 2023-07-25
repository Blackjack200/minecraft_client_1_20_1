package net.minecraft.world.phys.shapes;

public interface BooleanOp {
   BooleanOp FALSE = (flag, flag1) -> false;
   BooleanOp NOT_OR = (flag, flag1) -> !flag && !flag1;
   BooleanOp ONLY_SECOND = (flag, flag1) -> flag1 && !flag;
   BooleanOp NOT_FIRST = (flag, flag1) -> !flag;
   BooleanOp ONLY_FIRST = (flag, flag1) -> flag && !flag1;
   BooleanOp NOT_SECOND = (flag, flag1) -> !flag1;
   BooleanOp NOT_SAME = (flag, flag1) -> flag != flag1;
   BooleanOp NOT_AND = (flag, flag1) -> !flag || !flag1;
   BooleanOp AND = (flag, flag1) -> flag && flag1;
   BooleanOp SAME = (flag, flag1) -> flag == flag1;
   BooleanOp SECOND = (flag, flag1) -> flag1;
   BooleanOp CAUSES = (flag, flag1) -> !flag || flag1;
   BooleanOp FIRST = (flag, flag1) -> flag;
   BooleanOp CAUSED_BY = (flag, flag1) -> flag || !flag1;
   BooleanOp OR = (flag, flag1) -> flag || flag1;
   BooleanOp TRUE = (flag, flag1) -> true;

   boolean apply(boolean flag, boolean flag1);
}
