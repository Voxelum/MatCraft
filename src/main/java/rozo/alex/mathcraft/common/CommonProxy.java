package rozo.alex.mathcraft.common;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
//import rozo.alex.mathcraft.block.BlockLoader;
//import rozo.alex.mathcraft.crafting.CraftingLoader;
import rozo.alex.mathcraft.creativetab.CreativeTabsLoader;
import rozo.alex.mathcraft.item.ItemLoader;

public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {

        new CreativeTabsLoader(event);//must be in front of item and block
        new ItemLoader(event);

    }

    public void init(FMLInitializationEvent event)
    {


        new EventLoader();
    }

    public void postInit(FMLPostInitializationEvent event)
    {

    }
}