package com.woytuloo.discordbot.commands;

import com.woytuloo.discordbot.storage.UserStorage;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(LinkCommand.class);

    @Override
    public String getName() {
        return "Link";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        OptionMapping textOption = event.getOption("steam_url");

        if (textOption == null) {
            throw new IllegalArgumentException("Option 'steam_url' is required.");
        }

        long steamID64FromUrl = getSteamID64FromUrl(textOption.getAsString());
        long steamId3 = convertSteamID64ToSteamID3(steamID64FromUrl);

        String discordName = event.getUser().getEffectiveName();
        String userId = event.getUser().getId();
        try {
            UserStorage.addUser(userId, steamId3 + "", discordName);
            logger.info("Added user with discord name: {} discordId: {} and steamId3: {}", discordName, userId, steamId3);
        }catch (Exception e) {
            event.reply("Failed to link your account. " + e.getMessage())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.reply("Successfully linked your account with SteamId3: " + steamId3)
                .setEphemeral(true)
                .queue();
    }

    private static long getSteamID64FromUrl(String steamUrl) throws IllegalArgumentException {
        try {
            if (steamUrl.contains("/profiles/")) {
                String idStr = steamUrl.split("/profiles/")[1].split("/")[0];
                return Long.parseLong(idStr);
            }

            throw new IllegalArgumentException("Invalid Steam URL format - provide url with /profiles in link");
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not extract SteamID from URL", e);
        }
    }

    private static long convertSteamID64ToSteamID3(long steamID64) {
        return steamID64 - 76561197960265728L;
    }

}
