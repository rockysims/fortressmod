package com.newyith.fortressmod.items;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.io.CharStreams;
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
		} catch (Exception e) {
			e.printStackTrace();
			pages.add("Failed to read contents");
		}
		
		this.setPages(pages);
	}

}
