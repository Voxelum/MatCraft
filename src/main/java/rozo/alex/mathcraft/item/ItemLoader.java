package rozo.alex.mathcraft.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSign;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ItemLoader
{

    public static Item itemMatCraft=new ItemMatCraft();
    public static final Item.ToolMaterial REDSTONE = EnumHelper.addToolMaterial("REDSTONE", 3, 16, 16.0F, 0.0F, 10);



    public ItemLoader(FMLPreInitializationEvent event)
    {
        register(itemMatCraft, "mat_craft");
    }

    @SideOnly(Side.CLIENT)
    public static void registerRenders()
    {
        registerRender(itemMatCraft);
    }

    private static void register(Item item, String name)
    {
        GameRegistry.registerItem(item, name);
    }

    @SideOnly(Side.CLIENT)
    private static void registerRender(Item item)
    {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0,
                new ModelResourceLocation(GameData.getItemRegistry().getNameForObject(item).toString(), "inventory"));
    }
}