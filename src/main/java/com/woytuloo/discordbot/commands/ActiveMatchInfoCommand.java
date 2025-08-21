package com.woytuloo.discordbot.commands;

import com.woytuloo.discordbot.deadlockservice.player.DeadlockPlayerService;
import com.woytuloo.discordbot.entities.deadlockapiresponse.CurrentMatchResponse;
import com.woytuloo.discordbot.entities.deadlockapiresponse.Player;
import com.woytuloo.discordbot.storage.UserStorage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.woytuloo.discordbot.storage.HeroStorage.getHeroName;

public class ActiveMatchInfoCommand implements Command {
    @Override
    public String getName() {
        return "Get Active Match Info";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {

        OptionMapping textOption = event.getOption("discord_username");


        String userId3;

        if(textOption != null){
            String playerNickname = textOption.getAsString();
            try {
                userId3 = UserStorage.getDeadlockIdByName(playerNickname);
            }catch (Exception e){
                event.reply("No Deadlock account linked to the provided Discord username: " + playerNickname)
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }else
            userId3 = UserStorage.getDeadlockId(event.getUser().getId());


        if (userId3 == null) {
            event.reply("You need to link your Deadlock account first using `/link` command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        CurrentMatchResponse currentMatch;

        try {
            currentMatch = DeadlockPlayerService.getInstance().getCurrentMatch(userId3);
            if(currentMatch == null) {
                throw new Exception("No active match found.");
            }
        }catch (Exception e) {
            event.reply("Failed to fetch a match. " + e.getMessage()).setEphemeral(true).queue();
            return;
        }

        List<Team> teams = divideTeams(currentMatch);

        int blueNW = Integer.parseInt(currentMatch.netWorthTeam0());
        int redNW = Integer.parseInt(currentMatch.netWorthTeam1());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("⚔️ Current Match - " + currentMatch.matchId())
                .setColor(blueNW > redNW ? Color.BLUE : redNW > blueNW ? Color.RED : Color.GRAY)
                .setFooter("📊 Updated at " + java.time.LocalTime.now().withNano(0));

        List<Button> buttons = new ArrayList<>();

        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            boolean isBlue = i == 0;

            embedBuilder.addField(
                    (isBlue ?
                            "🔵 **TEAM BLUE**   \uD83D\uDCB0 " + NumberFormat.getNumberInstance(Locale.US).format(blueNW) :
                            "🔴 **TEAM RED**   \uD83D\uDCB0 " + NumberFormat.getNumberInstance(Locale.US).format(redNW)),
                    "━━━━━━━━━━━━━━━━━━━━━━━",
                    false
            );

            for (Player player : team.players()) {
                embedBuilder.addField(
                        getHeroName(player.getHeroId()),
                        getPlayerTile(player),
                        true
                );

                buttons.add(Button.link(
                        "https://statlocker.gg/profile/" + player.getAccountId() + "/matches",
                        getHeroName(player.getHeroId()) + " 🔗"
                ));
            }

            if (i == 0) {
                embedBuilder.addBlankField(false);
            }
        }

        List<ActionRow> actionRows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 6) {
            int end = Math.min(i + 5, buttons.size());
            actionRows.add(ActionRow.of(buttons.subList(i, end)));
        }


        event.replyEmbeds(embedBuilder.build()).setComponents(actionRows).queue();
    }

    private String getPlayerTile(Player player) {
        return "```diff\n" +
                getTitle(player) +
                "\nK:  " + player.getKills() +
                "\nD:  " + player.getDeaths() +
                "\nA:  " + player.getAssists() +
                "\n -  --------" +
                "\nKD: " + String.format("%.2f", getKD(player)) + "\n" +
                "🎯  " + String.format("%.1f%%", Double.parseDouble(player.getAccuracy()) * 100) + "\n" +
                "🏆  " + player.getRank() +
                "\n```";
    }

    private String getTitle(Player player) {
        double kills = Double.parseDouble(player.getKills());
        double deaths = Double.parseDouble(player.getDeaths());
        double assists = Double.parseDouble(player.getAssists());

        /// kd > 1.2 - "Uwaga"
        /// ak > 1.5 - "Support?"
        /// ak < 1 && kd < 0.8 - "Bot"

        double kd = kills / (deaths == 0 ? 1 : deaths);
        double ak = assists / (kills == 0 ? 1 : kills);

        return kd > 1.2 ? "- Uwaga" :
               ak > 2 ? "+ Support?" :
               ak < 1 && kd < 0.8 ? "Bot" : "=======";

    }

    private Double getKD(Player player) {
        double kills = Double.parseDouble(player.getKills());
        double deaths = Double.parseDouble(player.getDeaths());

        if (deaths == 0) {
            return kills ;
        }
        return kills / deaths;
    }


    public List<Team> divideTeams(CurrentMatchResponse currentMatch) {
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(currentMatch.netWorthTeam0(), currentMatch.objectivesMaskTeam0(), new ArrayList<>()));
        teams.add(new Team(currentMatch.netWorthTeam1(), currentMatch.objectivesMaskTeam1(), new ArrayList<>()));

        currentMatch.players().forEach(player -> {
            if(player.getTeam().equals("0"))
                teams.get(0).players().add(player);
            else if(player.getTeam().equals("1"))
                teams.get(1).players().add(player);
        });

        return teams;
    }

    public record Team(
            String netWorth,
            String objectivesMask,
            List<Player> players
    ){}
}
