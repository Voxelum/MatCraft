package rozo.alex.mathcraft.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import rozo.alex.mathcraft.creativetab.CreativeTabsLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ItemMatCraft extends ItemSign
{
	private String localName = "MatCraft";
	private boolean isNew = true;

	public ItemMatCraft()
	{
		super();
		this.setUnlocalizedName(localName);
		this.setCreativeTab(CreativeTabsLoader.tabTesting);
	}

	public String getName()
	{
		return localName;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
	{
		playerIn.addChatComponentMessage(new TextComponentString("onItenRightClick"));

		return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		//player.addChatComponentMessage(new TextComponentString("--onItemUseFirst--" ));//for debug
		TileEntity tileentity = world.getTileEntity(pos);
		//player.addChatComponentMessage(new TextComponentString(String.valueOf(tileentity instanceof TileEntitySign) ));//for debug

		if (tileentity instanceof TileEntitySign)
		{
			this.isNew = false;
			graph3D((TileEntitySign) tileentity, player, world, pos);
		}
		//player.addChatComponentMessage(new TextComponentString("--onItemUseFirst--" ));//for debug
		return EnumActionResult.PASS;
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (this.isNew)// prevent the old sign gui appear;
		{
			IBlockState iblockstate = worldIn.getBlockState(pos);
			boolean flag = iblockstate.getBlock().isReplaceable(worldIn, pos);

			TileEntity tileentity = worldIn.getTileEntity(pos);
			playerIn.addChatComponentMessage(new TextComponentString(String.valueOf(tileentity instanceof TileEntitySign)));

			if (tileentity instanceof TileEntitySign)
				graph3D((TileEntitySign) tileentity, playerIn, worldIn, pos);


			if (facing != EnumFacing.DOWN && (iblockstate.getMaterial().isSolid() || flag) && (!flag || facing == EnumFacing.UP))
			{
				pos = pos.offset(facing);
				if (playerIn.canPlayerEdit(pos, facing, stack) && Blocks.STANDING_SIGN.canPlaceBlockAt(worldIn,
						pos))
					if (worldIn.isRemote)
						return EnumActionResult.SUCCESS;
					else
					{
						pos = flag ? pos.down() : pos;

						if (facing == EnumFacing.UP)
						{
							int i = MathHelper.floor_double((double) ((playerIn.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
							worldIn.setBlockState(pos, Blocks.STANDING_SIGN.getDefaultState().withProperty(BlockStandingSign.ROTATION, Integer.valueOf(i)), 11);
						}
						else
							worldIn.setBlockState(pos, Blocks.WALL_SIGN.getDefaultState().withProperty(BlockWallSign
									.FACING, facing), 11);

						--stack.stackSize;
						tileentity = worldIn.getTileEntity(pos);

						if (tileentity instanceof TileEntitySign && !ItemBlock.setTileEntityNBT(worldIn, playerIn, pos, stack))
							playerIn.openEditSign((TileEntitySign) tileentity);

						return EnumActionResult.SUCCESS;
					}
				else
					return EnumActionResult.FAIL;
			}
			else
				return EnumActionResult.FAIL;
		}
		else
		{
			isNew = true;
			return EnumActionResult.SUCCESS;
		}
	}

	private void graph3D(TileEntitySign tileentity, EntityPlayer playerIn, World world, BlockPos pos)
	{
		String[] inps = new String[4];
		for (int i = 0; i < 4; ++i)
		{
			inps[i] = tileentity.signText[i].getUnformattedText();
			playerIn.addChatComponentMessage(new TextComponentString(tileentity.signText[i].getUnformattedText()));
		}
		if (!ExpressionHelper.isEmpty(inps))
		{
			int[] numOfPara = ExpressionHelper.findNumberOfParameters(inps[0]);//potential to upgrade to parametric
			if (numOfPara == null)
			{//check number of brakets
				System.out.println("numberOfBraketsError or parametricError");
				playerIn.addChatComponentMessage(new TextComponentString("Not Valid For Graphing."));
				return;
			}                             //check number of brakets

			if (numOfPara[2] != 0)
				doParametrized(inps, world, pos, playerIn);
			else
				doGraph(inps, world, pos, playerIn);
		}
		else
			playerIn.addChatComponentMessage(new TextComponentString("Not a Valid Input For Graphing."));
	}

	private void doParametrized(String[] inps, World world, BlockPos pos, EntityPlayer playerIn)
	{
		Queue<Integer> xValue;
		Queue<Integer> yValues;
		Queue<Integer> zValues;
		Double t_start = ExpressionHelper.toBountry(inps[3], true);
		Double t_end = ExpressionHelper.toBountry(inps[3], false);
		IBlockState newState;

		if (t_start != null && t_end != null)
		{
			xValue = inps[0].replaceAll(" ", "").equals("") ? makeZeros(t_start, t_end) : new ThreeDimensionalGraphing(inps[0], t_start, t_end).getResults();
			yValues = inps[1].replaceAll(" ", "").equals("") ? makeZeros(t_start, t_end) : new ThreeDimensionalGraphing(inps[1], t_start, t_end).getResults();
			zValues = inps[2].replaceAll(" ", "").equals("") ? makeZeros(t_start, t_end) : new ThreeDimensionalGraphing(inps[2], t_start, t_end).getResults();
			newState = Blocks.IRON_BLOCK.getDefaultState();
		}
		else
		{
			xValue = inps[0].replaceAll(" ", "").equals("") ? makeZeros(0, 20) : new ThreeDimensionalGraphing(inps[0], 0, 20).getResults();
			yValues = inps[1].replaceAll(" ", "").equals("") ? makeZeros(0, 20) : new ThreeDimensionalGraphing(inps[1], 0, 20).getResults();
			zValues = inps[2].replaceAll(" ", "").equals("") ? makeZeros(0, 20) : new ThreeDimensionalGraphing(inps[2], 0, 20).getResults();
			newState = determineTexture(inps[3], playerIn);//determine the texture of the graph
		}

		if (xValue == null || yValues == null || zValues == null)
		{
			playerIn.addChatComponentMessage(new TextComponentString("Not void for Graphing"));
			return;
		}


		playerIn.addChatComponentMessage(new TextComponentString("Start Graphing..."));
		playerIn.addChatComponentMessage(new TextComponentString("The orgin coordinates is ( " + String.valueOf(pos.getX()) + ", " + String.valueOf(pos.getY()) + ", " + String.valueOf(pos.getZ()) + " )."));
		long startTime = System.currentTimeMillis();
		int counter = 0;
		BlockPos.MutableBlockPos newPos = new BlockPos.MutableBlockPos();

		while (!xValue.isEmpty())
		{
			int newY = pos.getY() + yValues.poll(); //translate to local
			int newX = pos.getX() + xValue.poll();
			int newZ = pos.getZ() + zValues.poll();

			yValues.poll();// remove useless 0 in param
			xValue.poll();
			zValues.poll();

			if (newY > 255)
				newY = 255;
			else if (newY <= 1)
				newY = 1;
			newPos.setPos(newX, newY, newZ);
//			System.out.println("(" + newX + "," + newY + "," + newZ + ")\n");
			world.setBlockState(newPos, newState);
			counter++;
		}

		long et = System.currentTimeMillis();
		playerIn.addChatComponentMessage(new TextComponentString("Done Graphing. Takes " + (et - startTime) + "ms"));
		playerIn.addChatComponentMessage(new TextComponentString(counter + " Blocks Added."));
		/////
	}

	private Queue<Integer> makeZeros(double t_start, double t_end)
	{
		Queue<Integer> zeros = new LinkedList<Integer>();
		if (t_start > t_end)
		{
			double temp = t_start;
			t_start = t_end;
			t_end = temp;
		}
		int ts = (int) Math.ceil(t_start);
		int te = (int) t_end;
		for (int i = ts; i <= te; i++)
			zeros.add(0);
		return zeros;
	}

	//The method actually does the work
	private void doGraph(String[] inps, World world, BlockPos pos, EntityPlayer playerIn)
	{
		Double x_lower = ExpressionHelper.toBountry(inps[1], true);
		Double x_upper = ExpressionHelper.toBountry(inps[1], false);
		Double z_lower = ExpressionHelper.toBountry(inps[2], true);
		Double z_upper = ExpressionHelper.toBountry(inps[2], false);

		//Start

		ThreeDimensionalGraphing TDG;
		inps[0] = inps[0].replaceAll("z", "y");
		inps[0] = inps[0].replaceAll("Z", "y");

		long startTime = System.currentTimeMillis();
		if (x_lower != null && x_upper != null && z_lower != null && z_upper != null)
		{
			TDG = new ThreeDimensionalGraphing(inps[0], x_lower, x_upper, z_lower, z_upper);
			long endTime = System.currentTimeMillis();
			playerIn.addChatComponentMessage(new TextComponentString("Generating z values takes " + (endTime - startTime) + "ms"));
		}
		else if ((x_lower != null && x_upper != null))
		{
			TDG = new ThreeDimensionalGraphing(inps[0], x_lower, x_upper, 0, 0);
			long endTime = System.currentTimeMillis();
			playerIn.addChatComponentMessage(new TextComponentString("Generating z values takes " + (endTime - startTime) + "ms"));

		}
		else if (z_lower != null && z_upper != null)
		{
			TDG = new ThreeDimensionalGraphing(inps[0], 0, 0, z_lower, z_upper);
			long endTime = System.currentTimeMillis();
			playerIn.addChatComponentMessage(new TextComponentString("Generating z values takes " + (endTime - startTime) + "ms"));

		}
		else if (x_lower == null && x_upper == null && z_lower == null && z_upper == null)
		{
			TDG = new ThreeDimensionalGraphing(inps[0]);
			long endTime = System.currentTimeMillis();
			playerIn.addChatComponentMessage(new TextComponentString("Done calculations. Takes " + (endTime - startTime) + "ms"));
		}
		else
		{
			playerIn.addChatComponentMessage(new TextComponentString("Not a Valid Domain For Graphing."));
			return;
		}

		if (TDG.getPostfixOperations() == null)
		{
			playerIn.addChatComponentMessage(new TextComponentString("Fail to Graph. The expression of the function might be wrong."));
		}
		else
		{
			IBlockState newState = determineTexture(inps[3], playerIn);//determine the texture of the graph
			//what blocks will be used

//			service.submit(new CalTask(TDG)).addListener(new Runnable()
//			{
//				@Override
//				public void run()
//				{
//
//				}
//			}, service);
			//System.out.println(TDG.toString());

			generateBlocksMultiThread(world, playerIn, pos, newState, TDG);

			/////
		}
	}

	private void generateBlocksCommon(World world, EntityPlayer playerIn, BlockPos pos, IBlockState newState,
									  ThreeDimensionalGraphing graphing)
	{
		playerIn.addChatComponentMessage(new TextComponentString("Start Graphing..."));
		playerIn.addChatComponentMessage(new TextComponentString("The orgin coordinates is ( " + String.valueOf(pos.getX()) + ", " + String.valueOf(pos.getY()) + ", " + String.valueOf(pos.getZ()) + " )."));
		long startTime = System.currentTimeMillis();

		BlockPos.MutableBlockPos newPos = new BlockPos.MutableBlockPos();
		Queue<Integer> resultsCoordinates = graphing.getResults();
		int counter = 0;

		while (!resultsCoordinates.isEmpty())
		{
			int newY = pos.getY() + resultsCoordinates.poll();
			if (newY > 255)
			{
				newY = 255;
			}
			else if (newY <= 1)
			{
				newY = 1;
			}//enforce the coordinates to be inside the world
			int newX = pos.getX() + resultsCoordinates.poll();
			int newZ = pos.getZ() + resultsCoordinates.poll();
			newPos.setPos(newX, newY, newZ);

			world.setBlockState(newPos, newState);
			counter++;
		}
		long et = System.currentTimeMillis();
		playerIn.addChatComponentMessage(new TextComponentString("Done Graphing. Takes " + (et - startTime) + "ms"));
		playerIn.addChatComponentMessage(new TextComponentString(counter + " Blocks Added."));
	}

	private void generateByDispatch(final World world, EntityPlayer playerIn, BlockPos pos, IBlockState newState,
									ThreeDimensionalGraphing graphing)
	{
		playerIn.addChatComponentMessage(new TextComponentString("Start Graphing..."));
		playerIn.addChatComponentMessage(new TextComponentString("The orgin coordinates is ( " + String.valueOf(pos.getX()) + ", " + String.valueOf(pos.getY()) + ", " + String.valueOf(pos.getZ()) + " )."));
		long startTime = System.currentTimeMillis();

		BlockPosDispatcher blockPosDispatcher = new BlockPosDispatcher(world, newState);
		Queue<Integer> resultsCoordinates = graphing.getResults();

		int counter = 0;

		while (!resultsCoordinates.isEmpty())
		{
			int newY = pos.getY() + resultsCoordinates.poll();
			if (newY > 255)
			{
				newY = 255;
			}
			else if (newY <= 1)
			{
				newY = 1;
			}//enforce the coordinates to be inside the world
			int newX = pos.getX() + resultsCoordinates.poll();
			int newZ = pos.getZ() + resultsCoordinates.poll();
			blockPosDispatcher.add(newX, newY, newZ);
			counter++;
		}

		blockPosDispatcher.trySet();

		long et = System.currentTimeMillis();
		playerIn.addChatComponentMessage(new TextComponentString("Done Graphing. Takes " + (et - startTime) + "ms"));
		playerIn.addChatComponentMessage(new TextComponentString(counter + " Blocks Added."));
	}

	private void generateBlocksMultiThread(final World world, EntityPlayer playerIn, BlockPos pos, IBlockState newState,
										   ThreeDimensionalGraphing graphing)
	{
		playerIn.addChatComponentMessage(new TextComponentString("Start Graphing..."));
		playerIn.addChatComponentMessage(new TextComponentString("The orgin coordinates is ( " + String.valueOf(pos.getX()) + ", " + String.valueOf(pos.getY()) + ", " + String.valueOf(pos.getZ()) + " )."));
		long startTime = System.currentTimeMillis();
		Queue<Integer> resultsCoordinates = graphing.getResults();
		int counter = 0;
		Queue<ArrayList<BlockPos>> cache = Queues.newArrayDeque();
		ArrayList<BlockPos> session = Lists.newArrayList();
		while (!resultsCoordinates.isEmpty())
		{
			int newY = pos.getY() + resultsCoordinates.poll();
			if (newY > 255)
				newY = 255;
			else if (newY <= 1)
				newY = 1;
			//enforce the coordinates to be inside the world
			int newX = pos.getX() + resultsCoordinates.poll();
			int newZ = pos.getZ() + resultsCoordinates.poll();
			session.add(new BlockPos(newX, newY, newZ));
			if (counter++ > 128)
			{
				cache.add(session);
				session = Lists.newArrayList();
				counter = 0;
			}
		}
		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new LinkedTask(cache, world,
				startTime, newState, playerIn));

	}

	private static ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));

	public static class LinkedTask implements Runnable
	{
		Queue<ArrayList<BlockPos>> cache;
		World world;
		long startTime;
		IBlockState newState;
		EntityPlayer playerIn;

		public LinkedTask(Queue<ArrayList<BlockPos>> cache, World world, long startTime, IBlockState newState, EntityPlayer playerIn)
		{
			this.cache = cache;
			this.world = world;
			this.startTime = startTime;
			this.newState = newState;
			this.playerIn = playerIn;
		}

		@Override
		public void run()
		{
			ArrayList<BlockPos> poll = cache.poll();
			for (BlockPos blockPos : poll)
				world.setBlockState(blockPos, newState, 2);
			if (!cache.isEmpty())
				FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(
						new LinkedTask(cache, world, startTime, newState, playerIn));
			else
			{
				long et = System.currentTimeMillis();
				playerIn.addChatComponentMessage(new TextComponentString("Done Graphing. Takes " + (et - startTime) + "ms"));
			}
		}
	}

	public static class SetTask implements Runnable
	{
		final World world;
		final EntityPlayer playerIn;
		final BlockPos pos;
		final Queue<Integer> resultsCoordinates;
		final long startTime;
		final IBlockState newState;

		public SetTask(World world, EntityPlayer playerIn, BlockPos pos, Queue<Integer> resultsCoordinates, long startTime, IBlockState newState)
		{
			this.world = world;
			this.playerIn = playerIn;
			this.pos = pos;
			this.resultsCoordinates = resultsCoordinates;
			this.startTime = startTime;
			this.newState = newState;
		}

		@Override
		public void run()
		{
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(
					new Runnable()
					{
						@Override
						public void run()
						{
							BlockPos.MutableBlockPos newPos = new BlockPos.MutableBlockPos();
							int counter = 0;

							while (!resultsCoordinates.isEmpty())
							{
								int newY = pos.getY() + resultsCoordinates.poll();
								if (newY > 255)
									newY = 255;
								else if (newY <= 1)
									newY = 1;
								//enforce the coordinates to be inside the world
								int newX = pos.getX() + resultsCoordinates.poll();
								int newZ = pos.getZ() + resultsCoordinates.poll();
								newPos.setPos(newX, newY, newZ);
								world.setBlockState(newPos, newState);
								counter++;
							}
							long et = System.currentTimeMillis();
							playerIn.addChatComponentMessage(new TextComponentString("Done Graphing. Takes " + (et - startTime) + "ms"));
							playerIn.addChatComponentMessage(new TextComponentString(counter + " Blocks Added."));
						}
					});
		}
	}

	//Determine the which block is going to be used

	private IBlockState determineTexture(String inp, EntityPlayer playerIn)
	{

		inp = inp.toLowerCase();

		if (inp.equals("black") || inp.equals("dark") || inp.equals("obsidian"))
		{
			playerIn.addChatComponentMessage(new TextComponentString("Apply Obsidian Blocks."));
			return Blocks.OBSIDIAN.getDefaultState();
		}

		if (inp.equals("white") || inp.equals("quartz"))
		{
			playerIn.addChatComponentMessage(new TextComponentString("Apply Quartz Blocks"));
			return Blocks.QUARTZ_BLOCK.getDefaultState();
		}

		if (inp.equals("tnt"))
		{
			playerIn.addChatComponentMessage(new TextComponentString("Apply TNTs. Be Careful, Stay Safe :3"));
			return Blocks.TNT.getDefaultState();
		}

		if (inp.equals("glass") || inp.equals("transparent"))
		{
			playerIn.addChatComponentMessage(new TextComponentString("Apply Glass Blocks."));
			return Blocks.GLASS.getDefaultState();
		}

		if (inp.equals("wood") || inp.equals("planks"))
		{
			playerIn.addChatComponentMessage(new TextComponentString("Apply Black Wood Planks."));
			return Blocks.PLANKS.getDefaultState();
		}

		if (inp.equals("red") || inp.equals("redstone"))
		{
			playerIn.addChatComponentMessage(new TextComponentString("Apply Redstond Block Blocks."));
			return Blocks.REDSTONE_BLOCK.getDefaultState();
		}

		if (inp.equals("stone") || inp.equals("gray") || inp.equals("cobblestone"))
		{
			playerIn.addChatComponentMessage(new TextComponentString("Apply Cobblestone Blocks."));
			return Blocks.COBBLESTONE.getDefaultState();
		}

		if (inp.equals("yellow") || inp.equals("glowing") || inp.equals("glowstone"))
		{
			playerIn.addChatComponentMessage(new TextComponentString("Apply Glowstone Blocks."));
			return Blocks.GLOWSTONE.getDefaultState();
		}

		if (inp.equals("gold") || inp.equals("golden"))
		{
			playerIn.addChatComponentMessage(new TextComponentString("Apply Gold Blocks."));
			return Blocks.GLOWSTONE.getDefaultState();
		}


		playerIn.addChatComponentMessage(new TextComponentString("Cannot identify the texture, Apply Iron Blocks."));
		return Blocks.IRON_BLOCK.getDefaultState();
	}
}
