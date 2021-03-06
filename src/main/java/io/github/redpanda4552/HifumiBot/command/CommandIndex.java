/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2020 RedPanda4552 (https://github.com/RedPanda4552)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.redpanda4552.HifumiBot.command;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.commands.AbstractCommand;
import io.github.redpanda4552.HifumiBot.command.commands.CommandAbout;
import io.github.redpanda4552.HifumiBot.command.commands.CommandCPU;
import io.github.redpanda4552.HifumiBot.command.commands.CommandDX9;
import io.github.redpanda4552.HifumiBot.command.commands.CommandDev;
import io.github.redpanda4552.HifumiBot.command.commands.CommandDynCmd;
import io.github.redpanda4552.HifumiBot.command.commands.CommandGPU;
import io.github.redpanda4552.HifumiBot.command.commands.CommandHelp;
import io.github.redpanda4552.HifumiBot.command.commands.CommandPFP;
import io.github.redpanda4552.HifumiBot.command.commands.CommandPnachTest;
import io.github.redpanda4552.HifumiBot.command.commands.CommandReload;
import io.github.redpanda4552.HifumiBot.command.commands.CommandRun;
import io.github.redpanda4552.HifumiBot.command.commands.CommandShutdown;
import io.github.redpanda4552.HifumiBot.command.commands.CommandWarez;
import io.github.redpanda4552.HifumiBot.command.commands.CommandWiki;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CommandIndex {

    private static final int COMMANDS_PER_PAGE = 10;
    
    private HashMap<String, AbstractCommand> commandMap;
    private HashMap<String, ArrayList<MessageEmbed>> helpPages;
    private MessageEmbed helpRoot;
    
    /**
     * Create a new CommandIndex and invoke the {@link CommandIndex#rebuild rebuild()} method.
     */
    public CommandIndex() {
        commandMap = new HashMap<String, AbstractCommand>();
        rebuild();
    }
    
    /**
     * Rebuild this CommandIndex from the Config object in HifumiBot.
     */
    public void rebuild() {
        commandMap.clear();
        CommandAbout about = new CommandAbout();
        commandMap.put(about.getName(), about);
        CommandCPU cpu = new CommandCPU();
        commandMap.put(cpu.getName(), cpu);
        CommandDev dev = new CommandDev();
        commandMap.put(dev.getName(), dev);
        CommandDX9 dx9 = new CommandDX9();
        commandMap.put(dx9.getName(), dx9);
        CommandDynCmd dyncmd = new CommandDynCmd();
        commandMap.put(dyncmd.getName(), dyncmd);
        CommandGPU gpu = new CommandGPU();
        commandMap.put(gpu.getName(), gpu);
        CommandHelp help = new CommandHelp();
        commandMap.put(help.getName(), help);
        CommandPFP pfp = new CommandPFP();
        commandMap.put(pfp.getName(), pfp);
        CommandReload reload = new CommandReload();
        commandMap.put(reload.getName(), reload);
        CommandPnachTest commandPnachTest = new CommandPnachTest();
        commandMap.put(commandPnachTest.getName(), commandPnachTest);
        CommandRun run = new CommandRun();
        commandMap.put(run.getName(), run);
        CommandShutdown shutdown = new CommandShutdown();
        commandMap.put(shutdown.getName(), shutdown);
        CommandWarez warez = new CommandWarez();
        commandMap.put(warez.getName(), warez);
        CommandWiki wiki = new CommandWiki();
        commandMap.put(wiki.getName(), wiki);
        
        for (DynamicCommand dynamicCommand : HifumiBot.getSelf().getConfig().dynamicCommands) {
            commandMap.put(dynamicCommand.getName(), dynamicCommand);
        }
        
        rebuildHelpPages();
    }
    
    public Set<String> getAll() {
        return commandMap.keySet();
    }
    
    public boolean isCommand(String name) {
        return commandMap.get(name) != null;
    }
    
    public boolean isDynamicCommand(String name) {
        AbstractCommand cmd = commandMap.get(name);
        return cmd != null && cmd instanceof DynamicCommand;
    }
    
    public AbstractCommand getCommand(String name) {
        return commandMap.get(name);
    }
    
    public DynamicCommand getDynamicCommand(String name) {
        AbstractCommand cmd = getCommand(name);
        
        if (cmd == null) {
            return null;
        } else if (cmd instanceof DynamicCommand) {
            return (DynamicCommand) cmd;
        } else {
            return null;
        }
    }
    
    public void addCommand(DynamicCommand dyncmd) {
        // Insert it into the ArrayList in Config, then reload the CommandIndex.
        ArrayList<DynamicCommand> configDynamicCommands = HifumiBot.getSelf().getConfig().dynamicCommands;
        Iterator<DynamicCommand> iter = configDynamicCommands.iterator();
        DynamicCommand configDynamicCommand = null;
        boolean commandExists = false;
        
        while (iter.hasNext()) {
            configDynamicCommand = iter.next();

            if (configDynamicCommand.getName().equals(dyncmd.getName())) {
                configDynamicCommand = dyncmd;
                commandExists = true;
            }
        }
        
        // If no command exists in the iterator, just add it
        if (!commandExists) {
            configDynamicCommands.add(dyncmd);
        }
        
        ConfigManager.write(HifumiBot.getSelf().getConfig());
        HifumiBot.getSelf().getCommandIndex().rebuild();
    }
    
    public void deleteCommand(String name) {
        ArrayList<DynamicCommand> dynamicCommands = HifumiBot.getSelf().getConfig().dynamicCommands;
        Iterator<DynamicCommand> iter = dynamicCommands.iterator();
        DynamicCommand toDelete = null;
        
        while (iter.hasNext()) {
            DynamicCommand dyncmd = iter.next();

            if (dyncmd.getName().equals(name)) {
                toDelete = dyncmd;
                break;
            }
        }
        
        if (toDelete != null) {
            dynamicCommands.remove(toDelete);
            ConfigManager.write(HifumiBot.getSelf().getConfig());
            HifumiBot.getSelf().getCommandIndex().rebuild();
        }
    }
    
    /**
     * Get a HashMap<String, TreeSet<String>> organizing commands by their
     * categories. Currently only used to simplify help page generation. 
     */
    public HashMap<String, TreeSet<String>> getCategorizedCommandNames() {
        Set<String> commandNames = new HashSet<String>();
        commandNames.addAll(getAll());
        HashMap<String, TreeSet<String>> ret = new HashMap<String, TreeSet<String>>();
        
        for (String commandName : commandNames) {
            AbstractCommand command = commandMap.get(commandName);
            TreeSet<String> categoryCommands = null;
            
            if (ret.containsKey(command.getCategory())) {
                categoryCommands = ret.get(command.getCategory());
            } else {
                categoryCommands = new TreeSet<String>(Collator.getInstance());
            }
            
            categoryCommands.add(commandName);
            ret.put(command.getCategory(), categoryCommands);
        }
        
        return ret;
    }

    /**
     * Fully rebuilds the help page lists.
     */
    private void rebuildHelpPages() {
        helpPages = new HashMap<String, ArrayList<MessageEmbed>>();
        HashMap<String, TreeSet<String>> commandMap = this.getCategorizedCommandNames();
        
        for (String category : commandMap.keySet()) {
            int pageCount = (int) Math.ceil((double) commandMap.get(category).size() / COMMANDS_PER_PAGE);
            helpPages.put(category, new ArrayList<MessageEmbed>());
            EmbedBuilder eb = new EmbedBuilder();
            
            for (String command : commandMap.get(category)) {
                eb.addField(">" + command, this.getCommand(command).getHelpText(), false);
                
                if (eb.getFields().size() >= COMMANDS_PER_PAGE) {
                    addToPages(category, eb, pageCount);
                    eb = new EmbedBuilder();
                }
            }
            
            if (eb.getFields().size() > 0)
                addToPages(category, eb, pageCount);
        }
        
        EmbedBuilder helpRootBuilder = new EmbedBuilder();
        helpRootBuilder.setTitle("HifumiBot Help");
        helpRootBuilder.setDescription("The prefix for all commands is \"" + CommandInterpreter.PREFIX + "\".\nTo view available commands use `" + CommandInterpreter.PREFIX + "help <category> [page]`");
        StringBuilder sb = new StringBuilder();
        
        for (String category : commandMap.keySet())
            sb.append(category).append("\n");
        
        helpRootBuilder.addField("Available Categories", sb.toString(), false);
        helpRoot = helpRootBuilder.build();
    }
    
    private void addToPages(String category, EmbedBuilder eb, int pageCount) {
        eb.setTitle("HifumiBot Help - " + category + " - Page " + (helpPages.get(category).size() + 1) + " / " + pageCount);
        eb.setDescription("Use `" + CommandInterpreter.PREFIX + "help " + category + " [page]` to browse other pages.");
        helpPages.get(category).add(eb.build());
    }
    
    public HashMap<String, ArrayList<MessageEmbed>> getHelpPages() {
        return helpPages;
    }
    
    public MessageEmbed getHelpRootPage() {
        return helpRoot;
    }
}
