package net.pistonmaster.pistonfilter.utils;

import java.util.*;

public record FilteredPlayer(UUID id, Deque<MessageInfo> lastMessages) {
}
