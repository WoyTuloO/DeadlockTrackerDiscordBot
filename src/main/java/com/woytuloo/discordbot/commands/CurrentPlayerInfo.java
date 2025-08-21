package com.woytuloo.discordbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CurrentPlayerInfo implements Command {

    @Override
    public String getName() {
        return "Get Player Stats";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {

//        String userId3 = UserStorage.getUserId(event.getUser().getId());
//
//        if (userId3 == null) {
//            event.reply("You need to link your Deadlock account first using `/link` command.").setEphemeral(true).queue();
//            return;
//        }
//
//        GeneralPlayerStatsResponse generalPlayerStats = DeadlockPlayerService.getInstance().getGeneralPlayerStats(userId3);

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(event.getUser().getName() + "'s stats");

    }


}
