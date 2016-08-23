package rozo.alex.mathcraft;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import rozo.alex.mathcraft.common.CommonProxy;

@Mod (modid=MathCraft.MODID, name=MathCraft.NAME, version=MathCraft.VERSION,acceptedMinecraftVersions = "[1.8,)")

public class MathCraft {

    public static final String MODID = "mathcraft";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "mathcraft";

    @SidedProxy(clientSide = "rozo.alex.mathcraft.client.ClientProxy",
            serverSide = "rozo.alex.mathcraft.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MathCraft.MODID)
    public static MathCraft instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
