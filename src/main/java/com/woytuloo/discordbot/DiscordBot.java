package com.woytuloo.discordbot;

import com.woytuloo.discordbot.config.BotConfig;
import com.woytuloo.discordbot.listeners.CommandListener;
import com.woytuloo.discordbot.storage.FirebaseService;
import com.woytuloo.discordbot.storage.HeroStorage;
import com.woytuloo.discordbot.storage.RankStorage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class DiscordBot {
    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);
    private static JDA jda;

    public static void main(String[] args) {
        String botToken = BotConfig.getBotToken();

        if (botToken == null || botToken.isEmpty()) {
            logger.error("Bot token not found in config.properties. Please provide a valid token.");
            return;
        }

        try {
            FirebaseService.initialize();

            jda = JDABuilder.createDefault(botToken)
                    .enableIntents(EnumSet.allOf(GatewayIntent.class))
                    .addEventListeners(new CommandListener())
                    .build();

            jda.awaitReady();
            logger.info("Bot is online and ready!");

            registerSlashCommands();

            logger.info("Hero storage loaded successfully!");
            HeroStorage.getInstance().loadHeroes();
            logger.info("Rank storage loaded successfully!");
            RankStorage.getInstance().loadRanks();
        } catch (Exception e) {
            logger.error("Error starting the bot: ", e);
        }
    }

    private static void registerSlashCommands() {
        if (jda == null) {
            logger.error("JDA instance is not initialized. Cannot register slash commands.");
            return;
        }

        logger.info("Registering Slash Commands...");
        jda.updateCommands()
                .addCommands(
                        Commands.slash("ping", "Checks the bot's latency to Discord's gateway and availability of deadlock services."),
                        Commands.slash("live", "Get's live match data.")
                                .addOption(OptionType.STRING, "discord_username", "Discord username of a player you want to look up.", false),
                        Commands.slash("connect", "Links bot with your Deadlock account. - provide your steam account link")
                                .addOption(OptionType.STRING, "steam_url", "SteamUrl link : http://steamcommunity.com/profiles/7222222222505563 ", true))
                .queue(success -> logger.info("Slash commands registered successfully!"),
                        failure -> logger.error("Failed to register slash commands: ", failure));
    }

}
