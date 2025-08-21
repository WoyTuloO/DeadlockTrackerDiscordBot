package com.woytuloo.discordbot.entities.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamUserResponse(
        @JsonProperty("account_id") String accountId,
        @JsonProperty("personaname") String personaName,
        @JsonProperty("avatarmedium") String avatarUrl,
        @JsonProperty("profileurl") String profileUrl
) {
}
