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

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import static net.md_5.bungee.api.ChatColor.BLUE;
import static net.md_5.bungee.api.ChatColor.DARK_GRAY;
import static net.md_5.bungee.api.ChatColor.GRAY;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 3/15/16.
 */
public class GateKeeperCommand extends Command implements Listener
{
    private final String GATEKEEPER_PREFIX = DARK_GRAY + "[" + BLUE + "GateKeeper" + DARK_GRAY + "] " + GRAY;

    private GateKeeper plugin;

    public GateKeeperCommand(GateKeeper plugin)
    {
        super("gatekeeper", "GateKeeper", "gk");

        this.plugin = plugin;
    }

    public void register()
    {
        plugin.getProxy().getPluginManager().registerCommand(plugin, this);
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    public void shutdown()
    {
        plugin.getProxy().getPluginManager().unregisterCommand(this);
        plugin.getProxy().getPluginManager().unregisterListener(this);
        plugin = null;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings)
    {
        if(!commandSender.hasPermission("GateKeeper"))
        {
            return;
        }

        if(strings.length == 0)
        {
            commandSender.sendMessage(TextComponent.fromLegacyText(
                GRAY + " -- " + GATEKEEPER_PREFIX + " --"));
            commandSender.sendMessage(TextComponent.fromLegacyText(
                GRAY + " /gk <add|remove> <name>"));
            commandSender.sendMessage(TextComponent.fromLegacyText(
                GRAY + " /gk <save|reload>"));
            return;
        }

        if(strings.length >= 2 && strings[0].equalsIgnoreCase("add"))
        {
            String name = strings[1].toLowerCase();

            if(plugin.getBypassers().add(name))
            {
                commandSender.sendMessage(TextComponent.fromLegacyText(
                    GATEKEEPER_PREFIX + name + " added."));
            }
            else
            {
                commandSender.sendMessage(TextComponent.fromLegacyText(
                    GATEKEEPER_PREFIX + name + " is already on the list."));
            }
        }
        else if(strings.length >= 2 && strings[0].equalsIgnoreCase("remove"))
        {
            String name = strings[1].toLowerCase();

            if(plugin.getBypassers().remove(name))
            {
                commandSender.sendMessage(TextComponent.fromLegacyText(
                    GATEKEEPER_PREFIX + name + " removed."));
            }
            else
            {
                commandSender.sendMessage(TextComponent.fromLegacyText(
                    GATEKEEPER_PREFIX + name + " is not in the list."));
            }
        }
        else if(strings.length >= 1 && strings[0].equalsIgnoreCase("save"))
        {
            plugin.saveConfiguration();

            commandSender.sendMessage(TextComponent.fromLegacyText(
                GATEKEEPER_PREFIX + "Configuration saved."));
        }
        else if(strings.length >= 1 && strings[0].equalsIgnoreCase("reload"))
        {
            plugin.reloadConfiguration();

            commandSender.sendMessage(TextComponent.fromLegacyText(
                GATEKEEPER_PREFIX + "Configuration reloaded."));
        }
        else
        {
            commandSender.sendMessage(TextComponent.fromLegacyText(
                GRAY + " -- " + GATEKEEPER_PREFIX + " --"));
            commandSender.sendMessage(TextComponent.fromLegacyText(
                GRAY + " /gk <add|remove> <name>"));
            commandSender.sendMessage(TextComponent.fromLegacyText(
                GRAY + " /gk <save|reload>"));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogin(LoginEvent event)
    {
        if(!event.isCancelled())
        {
            String name = event.getConnection().getName().toLowerCase();
            int count = plugin.getProxy().getPlayers().size();

            if(count >= plugin.getPlayerLimit() && !plugin.getBypassers().contains(name))
            {
                event.setCancelReason(plugin.getMessageOnFull());
                event.setCancelled(true);
            }
        }
    }
}
