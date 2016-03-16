/*
 * This file is part of GateKeeper.
 *
 * GateKeeper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GateKeeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GateKeeper.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.GateKeeper;

import io.netty.util.internal.ConcurrentSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 3/15/16.
 */
public class GateKeeper extends Plugin
{
    private GateKeeperCommand gateKeeperCommand;
    private Configuration configuration;
    private int playerLimit;
    private int configurationSaveInterval;
    private String messageOnFull;
    private final Set<String> bypassers = new ConcurrentSet<>();

    @Override
    public void onEnable()
    {
        reloadConfiguration();
        gateKeeperCommand = new GateKeeperCommand(this);
        gateKeeperCommand.register();
    }

    @Override
    public void onDisable()
    {
        saveConfiguration();
        gateKeeperCommand.shutdown();
        gateKeeperCommand = null;
    }

    public int getPlayerLimit()
    {
        return playerLimit;
    }

    public String getMessageOnFull()
    {
        return messageOnFull;
    }

    public Set<String> getBypassers()
    {
        return bypassers;
    }

    public synchronized void saveConfiguration()
    {
        File file = new File(getDataFolder(), "config.yml");

        configuration.set("PlayerLimit", playerLimit);
        configuration.set("MessageOnFull", messageOnFull);
        configuration.set("ConfigurationSaveInterval", configurationSaveInterval);
        configuration.set("Bypassers", convertBypassersToList());

        try
        {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public synchronized void reloadConfiguration()
    {
        File file = ConfigUtil.saveResource(this, "config.yml", "config.yml");

        try
        {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            if(configuration != null)
            {
                getProxy().getScheduler().cancel(this);

                playerLimit = configuration.getInt("PlayerLimit", 100);
                configurationSaveInterval = configuration.getInt("ConfigurationSaveInterval", 30);
                messageOnFull = ChatColor.translateAlternateColorCodes('&',
                    configuration.getString("MessageOnFull", "<missing in config>"));

                getProxy().getScheduler().schedule(this, this::saveConfiguration, 30, 30, TimeUnit.MINUTES);

                bypassers.clear();

                for(String name : configuration.getStringList("Bypassers"))
                {
                    bypassers.add(name.toLowerCase());
                }
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private List<String> convertBypassersToList()
    {
        List<String> bypasserList = new ArrayList<>(bypassers.size());

        synchronized(bypassers)
        {
            for(String bypasser : bypassers)
            {
                bypasserList.add(bypasser);
            }
        }

        Collections.sort(bypasserList);
        return bypasserList;
    }
}
