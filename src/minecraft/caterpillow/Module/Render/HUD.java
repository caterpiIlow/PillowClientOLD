package caterpillow.Module.Render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;

import caterpillow.Client.Client;
import caterpillow.Module.Category;
import caterpillow.Module.Module;
import caterpillow.Module.ModuleManager;
import caterpillow.event.EventTarget;
import caterpillow.event.events.Event2D;
import de.Hero.clickgui.util.ColorUtil;
import de.Hero.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class HUD extends Module {

	public HUD() {
		super("HUD", Keyboard.KEY_MULTIPLY, Category.RENDER);

	}

	@Override
	public void setup() {
		ArrayList<String> options = new ArrayList<>();
		options.add("Clean");
		options.add("Lined");
		options.add("Boxed");
		options.add("Outlined");
		Client.instance.settingsManager.rSetting(new Setting("Style", this, "Outlined", options));
		ArrayList<String> options2 = new ArrayList<>();
		options2.add("None");
		options2.add("Category");
		options2.add("Rainbow");
		Client.instance.settingsManager.rSetting(new Setting("Text Color", this, "Rainbow", options2));
		Client.instance.settingsManager.rSetting(new Setting("Color delay", this, 20, 0, 100, "HUDDelay", true));
		Client.instance.settingsManager.rSetting(new Setting("Offset", this, 20, 0, 100, "RainbowDelay", true));
		Client.instance.settingsManager.rSetting(new Setting("Client", this, true));
		Client.instance.settingsManager.rSetting(new Setting("Name scale", this, 2, 0.1, 15, true, 0.1));
		Client.instance.settingsManager.rSetting(new Setting("Modules", this, true));
		Client.instance.settingsManager.rSetting(new Setting("Module scale", this, 1, 0, 15, true, 0.1));
		Client.instance.settingsManager.rSetting(new Setting("Potions", this, true));
		Client.instance.settingsManager.rSetting(new Setting("Potion scale", this, 1, 0.1, 15, true, 0.1));
		Client.instance.settingsManager.rSetting(new Setting("PPSP", this, true));

	}

	public ModuleManager moduleManager;
	public EntityLivingBase entityLivingBase;

	public Minecraft mc = Minecraft.getMinecraft();

	public static class ModuleComparator implements Comparator<Module> {

		public int compare(Module module1, Module module2) {
			if (Minecraft.getMinecraft().fontRendererObj
					.getStringWidth(module1.getDisplayName()) > Minecraft.getMinecraft().fontRendererObj
							.getStringWidth(module2.getDisplayName()))
				return -1;
			else
				return 1;
		}

	}

	public static int rainbow(double offset, double delay) {
		double rainbowState = Math.ceil((System.currentTimeMillis() - offset * 20) / delay);
		rainbowState %= 360;
		return Color.getHSBColor((float) (rainbowState / 360.0f), 1f, 1f).getRGB();
	}

	public int getHexColorModule(Module module, int count) {

		if (settingEquals("Text Color", "None")) {
			return 0xFFFFFFFF;
		} else if (settingEquals("Text Color", "Category")) {
			return 0xFFFFFFFF;
		} else if (settingEquals("Text Color", "Rainbow")) {
			return rainbow(count * Client.instance.settingsManager.getSettingByName("Offset").getValDouble(),
					Client.instance.settingsManager.getSettingByID("HUDDelay").getValDouble());
		}
		return -1;

	}

	private static String getPotionDisplay(PotionEffect p) {
		String potionName = Potion.potionTypes[p.getPotionID()].getName();
		potionName = I18n.format(potionName, new Object[0]);

		if (p.getAmplifier() == 0) {
			potionName = potionName + " I ";
		} else if (p.getAmplifier() == 1) {
			potionName = potionName + " II ";
		} else if (p.getAmplifier() == 2) {
			potionName = potionName + " III ";
		} else if (p.getAmplifier() == 3) {
			potionName = potionName + " IV ";
		} else if (p.getAmplifier() == 4) {
			potionName = potionName + " V ";
		}
		potionName = potionName + Potion.getDurationString(p);
		return potionName;
	}

	public static class PotionComparator implements Comparator<PotionEffect> {

		public int compare(PotionEffect potion1, PotionEffect potion2) {
			if (Minecraft.getMinecraft().fontRendererObj
					.getStringWidth(getPotionDisplay(potion1)) > Minecraft.getMinecraft().fontRendererObj
							.getStringWidth(getPotionDisplay(potion2)))
				return -1;
			else
				return 1;
		}

	}

	private String potionColor(Potion potion) {
		if (potion.isBadEffect()) {
			return "§c";
		} else {
			return "§a";

		}
	}

//	private void drawString(String text, double x, double y, int color) {
//		
//	}
	private long RGBtoHex(Color RGB) {
		long decimal = RGB.getRed() * 65536 + RGB.getBlue() * 256 + RGB.getGreen() + 4278190080L;
		return Long.parseLong(Long.toHexString(decimal), 16);
	}

	private int potionHexColor(Potion potion) {
		if (potion.isBadEffect()) {
			return 0xFFFF5555;
		} else {
			return 0xFF55FF55;

		}
	}

	private boolean settingEquals(String setting, String name) {
		return Client.instance.settingsManager.getSettingByName(setting).getValString().equalsIgnoreCase(name);
	}

	@EventTarget
	public void onEvent2D(Event2D event) {

		double clientScale = Client.instance.settingsManager.getSettingByName("Name scale").getValDouble();
		double moduleScale = Client.instance.settingsManager.getSettingByName("Module scale").getValDouble();
		double potionScale = Client.instance.settingsManager.getSettingByName("Potion scale").getValDouble();

		ScaledResolution sr = new ScaledResolution(mc);

		FontRenderer fr = mc.fontRendererObj;

		ColorUtil colorUtil;

		List<Module> sortedModules = new ArrayList<Module>();
//
//		sortedModules = moduleManager.getModules();

		Collections.sort(moduleManager.modules, new ModuleComparator());

		sortedModules = moduleManager.modules.stream().filter(module -> module.isToggled())
				.collect(Collectors.toList());

//		for (Module m : sortedModules) {
//			if (!m.isToggled())
//				sortedModules.remove(m);
//		}

		ArrayList<PotionEffect> potionNames = new ArrayList();

//
//		System.out.println(potions);
//
		for (PotionEffect p : mc.thePlayer.getActivePotionEffects()) {
			potionNames.add(p);

		}
//
//		System.out.println(potions);

		Collections.sort(potionNames, new PotionComparator());
//
////		System.out.println(potions);
//		(0xff << 24)
//		| (((int) Client.instance.settingsManager.getSettingByName("GuiGreen")
//		.getValDouble()
//				& 0xff) << 16)
//		| (((int) Client.instance.settingsManager.getSettingByName("GuiGreen")
//				.getValDouble() & 0xff) << 8)
//		| ((int) Client.instance.settingsManager.getSettingByName("GuiBlue").getValDouble()
//				& 0xff)
		if (Client.instance.settingsManager.getSettingByName("Client").getValBoolean()) {
			GlStateManager.translate(4.0, 4.0, 0.0);
			GlStateManager.scale(clientScale, clientScale, 1.0);
			GlStateManager.translate(-4.0, -4.0, 0.0);
			if (settingEquals("Text Color", "Rainbow")) {
				fr.drawStringWithShadow(Client.instance.name + " Client v" + Client.instance.version, 4, 4,
						rainbow(0, Client.instance.settingsManager.getSettingByName("Color Delay").getValDouble()));
			} else if (settingEquals("Text Color", "Category")) {
				fr.drawStringWithShadow(Client.instance.name + " Client v" + Client.instance.version, 4, 4, (0xff << 24)
						| (((int) Client.instance.settingsManager.getSettingByName("GuiGreen").getValDouble()
								& 0xff) << 16)
						| (((int) Client.instance.settingsManager.getSettingByName("GuiGreen").getValDouble()
								& 0xff) << 8)
						| ((int) Client.instance.settingsManager.getSettingByName("GuiBlue").getValDouble() & 0xff));
			} else if (settingEquals("Text Color", "None")) {
				fr.drawStringWithShadow(Client.instance.name + " Client v" + Client.instance.version, 4, 4, -1);
			}

			GlStateManager.translate(4.0, 4.0, 0.0);
			GlStateManager.scale(1 / clientScale, 1 / clientScale, 1.0);
			GlStateManager.translate(-4.0, -4.0, 0.0);
		}

		int count2 = 0;
		double potionYOffset;
		if (Client.instance.settingsManager.getSettingByName("Client").getValBoolean())
			potionYOffset = (fr.FONT_HEIGHT) * clientScale + 10;
		else
			potionYOffset = 2;

		if (Client.instance.settingsManager.getSettingByName("Potions").getValBoolean()) {

			GlStateManager.translate(0, potionYOffset, 0.0);
			GlStateManager.scale(potionScale, potionScale, 1.0);
			GlStateManager.translate(0, -potionYOffset, 0.0);

			for (PotionEffect p : potionNames) {
				if (!(p.getDuration() > 0))
					continue;

				String potionName = getPotionDisplay(p);

				if (settingEquals("Style", "Clean")) {

					this.setDisplayName("HUD §7Clean");
					fr.drawStringWithShadow(potionName, 4, (int) potionYOffset + count2 * (fr.FONT_HEIGHT + 2),
							settingEquals("Text Color", "None") ? -1
									: Potion.potionTypes[p.getPotionID()].getLiquidColor());

				} else if (settingEquals("Style", "Lined")) {
					this.setDisplayName("HUD §7Lined");

					Gui.drawRect(/* x */0, /* y */count2 * (fr.FONT_HEIGHT + 6) + potionYOffset,
							/* x2 */fr.getStringWidth(potionName) + 8,
							/* y2 */6 + potionYOffset + count2 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT, 0x90000000);
					Gui.drawRect(/* x */fr.getStringWidth(potionName) + 8,
							/* y */count2 * (fr.FONT_HEIGHT + 6) + potionYOffset,
							/* x2 */fr.getStringWidth(potionName) + 10,
							/* y2 */6 + potionYOffset + count2 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT,
							settingEquals("Text Color", "None") ? 0xFFFFFFFF
									: Long.parseLong(
											Long.toHexString(
													Potion.potionTypes[p.getPotionID()].getLiquidColor() + 4278190080L),
											16));

					fr.drawStringWithShadow(potionName, 4, 4 + (int) potionYOffset + count2 * (fr.FONT_HEIGHT + 6),
							settingEquals("Text Color", "None") ? -1
									: Potion.potionTypes[p.getPotionID()].getLiquidColor());

				} else if (settingEquals("Style", "Boxed")) {
					this.setDisplayName("HUD §7Boxed");

					if (count2 == 0) {
						Gui.drawRect(/* x */fr.getStringWidth(potionName) + 10,
								/* y */-1 * (fr.FONT_HEIGHT + 6) + potionYOffset + fr.FONT_HEIGHT + 4, /* x2 */0,
								/* y2 */6 + potionYOffset + -1 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT,
								settingEquals("Text Color", "None") ? 0xFFFFFFFF
										: Long.parseLong(Long.toHexString(
												Potion.potionTypes[p.getPotionID()].getLiquidColor() + 4278190080L),
												16));
					}
					Gui.drawRect(/* x */0, /* y */count2 * (fr.FONT_HEIGHT + 6) + potionYOffset,
							/* x2 */fr.getStringWidth(potionName) + 8,
							/* y2 */6 + potionYOffset + count2 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT, 0x90000000);
					Gui.drawRect(/* x */fr.getStringWidth(potionName) + 8,
							/* y */count2 * (fr.FONT_HEIGHT + 6) + potionYOffset,
							/* x2 */fr.getStringWidth(potionName) + 10,
							/* y2 */6 + potionYOffset + count2 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT,
							settingEquals("Text Color", "None") ? 0xFFFFFFFF
									: Long.parseLong(
											Long.toHexString(
													Potion.potionTypes[p.getPotionID()].getLiquidColor() + 4278190080L),
											16));
					Gui.drawRect(/* x */fr.getStringWidth(potionName) + 10,
							/* y */count2 * (fr.FONT_HEIGHT + 6) + potionYOffset + fr.FONT_HEIGHT + 4, /* x2 */0,
							/* y2 */6 + potionYOffset + count2 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT,
							settingEquals("Text Color", "None") ? 0xFFFFFFFF
									: Long.parseLong(
											Long.toHexString(
													Potion.potionTypes[p.getPotionID()].getLiquidColor() + 4278190080L),
											16));

					fr.drawStringWithShadow(potionName, 4, 3 + (int) potionYOffset + count2 * (fr.FONT_HEIGHT + 6),
							settingEquals("Text Color", "None") ? -1
									: Potion.potionTypes[p.getPotionID()].getLiquidColor());

				} else if (settingEquals("Style", "Outlined")) {
					this.setDisplayName("HUD §7Outlined");

					if (count2 == 0) {
						Gui.drawRect(/* x */fr.getStringWidth(potionName) + 10,
								/* y */-1 * (fr.FONT_HEIGHT + 6) + potionYOffset + fr.FONT_HEIGHT + 4, /* x2 */0,
								/* y2 */6 + potionYOffset + -1 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT,
								settingEquals("Text Color", "None") ? 0xFFFFFFFF
										: Long.parseLong(Long.toHexString(
												Potion.potionTypes[p.getPotionID()].getLiquidColor() + 4278190080L),
												16));
					}
					Gui.drawRect(/* x */0, /* y */count2 * (fr.FONT_HEIGHT + 6) + potionYOffset,
							/* x2 */fr.getStringWidth(potionName) + 8,
							/* y2 */6 + potionYOffset + count2 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT, 0x90000000);

					Gui.drawRect(/* x */fr.getStringWidth(potionName) + 8,
							/* y */count2 * (fr.FONT_HEIGHT + 6) + potionYOffset,
							/* x2 */fr.getStringWidth(potionName) + 10,
							/* y2 */6 + potionYOffset + count2 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT,
							settingEquals("Text Color", "None") ? 0xFFFFFFFF
									: Long.parseLong(
											Long.toHexString(
													Potion.potionTypes[p.getPotionID()].getLiquidColor() + 4278190080L),
											16));
					if (count2 == potionNames.size() - 1) {

						Gui.drawRect(/* x */fr.getStringWidth(potionName) + 10,
								/* y */count2 * (fr.FONT_HEIGHT + 6) + potionYOffset + fr.FONT_HEIGHT + 4, /* x2 */0,
								/* y2 */6 + potionYOffset + count2 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT,
								settingEquals("Text Color", "None") ? 0xFFFFFFFF
										: Long.parseLong(Long.toHexString(
												Potion.potionTypes[p.getPotionID()].getLiquidColor() + 4278190080L),
												16));
					} else {
						Gui.drawRect(/* x */fr.getStringWidth(potionName) + 10,
								/* y */count2 * (fr.FONT_HEIGHT + 6) + potionYOffset + fr.FONT_HEIGHT + 4,
								/* x2 */fr.getStringWidth(getPotionDisplay(potionNames.get(count2 + 1))) + 8,
								/* y2 */6 + potionYOffset + count2 * (fr.FONT_HEIGHT + 6) + fr.FONT_HEIGHT,
								settingEquals("Text Color", "None") ? 0xFFFFFFFF
										: Long.parseLong(Long.toHexString(
												Potion.potionTypes[p.getPotionID()].getLiquidColor() + 4278190080L),
												16));
					}

					fr.drawStringWithShadow(potionName, 4, 3 + (int) potionYOffset + count2 * (fr.FONT_HEIGHT + 6),
							settingEquals("Text Color", "None") ? -1
									: Potion.potionTypes[p.getPotionID()].getLiquidColor());

				}

				count2++;
			}
			GlStateManager.translate(0, potionYOffset, 0.0);
			GlStateManager.scale(1 / potionScale, 1 / potionScale, 1.0);
			GlStateManager.translate(0, -potionYOffset, 0.0);
		}

		int count = 0;

		if (Client.instance.settingsManager.getSettingByName("Modules").getValBoolean()) {

			GlStateManager.translate(sr.getScaledWidth(), 0, 0.0);
			GlStateManager.scale(moduleScale, moduleScale, 1.0);
			GlStateManager.translate(-sr.getScaledWidth(), 0, 0.0);

			for (Module m : sortedModules) {
				// System.out.println(m.getName());

				if (settingEquals("Style", "Clean")) {
					this.setDisplayName("HUD §7Clean");

					fr.drawStringWithShadow(m.getDisplayName(),
							sr.getScaledWidth() - 4 - fr.getStringWidth(m.getDisplayName()),
							count * (fr.FONT_HEIGHT + 2) + 2, getHexColorModule(m, count));

				} else if (settingEquals("Style", "Lined")) {
					this.setDisplayName("HUD §7Lined");

					Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 8,
							count * (fr.FONT_HEIGHT + 6), sr.getScaledWidth(),
							6 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), 0x90000000);
					Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 10,
							count * (fr.FONT_HEIGHT + 6),
							sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 8,
							6 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), m.getHexColor());
					fr.drawStringWithShadow(m.getDisplayName(),
							sr.getScaledWidth() - 4 - fr.getStringWidth(m.getDisplayName()),
							4 + count * (fr.FONT_HEIGHT + 6), getHexColorModule(m, count));

				} else if (settingEquals("Style", "Boxed")) {
					this.setDisplayName("HUD §7Boxed");

					Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 8,
							count * (fr.FONT_HEIGHT + 6), sr.getScaledWidth(),
							6 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), 0x90000000);
					Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 10,
							count * (fr.FONT_HEIGHT + 6),
							sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 8,
							6 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), m.getHexColor());
					Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 10,
							4 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), sr.getScaledWidth(),
							6 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), m.getHexColor());

					fr.drawStringWithShadow(m.getDisplayName(),
							sr.getScaledWidth() - 4 - fr.getStringWidth(m.getDisplayName()),
							3 + count * (fr.FONT_HEIGHT + 6), getHexColorModule(m, count));

				} else if (settingEquals("Style", "Outlined")) {
					// if (count2 == potionNames.size() - 1) {
					this.setDisplayName("HUD §7Outlined");

					Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 8,
							count * (fr.FONT_HEIGHT + 6), sr.getScaledWidth(),
							6 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), 0x90000000);
					Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 10,
							count * (fr.FONT_HEIGHT + 6),
							sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 8,
							6 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), m.getHexColor());
					if (count == sortedModules.size() - 1) {
						Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 10,
								4 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), sr.getScaledWidth(),
								6 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), m.getHexColor());
					} else {
						Gui.drawRect(sr.getScaledWidth() - fr.getStringWidth(m.getDisplayName()) - 10,
								4 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), sr.getScaledWidth()
										- fr.getStringWidth(sortedModules.get(count + 1).getDisplayName()) - 8,
								6 + fr.FONT_HEIGHT + count * (fr.FONT_HEIGHT + 6), m.getHexColor());
					}

					fr.drawStringWithShadow(m.getDisplayName(),
							sr.getScaledWidth() - 4 - fr.getStringWidth(m.getDisplayName()),
							3 + count * (fr.FONT_HEIGHT + 6), getHexColorModule(m, count));

				}

				count++;
			}

			GlStateManager.translate(sr.getScaledWidth(), 0, 0.0);
			GlStateManager.scale(1 / moduleScale, 1 / moduleScale, 1.0);
			GlStateManager.translate(-sr.getScaledWidth(), 0, 0.0);
		}

		if (Client.instance.settingsManager.getSettingByName("PPSP").getValBoolean()) {
//			List<Vector3D> vertices = new ArrayList<Vector3D>();
//			double heading;
//			double radius = 20;
//
//			for (int a = 0; a < 360; a += 360) {
//				heading = Math.toRadians(a);
//				vertices.add(new Vector3d(Math.cos(heading) * radius, Math.sin(heading) * radius, sr.getScaledWidth()/2));
//			}
		}

	}
}
