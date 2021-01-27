package org.johnnei.javatorrent.gui.model;

import org.johnnei.javatorrent.torrent.PeerStateAccess;
import org.johnnei.javatorrent.torrent.Torrent;

public class TorrentWithState {

    private final Torrent torrent;

    private final PeerStateAccess peerState;

    public TorrentWithState(Torrent torrent, PeerStateAccess peerState) {
        this.torrent = torrent;
        this.peerState = peerState;
    }

    public Torrent getTorrent() {
        return torrent;
    }

    public PeerStateAccess getPeerState() {
        return peerState;
    }
}
