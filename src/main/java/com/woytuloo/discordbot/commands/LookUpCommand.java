package com.woytuloo.discordbot.commands;

import com.woytuloo.discordbot.deadlockservice.player.DeadlockPlayerService;
import com.woytuloo.discordbot.entities.deadlockapiresponse.PlayerSummaryResponse;
import com.woytuloo.discordbot.entities.external.SteamUserResponse;
import com.woytuloo.discordbot.storage.HeroStorage;
import com.woytuloo.discordbot.storage.UserStorage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;


import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class LookUpCommand implements Command {

    @Override
    public String getName() {
        return "lookup";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        OptionMapping usernameOption = event.getOption("deadlock_nickname");
        OptionMapping heroOption = event.getOption("deadlock_hero");

        String heroId;
        if (heroOption != null) {
            try {
                heroId = HeroStorage.getHeroId(heroOption.getAsString());
            }catch (Exception e) {
                event.reply("Invalid hero name provided. Please check the hero name and try again.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        } else
            heroId = "-1";


        String userId3;
        String username;

        if (usernameOption != null) {
            List<SteamUserResponse> users = UserStorage.getDeadlockIdByName(
                    usernameOption.getAsString()
            );

            if (users.size() == 1) {

                userId3 = users.getFirst().accountId();
                username = users.getFirst().personaName();

            } else {
                if(users.isEmpty())
                    event.reply("No user found with the provided nickname.")
                            .setEphemeral(true)
                            .queue();

                showUserSelectionButtons(event, users, heroId);
                return;
            }
        } else {
            userId3 = UserStorage.getDeadlockId(event.getUser().getId());
            username = event.getUser().getEffectiveName();
        }

        handleEvents(event, userId3, username, heroId);
    }

    private void showUserSelectionButtons(SlashCommandInteractionEvent event, List<SteamUserResponse> users, String heroId) {

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Multiple users found");
        embed.setDescription("Please select the correct user:");
        embed.setColor(Color.ORANGE);

        event.replyEmbeds(embed.build()).queue(interactionHook -> {
            for (SteamUserResponse user : users) {
                EmbedBuilder userEmbed = new EmbedBuilder()
                        .setTitle(user.personaName())
                        .setImage(user.avatarUrl())
                        .setColor(Color.BLUE);

                Button button = Button.primary(
                        "lookup:" + user.personaName() + ":"+ user.accountId() + ":" + heroId,
                        user.personaName()
                );

                interactionHook.sendMessageEmbeds(userEmbed.build())
                        .setComponents(ActionRow.of(button))
                        .queue();
            }
        });
    }

    public static EmbedBuilder processSingleUser(String userId3, String username, String heroId) {
        try {
            PlayerSummaryResponse playerSummary = DeadlockPlayerService.getInstance().getPlayerSummary25(userId3, heroId);

            if (playerSummary == null) {
                return null;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Player Summary for " + username);
            embed.addField("", "Data is an average of last 25 matches", true);
            embed.setDescription(getPlayerTile(playerSummary));
            embed.setColor(0x00FF00);

            return embed;
        }catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }


    public static void handleEvents(SlashCommandInteractionEvent event, String userId3, String username, String heroId){
        EmbedBuilder embedBuilder = processSingleUser(userId3, username, heroId);
        if (embedBuilder == null) {
            event.reply("No player found with the provided username or ID.")
                    .setEphemeral(true)
                    .queue();
            return ;
        }
        event.replyEmbeds(embedBuilder.build()).queue();
    }

    public static void handleEvents(ButtonInteractionEvent event, String username, String userId3, String heroId){
        EmbedBuilder embedBuilder = processSingleUser(userId3, username, heroId);
        if (embedBuilder == null) {
            event.reply("No player found with the provided username or ID.")
                    .setEphemeral(true)
                    .queue();
            return ;
        }
        event.replyEmbeds(embedBuilder.build()).queue();
    }

    private static String getPlayerTile(PlayerSummaryResponse player) {
        return "```diff\n" +
                getTitle(player) +
                "\nKD        :  " + String.format("%.2f", player.avgKD()) +
                "\n ---------------" +
                "\nKills     :  " + String.format("%.2f", player.avgKills()) +
                "\nDeaths    :  " + String.format("%.2f", player.avgDeaths()) +
                "\nAssists   :  " + String.format("%.2f", player.avgAssists()) +
                "\nDenies    :  " + String.format("%.2f", player.avgDenies()) +
                "\nNet Worth :  " + String.format("%.2f", player.avgNetWorth()) +
                "\nWin %     :  " + String.format("%.1f%%", player.winPercentage()) +

                "\nAccuracy  :  " + String.format("%.1f%%", player.accuracy() * 100) + "\n" +
//                "🏆  " + player.getRank() +
                "\n```";
    }

    private static String getTitle(PlayerSummaryResponse player) {
        double kills = player.avgKills();
        double deaths = player.avgDeaths();
        double assists = player.avgAssists();

        /// kd > 1.2 - "Uwaga"
        /// ak > 1.5 - "Support?"
        /// ak < 1 && kd < 0.8 - "Bot"

        double kd = kills / (deaths == 0 ? 1 : deaths);
        double ak = assists / (kills == 0 ? 1 : kills);

        return kd > 1.2 ? "- Uwaga" :
                ak > 2 ? "+ Support?" :
                        ak < 1 && kd < 0.8 ? "Bot" : "=======";

    }

}
