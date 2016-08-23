package rozo.alex.mathcraft.item;

import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemStack;
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
import rozo.alex.mathcraft.creativetab.CreativeTabsLoader;

import java.util.Queue;


public class ItemMatCraft extends ItemSign{

    private String localName= "MatCraft";

    public ItemMatCraft()
    {
        super();
        this.setUnlocalizedName(localName);
        this.setCreativeTab(CreativeTabsLoader.tabTesting);
    }

    public String getName(){
        return localName;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        playerIn.addChatComponentMessage(new TextComponentString("onItenRightClick" ));

        return new ActionResult(EnumActionResult.PASS, itemStackIn);
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        player.addChatComponentMessage(new TextComponentString("--onItemUseFirst--" ));
        TileEntity tileentity = world.getTileEntity(pos);
        player.addChatComponentMessage(new TextComponentString(String.valueOf(tileentity instanceof TileEntitySign) ));

        if (tileentity instanceof TileEntitySign )
        {
            graph3D((TileEntitySign) tileentity, player, world,  pos);
        }
        player.addChatComponentMessage(new TextComponentString("--onItemUseFirst--" ));
        return EnumActionResult.PASS;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        boolean flag = iblockstate.getBlock().isReplaceable(worldIn, pos);

        TileEntity tileentity = worldIn.getTileEntity(pos);
        playerIn.addChatComponentMessage(new TextComponentString(String.valueOf(tileentity instanceof TileEntitySign) ));

        if (tileentity instanceof TileEntitySign )
        {
            graph3D((TileEntitySign) tileentity, playerIn,worldIn, pos);
        }


        if (facing != EnumFacing.DOWN && (iblockstate.getMaterial().isSolid() || flag) && (!flag || facing == EnumFacing.UP))
        {
            pos = pos.offset(facing);

            if (playerIn.canPlayerEdit(pos, facing, stack) && Blocks.standing_sign.canPlaceBlockAt(worldIn, pos))
            {
                if (worldIn.isRemote)
                {
                    return EnumActionResult.SUCCESS;
                }
                else
                {
                    pos = flag ? pos.down() : pos;

                    if (facing == EnumFacing.UP)
                    {
                        int i = MathHelper.floor_double((double)((playerIn.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
                        worldIn.setBlockState(pos, Blocks.standing_sign.getDefaultState().withProperty(BlockStandingSign.ROTATION, Integer.valueOf(i)), 11);
                    }
                    else
                    {
                        worldIn.setBlockState(pos, Blocks.wall_sign.getDefaultState().withProperty(BlockWallSign.FACING, facing), 11);

                    }

                    --stack.stackSize;
                    tileentity = worldIn.getTileEntity(pos);

                    if (tileentity instanceof TileEntitySign && !ItemBlock.setTileEntityNBT(worldIn, playerIn, pos, stack))
                    {
                        playerIn.openEditSign((TileEntitySign)tileentity);
                    }

                    return EnumActionResult.SUCCESS;
                }
            }
            else
            {
                return EnumActionResult.FAIL;
            }
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }

    private void graph3D(TileEntitySign tileentity, EntityPlayer playerIn ,World world, BlockPos pos) {
        String[] inps=new String[4];
        for (int i = 0; i < 4; ++i) {
            inps[i]=tileentity.signText[i].getUnformattedText();
            playerIn.addChatComponentMessage(new TextComponentString(tileentity.signText[i].getUnformattedText() + "  yoyoyoyo"));
        }
        if(isOkForGraphing(inps)){
            doGraph(inps,world,pos,playerIn);
        }else{
            playerIn.addChatComponentMessage(new TextComponentString("Not a Valid Input For Graphing."));
        }

    }

    private void doGraph(String[] inps, World world, BlockPos pos , EntityPlayer playerIn) {
        Double x_lower=toBountry(inps[1],true);
        Double x_upper=toBountry(inps[1],false);
        Double z_lower=toBountry(inps[2],true);
        Double z_upper=toBountry(inps[2],false);
        ThreeDimensionalGraphing TDG;
        Queue<Integer> resultsCoordinates;
        inps[0]=inps[0].replaceAll("z","y");
        inps[0]=inps[0].replaceAll("Z","y");

        if(x_lower!=null && x_upper!=null && z_lower!=null && z_upper!=null) {
            TDG = new ThreeDimensionalGraphing(inps[0], x_lower, x_upper, z_lower, z_upper);
        }else if (x_lower!=null && x_upper!=null){
            TDG = new ThreeDimensionalGraphing(inps[0], x_lower, x_upper, 0, 0);
        }else if(z_lower!=null && z_upper!=null){
            TDG = new ThreeDimensionalGraphing(inps[0], 0, 0,z_lower, z_upper);
        }else if(x_lower==null && x_upper==null && z_lower==null && z_upper==null){
            TDG = new ThreeDimensionalGraphing(inps[0]);
        } else{
            playerIn.addChatComponentMessage(new TextComponentString("Not a Valid Domain For Graphing."));
            return;
        }

        if(TDG.getPostfixOperations()==null){
            playerIn.addChatComponentMessage(new TextComponentString("Fail to Graph. The expression of the function might be wrong.\nPlease Fllow the instruction.\n"+TDG.discription));
        }else{
            resultsCoordinates=TDG.getResults();
            playerIn.addChatComponentMessage(new TextComponentString(TDG.toString()));
            playerIn.addChatComponentMessage(new TextComponentString(String.valueOf(resultsCoordinates.peek())));
            playerIn.addChatComponentMessage(new TextComponentString(String.valueOf(pos.getX())));
            playerIn.addChatComponentMessage(new TextComponentString(String.valueOf(pos.getY())));
            playerIn.addChatComponentMessage(new TextComponentString(String.valueOf(pos.getZ())));
            for (int i=0;i<resultsCoordinates.size();i++){
                System.out.println("." );
                int newY=pos.getY()+resultsCoordinates.poll();
                if (newY>255){
                    newY=255;
                }else if(newY<=1){
                    newY=1;
                }//enforce the coordinates to be inside the world
                int newX=pos.getX()+resultsCoordinates.poll();
                int newZ=pos.getZ()+resultsCoordinates.poll();
                System.out.println("("+newX+","+newY+","+newZ+")\n" );
                BlockPos newPos=new BlockPos(newX,newY,newZ);
                world.setBlockState(newPos, Blocks.iron_block.getDefaultState(), 2);
            }
        }
    }

    private Double toBountry(String inp, boolean isOn) {
        String temp="";
        if(isOn){
            for(int i=0;i<inp.length();i++){
                if(inp.charAt(i)==','){
                    isOn=false;
                }
                if(isOn && inp.charAt(i)!=' '){
                    temp=temp+String.valueOf(inp.charAt(i));
                }
            }
        }else{
            for(int i=0;i<inp.length();i++){
                if(inp.charAt(i)==','){
                    isOn=true;
                }
                if(isOn && inp.charAt(i)!=' '){
                    temp=temp+String.valueOf(inp.charAt(i));
                }
            }
        }
        if(isNumeric(temp)){
            return Double.parseDouble(temp);
        }else{
            return null;
        }
    }

    private boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private boolean isOkForGraphing(String[] inps) {
        return isEmpty(inps);


    }

    private boolean isEmpty(String[] inps) {
        String s="";
        for (int i = 0; i < 4; ++i) {
            s=s+inps[i];
        }
        return(!s.replaceAll(" ","").equals(""));
    }


}
