package rozo.alex.mathcraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Collection;
import java.util.Map;

/**
 * @author ci010
 */
public class BlockPosDispatcher
{
	private World world;
	private IBlockState state;
	private Multimap<ChunkPos, BlockPos> chunkPosBlockPosMap = HashMultimap.create();

//	private Multimap<ExtendedBlockStorage, Vec3i> cache = HashMultimap.create();

	public BlockPosDispatcher(World world, IBlockState state)
	{
		this.world = world;
		this.state = state;
	}

	public void add(BlockPos pos)
	{
		chunkPosBlockPosMap.put(new ChunkPos(pos), pos);
	}

	public void add(int x, int y, int z)
	{
		chunkPosBlockPosMap.put(new ChunkPos(x, z), new BlockPos(x, y, z));
	}

	private Vec3i translateToChunkCoord(int x, int y, int z)
	{
		return new Vec3i(x & 15, y, z & 15);
	}

	private Vec3i translateToChunkCoord(BlockPos pos)
	{
		return new Vec3i(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
	}

	public void trySet()
	{
		if (world.isRemote) return;
		System.out.println("try set in " + (world.isRemote ? "client" : "sever"));
		for (Map.Entry<ChunkPos, Collection<BlockPos>> entry : chunkPosBlockPosMap.asMap().entrySet())
		{
			ChunkPos chunkPos = entry.getKey();
			Chunk chunk = world.getChunkFromChunkCoords(chunkPos.chunkXPos, chunkPos.chunkZPos);

			for (BlockPos blockPos : entry.getValue())
			{
				IBlockState old = chunk.setBlockState(blockPos, state);
//				world.markAndNotifyBlock(blockPos, chunk, old, state, 2);
			}
//			ExtendedBlockStorage[] arr = chunk.getBlockStorageArray();
//			for (Vec3i vec3i : entry.getValue())
//			{
//				ExtendedBlockStorage extendedBlockStorage = arr[vec3i.getY() >> 4];
//				if (extendedBlockStorage == null)
//					System.out.println("The storage at " + vec3i + " is null");
//				else
//					extendedBlockStorage.set(vec3i.getX(), vec3i.getY() & 15, vec3i.getZ(), state);
//			}
		}
	}

//	public void flushToCache()
//	{
//		for (Map.Entry<ChunkPos, Collection<Vec3i>> entry : chunkPosBlockPosMap.asMap().entrySet())
//		{
//			ChunkPos chunkPos = entry.getKey();
//			Chunk chunk = world.getChunkFromChunkCoords(chunkPos.chunkXPos, chunkPos.chunkZPos);
//			ExtendedBlockStorage[] arr = chunk.getBlockStorageArray();
//			for (Vec3i vec3i : entry.getValue())
//			{
//				ExtendedBlockStorage extendedBlockStorage = arr[vec3i.getY() >> 4];
//				cache.put(extendedBlockStorage, translateToStorage(vec3i));
//			}
//		}
//	}
//
//	public void trySet()
//	{
//		for (Map.Entry<ExtendedBlockStorage, Vec3i> entry : cache.entries())
//			entry.getKey().set(entry.getValue().getX(), entry.getValue().getY(), entry.getValue().getZ(), state);
//	}

}
