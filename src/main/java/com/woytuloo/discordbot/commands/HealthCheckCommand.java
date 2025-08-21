package com.woytuloo.discordbot.commands;

import com.woytuloo.discordbot.deadlockservice.utls.DeadlockUtilsService;
import com.woytuloo.discordbot.entities.deadlockapiresponse.HealthCheckResponse;
import com.woytuloo.discordbot.storage.FirebaseService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class HealthCheckCommand implements Command {

    @Override
    public String getName() {
        return "Health Check";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        long ping = event.getJDA().getGatewayPing();
        DeadlockUtilsService serviceInstance = DeadlockUtilsService.getInstance();

        HealthCheckResponse healthCheckResponse = serviceInstance.runHealthCheck();

        boolean DBup = FirebaseService.isAvailable();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Health Check Results");
        embedBuilder.setColor(new Color(0, 168, 3));
        embedBuilder.addField("Deadlock Service", healthCheckResponse.services().clickhouseStatus() ? "✅" : "❌", true);
        embedBuilder.addField("Deadlock Redis Status", healthCheckResponse.services().redisStatus() ? "✅" : "❌", true);
        embedBuilder.addField("Deadlock Postgres Status", healthCheckResponse.services().postgresStatus() ? "✅" : "❌", true);
        embedBuilder.addField("FirebaseDB status", DBup ? "✅" : "❌", true);
        embedBuilder.addField("Gateway Ping", String.format("%d ms", ping), true);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

}
