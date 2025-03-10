package net.pistonmaster.pistonfilter.utils;

import lombok.*;
import net.md_5.bungee.api.ChatColor;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageInfo {
    private final Instant time;
    private final String originalMessage;
    private final String strippedMessage;
    private final String[] words;
    private final String[] strippedWords;
    private final boolean containsDigit;

    public static MessageInfo of(Instant time, String message) {
        String[] words = Arrays.stream(message.split("\\s+")).toArray(String[]::new);
        String[] strippedWords = Arrays.stream(words)
                .map(MessageInfo::removeColorCodes)
                .map(StringHelper::revertLeet).toArray(String[]::new);

        return new MessageInfo(
                time,
                message,
                String.join("", strippedWords),
                words,
                strippedWords,
                message.matches(".*\\d.*")
        );
    }

    private static String removeColorCodes(String string) {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string));
    }
}
