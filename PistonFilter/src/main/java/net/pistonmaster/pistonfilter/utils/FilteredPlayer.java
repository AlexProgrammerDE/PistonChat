package net.pistonmaster.pistonfilter.utils;

import java.util.Deque;
import java.util.UUID;

public record FilteredPlayer(UUID id, Deque<MessageInfo> lastMessages) {
}
