package rozo.alex.mathcraft.creativetab;


import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import rozo.alex.mathcraft.item.ItemLoader;

public class CreativeTabsLoader
{
    public static CreativeTabs tabTesting;

    public CreativeTabsLoader(FMLPreInitializationEvent event)
    {
        tabTesting = new CreativeTabs("tabFMLTutor")
        {
            @Override
            public Item getTabIconItem()
            {
                return ItemLoader.itemMatCraft;
            }
        };
    }
}