package rozo.alex.mathcraft.item;

import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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


import java.util.LinkedList;
import java.util.Queue;


public class ItemMatCraft extends ItemSign{

    private String localName= "MatCraft";
    private boolean isNew = true;

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
        //player.addChatComponentMessage(new TextComponentString("--onItemUseFirst--" ));//for debug
        TileEntity tileentity = world.getTileEntity(pos);
        //player.addChatComponentMessage(new TextComponentString(String.valueOf(tileentity instanceof TileEntitySign) ));//for debug

        if (tileentity instanceof TileEntitySign )
        {
            this.isNew=false;
            graph3D((TileEntitySign) tileentity, player, world,  pos);
        }
        //player.addChatComponentMessage(new TextComponentString("--onItemUseFirst--" ));//for debug
        return EnumActionResult.PASS;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(this.isNew) {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            boolean flag = iblockstate.getBlock().isReplaceable(worldIn, pos);

            TileEntity tileentity = worldIn.getTileEntity(pos);
            playerIn.addChatComponentMessage(new TextComponentString(String.valueOf(tileentity instanceof TileEntitySign)));

            if (tileentity instanceof TileEntitySign) {
                graph3D((TileEntitySign) tileentity, playerIn, worldIn, pos);
            }


            if (facing != EnumFacing.DOWN && (iblockstate.getMaterial().isSolid() || flag) && (!flag || facing == EnumFacing.UP)) {
                pos = pos.offset(facing);

                if (playerIn.canPlayerEdit(pos, facing, stack) && Blocks.standing_sign.canPlaceBlockAt(worldIn, pos)) {
                    if (worldIn.isRemote) {
                        return EnumActionResult.SUCCESS;
                    } else {
                        pos = flag ? pos.down() : pos;

                        if (facing == EnumFacing.UP) {
                            int i = MathHelper.floor_double((double) ((playerIn.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
                            worldIn.setBlockState(pos, Blocks.standing_sign.getDefaultState().withProperty(BlockStandingSign.ROTATION, Integer.valueOf(i)), 11);
                        } else {
                            worldIn.setBlockState(pos, Blocks.wall_sign.getDefaultState().withProperty(BlockWallSign.FACING, facing), 11);

                        }

                        --stack.stackSize;
                        tileentity = worldIn.getTileEntity(pos);

                        if (tileentity instanceof TileEntitySign && !ItemBlock.setTileEntityNBT(worldIn, playerIn, pos, stack)) {
                            playerIn.openEditSign((TileEntitySign) tileentity);
                        }

                        return EnumActionResult.SUCCESS;
                    }
                } else {
                    return EnumActionResult.FAIL;
                }
            } else {
                return EnumActionResult.FAIL;
            }
        }else{
            isNew=true;
            return EnumActionResult.SUCCESS;
        }
    }

    private void graph3D(TileEntitySign tileentity, EntityPlayer playerIn ,World world, BlockPos pos) {
        String[] inps=new String[4];
        for (int i = 0; i < 4; ++i) {
            inps[i]=tileentity.signText[i].getUnformattedText();
            playerIn.addChatComponentMessage(new TextComponentString(tileentity.signText[i].getUnformattedText() ));
        }
        if(!isEmpty(inps)){
            int [] numberOfParameters = findNumberOfParameters(inps[0]);//potential to upgrade to parametric
            if(numberOfParameters==null ){//check number of brakets
                System.out.println("numberOfBraketsError or parametricError");
                playerIn.addChatComponentMessage(new TextComponentString("Not Valid For Graphing."));
                return;
            }                             //check number of brakets

            if(numberOfParameters[2]!=0){
                doParametrized(inps, world, pos , playerIn);
            }else {
                doGraph(inps, world, pos, playerIn);
            }
        }else{
            playerIn.addChatComponentMessage(new TextComponentString("Not a Valid Input For Graphing."));
        }

    }

    private void doParametrized(String[] inps, World world, BlockPos pos, EntityPlayer playerIn) {
        Queue<Integer> Xvalues;
        Queue<Integer> Yvalues;
        Queue<Integer> Zvalues;
        Double t_start=toBountry(inps[3],true);
        Double t_end=toBountry(inps[3],false);
        IBlockState newState;

        if(t_start!=null && t_end!=null) {
            Xvalues = inps[0].replaceAll(" ","").equals("")?makeZeros(t_start, t_end):new ThreeDimensionalGraphing(inps[0], t_start, t_end).getResults();
            Yvalues = inps[1].replaceAll(" ","").equals("")?makeZeros(t_start, t_end):new ThreeDimensionalGraphing(inps[1], t_start, t_end).getResults();
            Zvalues = inps[2].replaceAll(" ","").equals("")?makeZeros(t_start, t_end):new ThreeDimensionalGraphing(inps[2], t_start, t_end).getResults();
            newState=Blocks.iron_block.getDefaultState();
        }else{
            Xvalues = inps[0].replaceAll(" ","").equals("")?makeZeros(0, 20):new ThreeDimensionalGraphing(inps[0], 0, 20).getResults();
            Yvalues = inps[1].replaceAll(" ","").equals("")?makeZeros(0, 20):new ThreeDimensionalGraphing(inps[1], 0, 20).getResults();
            Zvalues = inps[2].replaceAll(" ","").equals("")?makeZeros(0, 20):new ThreeDimensionalGraphing(inps[2], 0, 20).getResults();
            newState=determineTexture(inps[3], playerIn);//determine the texture of the graph
        }

        if(Xvalues==null||Yvalues==null||Zvalues==null){
            playerIn.addChatComponentMessage(new TextComponentString("Not void for Graphing"));
            return;
        }


        playerIn.addChatComponentMessage(new TextComponentString("Start Graphing..."));
        playerIn.addChatComponentMessage(new TextComponentString("The orgin coordinates is ( "+String.valueOf(pos.getX())+", "+String.valueOf(pos.getY())+", "+String.valueOf(pos.getZ())+" )."));
        long startTime = System.currentTimeMillis();
        int counter=0;
        while (!Xvalues.isEmpty()){
            int newY=pos.getY()+Yvalues.poll();
            int newX=pos.getX()+Xvalues.poll();;
            int newZ=pos.getZ()+Zvalues.poll();;

            Yvalues.poll();
            Xvalues.poll();;
            Zvalues.poll();;

            if (newY>255){
                newY=255;
            }else if(newY<=1){
                newY=1;
            }//enforce the coordinates to be inside the world

            System.out.println("("+newX+","+newY+","+newZ+")\n" );
            BlockPos newPos=new BlockPos(newX,newY,newZ);
            world.setBlockState(newPos, newState);
            counter++;
        }

        long et  = System.currentTimeMillis();
        playerIn.addChatComponentMessage(new TextComponentString("Done Graphing. Takes "+(et - startTime)+"ms"));
        playerIn.addChatComponentMessage(new TextComponentString(counter+" Blocks Added."));
        /////


    }

    private Queue<Integer> makeZeros(double t_start, double t_end) {
        Queue<Integer> zeros=new LinkedList<Integer>();
        if(t_start>t_end){
            double temp=t_start;
            t_start=t_end;
            t_end=temp;
        }
        int ts = (int)Math.ceil(t_start);
        int te = (int)t_end;
        for(int i=ts;i<=te;i++){
            zeros.add(0);
        }
        return zeros;
    }

    //The method actually does the work
    private void doGraph(String[] inps, World world, BlockPos pos , EntityPlayer playerIn) {


        Double x_lower=toBountry(inps[1],true);
        Double x_upper=toBountry(inps[1],false);
        Double z_lower=toBountry(inps[2],true);
        Double z_upper=toBountry(inps[2],false);


        //Start

        ThreeDimensionalGraphing TDG;
        Queue<Integer> resultsCoordinates;
        inps[0]=inps[0].replaceAll("z","y");
        inps[0]=inps[0].replaceAll("Z","y");

        long startTime = System.currentTimeMillis();
        if(x_lower!=null && x_upper!=null && z_lower!=null && z_upper!=null) {
            TDG = new ThreeDimensionalGraphing(inps[0], x_lower, x_upper, z_lower, z_upper);
            long endTime   = System.currentTimeMillis();
            playerIn.addChatComponentMessage(new TextComponentString("Generating z values takes "+(endTime - startTime)+"ms"));
        }else if ((x_lower!=null && x_upper!=null)  ){
            TDG = new ThreeDimensionalGraphing(inps[0], x_lower, x_upper, 0, 0);
            long endTime   = System.currentTimeMillis();
            playerIn.addChatComponentMessage(new TextComponentString("Generating z values takes "+(endTime - startTime)+"ms"));

        }else if(z_lower!=null && z_upper!=null){
            TDG = new ThreeDimensionalGraphing(inps[0], 0, 0,z_lower, z_upper);
            long endTime   = System.currentTimeMillis();
            playerIn.addChatComponentMessage(new TextComponentString("Generating z values takes "+(endTime - startTime)+"ms"));

        }else if(x_lower==null && x_upper==null && z_lower==null && z_upper==null){
            TDG = new ThreeDimensionalGraphing(inps[0]);
            long endTime   = System.currentTimeMillis();
            playerIn.addChatComponentMessage(new TextComponentString("Done calculations. Takes "+(endTime - startTime)+"ms"));
        } else{
            playerIn.addChatComponentMessage(new TextComponentString("Not a Valid Domain For Graphing."));
            return;
        }

        if(TDG.getPostfixOperations()==null){
            playerIn.addChatComponentMessage(new TextComponentString("Fail to Graph. The expression of the function might be wrong."));
        }else{
            IBlockState newState=determineTexture(inps[3], playerIn);//determine the texture of the graph
            //what blocks will be used

           resultsCoordinates=TDG.getResults();
            //System.out.println(TDG.toString());
           playerIn.addChatComponentMessage(new TextComponentString("Start Graphing..."));
           playerIn.addChatComponentMessage(new TextComponentString("The orgin coordinates is ( "+String.valueOf(pos.getX())+", "+String.valueOf(pos.getY())+", "+String.valueOf(pos.getZ())+" )."));
            startTime = System.currentTimeMillis();
            int counter=0;
            while (!resultsCoordinates.isEmpty()){
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
                world.setBlockState(newPos, newState);
                counter++;
            }

            long et  = System.currentTimeMillis();
            playerIn.addChatComponentMessage(new TextComponentString("Done Graphing. Takes "+(et - startTime)+"ms"));
            playerIn.addChatComponentMessage(new TextComponentString(counter+" Blocks Added."+resultsCoordinates.size()));
            /////

        }
    }

    //Determine the which block is going to be used
    private IBlockState determineTexture(String inp, EntityPlayer playerIn) {

        inp=inp.toLowerCase();

        if(inp.equals("black")||inp.equals("dark")||inp.equals("obsidian")){
            playerIn.addChatComponentMessage(new TextComponentString("Apply Obsidian Blocks."));
            return Blocks.obsidian.getDefaultState();
        }

        if(inp.equals("white")||inp.equals("quartz")){
            playerIn.addChatComponentMessage(new TextComponentString("Apply Quartz Blocks"));
            return Blocks.quartz_block.getDefaultState();
        }

        if(inp.equals("tnt")){
            playerIn.addChatComponentMessage(new TextComponentString("Apply TNTs. Be Careful, Stay Safe :3"));
            return Blocks.tnt.getDefaultState();
        }

        if(inp.equals("glass")||inp.equals("transparent")){
            playerIn.addChatComponentMessage(new TextComponentString("Apply Glass Blocks."));
            return Blocks.glass.getDefaultState();
        }

        if(inp.equals("wood")||inp.equals("planks")){
            playerIn.addChatComponentMessage(new TextComponentString("Apply Black Wood Planks."));
            return Blocks.planks.getDefaultState();
        }

        if(inp.equals("red")||inp.equals("redstone")){
            playerIn.addChatComponentMessage(new TextComponentString("Apply Redstond Block Blocks."));
            return Blocks.redstone_block.getDefaultState();
        }

        if(inp.equals("stone")||inp.equals("gray")||inp.equals("cobblestone")){
            playerIn.addChatComponentMessage(new TextComponentString("Apply Cobblestone Blocks."));
            return Blocks.cobblestone.getDefaultState();
        }

        if(inp.equals("yellow")||inp.equals("glowing")||inp.equals("glowstone")){
            playerIn.addChatComponentMessage(new TextComponentString("Apply Glowstone Blocks."));
            return Blocks.glowstone.getDefaultState();
        }

        if(inp.equals("gold")||inp.equals("golden")){
            playerIn.addChatComponentMessage(new TextComponentString("Apply Gold Blocks."));
            return Blocks.glowstone.getDefaultState();
        }


        playerIn.addChatComponentMessage(new TextComponentString("Cannot identify the texture, Apply Iron Blocks."));
        return Blocks.iron_block.getDefaultState();
    }

    private int[] findNumberOfParameters(String inps) {
        int[] nofP =new int[3];
        int braketCounter=0;
        for(int i=0;i<inps.length();i++){
            if(inps.charAt(i)=='('){
                braketCounter++;
            }else if (inps.charAt(i)==')'){
                braketCounter--;
            }
            if(braketCounter<0){
                return null;
            }
            switch(inps.charAt(i)){
                case 'x':case'X':nofP[0]++;
                    break;
                case 'Z':case'z':nofP[1]++;
                    break;
                case 't':case'T':nofP[2]++;
            }

        }

        if(nofP[2]!=0 &&(nofP[0]!=0||nofP[1]!=0)){
            return null;
        }

        if(braketCounter==0 ) {
            return nofP;
        }else{
            return null;
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
                if(isOn && inp.charAt(i)!=' '){
                    temp=temp+String.valueOf(inp.charAt(i));
                }
                if(inp.charAt(i)==','){
                    isOn=true;
                }

            }
        }
        if(isNumeric(temp)){
            return Double.parseDouble(temp);
        }else{
            System.out.println("null situation: "+temp);
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



    private boolean isEmpty(String[] inps) {
        String s="";
        for (int i = 0; i < 4; ++i) {
            s=s+inps[i];
        }
        return(s.replaceAll(" ","").equals(""));
    }




}
