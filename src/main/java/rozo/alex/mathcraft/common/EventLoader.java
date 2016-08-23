package rozo.alex.mathcraft.common;


import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class EventLoader
{

    public static final EventBus EVENT_BUS = new EventBus();

    public EventLoader()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerItemPickup(PlayerEvent.ItemPickupEvent event)
    {
        if (event.player.isServerWorld())
        {
            event.player.addChatComponentMessage(new TextComponentString(event.pickedUp.toString()));

        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (!event.getWorld().isRemote)
        {
            event.getEntityPlayer().addChatComponentMessage(new TextComponentString("坐标 = " +event.getPos().toString()));
        }
    }

    @SubscribeEvent
    public void onPlayerClickGrassBlock(PlayerRightClickGrassBlockEvent event)
    {
        if (!event.world.isRemote)
        {
            BlockPos pos = event.pos;
            Entity tnt = new EntityTNTPrimed(event.world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, null);
            event.world.spawnEntityInWorld(tnt);
        }
    }


    @Cancelable
    public static class PlayerClickGrassBlockEvent extends PlayerInteractEvent.LeftClickBlock
    {
        public PlayerClickGrassBlockEvent(EntityPlayer player, BlockPos pos, EnumFacing face, Vec3d hitVec)
        {
            super(player,pos,face, hitVec);
        }
    }

    @Cancelable
    public static class PlayerRightClickGrassBlockEvent extends net.minecraftforge.event.entity.player.PlayerEvent
    {
        public final BlockPos pos;
        public final World world;

        public PlayerRightClickGrassBlockEvent(EntityPlayer player, BlockPos pos, World world)
        {
            super(player);
            this.pos = pos;
            this.world = world;
        }
    }



}
