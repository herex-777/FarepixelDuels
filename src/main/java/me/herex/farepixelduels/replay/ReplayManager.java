package me.herex.farepixelduels.replay;

import me.herex.farepixelduels.duels.Arena;
import me.herex.farepixelduels.duels.GameMode;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReplayManager {

    String startRecording(Arena arena, GameMode mode, List<UUID> players);

    void stopRecording(String replayId);

    <ReplayInfo> Map<String, ReplayInfo> getReplaysFor(Player p);

    void playReplay(Player viewer, String replayId);
}
