
package rozo.alex.mathcraft.item;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.Queue;

public class ThreadGeneratingBlocks extends Thread{
    private IBlockState newState;
    private World worldIn;
    private ThreeDimensionalGraphing TDG;
    private Queue<Integer> resultsCoordinates;
    private EntityPlayer playerIn;
    private BlockPos pos;

    private class setOneBlockThread extends Thread {
        private World worldIn;
        private BlockPos mutPos;
        IBlockState newState;
        private int randerFlag;

        public setOneBlockThread(World worldin,BlockPos mutpos,IBlockState newstate, int randerFlag){
            this.worldIn=worldin;
            this.mutPos=mutpos;
            this.newState=newstate;
            this.randerFlag=randerFlag;
        }


        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName());
            worldIn.setBlockState(mutPos, newState, randerFlag);
        }
    }


    public ThreadGeneratingBlocks(IBlockState ns, World wi,ThreeDimensionalGraphing tdg,EntityPlayer ep, BlockPos ps){
        this.newState=ns;
        this.worldIn=wi;
        this.TDG=tdg;
        this.resultsCoordinates=TDG.getResults();
        this.playerIn=ep;
        this.pos=ps;

    }

    @Override
    public void run(){

        //what blocks will be used

        System.out.println(TDG.toString());
        playerIn.addChatComponentMessage(new TextComponentString("Multi Thread Mode, Start Graphing..."));
        playerIn.addChatComponentMessage(new TextComponentString("The orgin coordinates is ( "+String.valueOf(pos.getX())+", "+String.valueOf(pos.getY())+", "+String.valueOf(pos.getZ())+" )."));
        long startTime = System.currentTimeMillis();
        int counter=0;
        //Long currentTime=MinecraftServer.getCurrentTimeMillis();
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
            BlockPos mutPos=new BlockPos (newX,newY,newZ);
            //worldIn.getChunkFromBlockCoords(mutPos);

            setOneBlockThread sobt=new setOneBlockThread(worldIn,mutPos,newState,4);
            sobt.start();
            try {
                sobt.join(25);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException");
                e.printStackTrace();
            }


            counter++;
        }
        /////

        long et  = System.currentTimeMillis();
        playerIn.addChatComponentMessage(new TextComponentString("Done Graphing. Takes "+(et - startTime)+"ms"));
        playerIn.addChatComponentMessage(new TextComponentString(counter+" Blocks Added. "+resultsCoordinates.size()));


    }
}


