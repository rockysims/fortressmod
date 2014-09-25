package com.newyith.fortressmod.items;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.io.CharStreams;
import com.newyith.fortressmod.FortressMod;
import com.newyith.fortressmod.ModInfo;


public class ItemFortressManual extends ItemWrittenBook {

	public ItemFortressManual() {
		super();
		setUnlocalizedName("FortressManual");
		setTextureName(ModInfo.MODID.toLowerCase() + ":" + "fortress_manual");
		
		List<String> pages = new ArrayList<String>();
		
		try {
			pages.clear();
			pages.addAll(Arrays.asList(
				CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("ItemFortressManualContents.txt"), "UTF-8"))
					.split("===+\n"))
			);
			
			//replace GLOWSTONE_BURN_TIME_HOURS and GLOWSTONE_BURN_TIME_HOURS_S
			float burnHours = (float)FortressMod.config_glowstoneBurnTimeMs / (1000*60*60);
			String burnTimeHours = String.format("%.3f", burnHours);
			burnTimeHours = burnTimeHours.replaceAll("\\.?0+$", "");
			for (int i = 0; i < pages.size(); i++) {
				pages.set(i, pages.get(i).replaceAll("GLOWSTONE_BURN_TIME_HOURS_S", (burnHours != 1)?"s":""));
				pages.set(i, pages.get(i).replaceAll("GLOWSTONE_BURN_TIME_HOURS", burnTimeHours));
			}
		} catch (Exception e) {
			e.printStackTrace();
			pages.add("Failed to read contents");
		}
		
		this.setPages(pages);
	}

}
