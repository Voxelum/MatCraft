package rozo.alex.mathcraft.client;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rozo.alex.mathcraft.common.CommonProxy;
import rozo.alex.mathcraft.common.FinalFieldUtils;

import java.lang.reflect.Field;


public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
		try
		{
			Field f = RenderGlobal.class.getDeclaredField("setLightUpdates");
			RenderGlobal instance = Minecraft.getMinecraft().renderGlobal;
			FinalFieldUtils.INSTANCE.set(instance, f, Sets.<BlockPos>newConcurrentHashSet());
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		new ItemRenderLoader();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
}
