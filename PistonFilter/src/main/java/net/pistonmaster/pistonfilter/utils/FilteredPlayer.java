package net.pistonmaster.pistonfilter.utils;

import java.util.Deque;
import java.util.UUID;

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Record components are intentionally mutable for performance")
public record FilteredPlayer(UUID id, Deque<MessageInfo> lastMessages) {
}
