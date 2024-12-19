package land.chipmunk.chipmunkmod.data;

import net.minecraft.util.math.BlockPos;

// ? Am I reinventing the wheel here?
public class BlockArea {
  public BlockPos start;
  public BlockPos end;

  public BlockArea (BlockPos start, BlockPos end) {
    this.start = start;
    this.end = end;
  }
}
