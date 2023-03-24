package moe.evoke.application.components;

import moe.evoke.application.components.peertube.PlayerStatus;

public interface PlayerStatusListener {

    void execute(PlayerStatus lastPlayerStatus, PlayerStatus playerStatus);

}
