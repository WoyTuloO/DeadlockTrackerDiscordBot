package com.woytuloo.discordbot.listeners;

import com.woytuloo.discordbot.commands.*;
import com.woytuloo.discordbot.deadlockservice.utls.DeadlockUtilsService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CommandListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CommandListener.class);
    private final Map<String, Command> commands = new HashMap<>();


    DeadlockUtilsService deadlockUtilsService = DeadlockUtilsService.getInstance();

    public CommandListener() {
        commands.put("ping", new HealthCheckCommand());
        commands.put("live", new ActiveMatchInfoCommand());
        commands.put("connect", new LinkCommand());
        logger.info("Registered {} commands.", commands.size());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("JDA is ready! Logged in as {}#{}",
                event.getJDA().getSelfUser().getName(),
                event.getJDA().getSelfUser().getDiscriminator());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Command command = commands.get(commandName);

        if (command != null) {
            logger.debug("Executing slash command: {} from user: {}", commandName, event.getUser().getName());
            try{
                command.executeSlash(event);
            }catch (Exception e) {
                logger.error("Error executing command: {} from user: {}", commandName, event.getUser().getName(), e);
                event.reply("An error occurred while processing your command.\n"+e.getMessage())
                        .setEphemeral(true)
                        .queue();
            }
        } else {
            logger.warn("Unknown slash command: {} from user: {}", commandName, event.getUser().getName());
            event.reply("Unknown command!").setEphemeral(true).queue();
        }
    }
}
