package com.clubpetey.teleportals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraftforge.common.config.Configuration;

public class ConfigManager {

    public static Configuration config;

    public static String dataFile = Teleportals.MODID + ".dims";
    private static String configPath;


    public ConfigManager (String path, File file) {
        config = new Configuration(file);
        configPath = path;
        syncConfigData();
    }

	public static void syncConfigData () {
		dataFile = configPath + "/" + config.getString("dataFile", Configuration.CATEGORY_GENERAL, Teleportals.MODID + ".dims", "File with dimension setup");
		
        if (config.hasChanged()) {
            config.save();
        }
    }
	
	public static void reload() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(dataFile));
			while (in.ready()) {
				String line = in.readLine().trim().toLowerCase();
				if (line.startsWith("dim ")) {
					PortalDef pd = null;
					boolean allOK = true;
					
					if (line.charAt(4) == '*') pd = new PortalDef();
					else {
						int dim = Utils.parseInt(line.substring(4), Integer.MAX_VALUE);
						allOK = (dim < Integer.MAX_VALUE);
						pd = new PortalDef(dim);
					}
					line = in.readLine().trim();
					while (line.length() > 0) {
						String[] args = Utils.getCmdArray(line);
						List<String> data = new ArrayList<String>(Arrays.asList(args));
						
						String cmd = data.remove(0);
						if ("[".equals(data.get(data.size()-1))) {
							data.remove(data.size() - 1);
							line = in.readLine().trim();
							while(!"]".equals(line)) {
								data.add(line);
								line = in.readLine().trim();
							}
						}	
						args = new String[data.size()];
						args = data.toArray(args);
						allOK = allOK && pd.parse(cmd, args);
						line = in.readLine().trim();
					}
					if (!allOK) {
						Teleportals.logger.warn("Skipping portal definitions due to errors");
					} else {
						Teleportals.logger.info("Adding: " + pd.toString());
						//add to correct list
						switch (pd.trigger) {
						case PortalDef.POS:
							Teleportals.POS_MAP.put(pd.triggerPos.toString(), pd);
						break;
						case PortalDef.R_CLICK:
							Teleportals.CLICK_LIST.add(pd);
						break;
						case PortalDef.BLOCK:
							Teleportals.BLOCK_LIST.add(pd);
						break;
						}
					}
				} else {
					while (line.length() > 0) {
						line = in.readLine().trim();
					}					
				}
			}
		} catch (IOException e) {
			Teleportals.logger.fatal(e);
		}	finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) { } //munch
		}		
		
	}
}