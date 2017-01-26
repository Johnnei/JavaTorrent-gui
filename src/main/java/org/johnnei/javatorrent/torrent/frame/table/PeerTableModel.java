package org.johnnei.javatorrent.torrent.frame.table;

import javax.swing.table.AbstractTableModel;

import java.time.Duration;
import java.time.LocalDateTime;

import org.johnnei.javatorrent.torrent.Torrent;
import org.johnnei.javatorrent.torrent.peer.Peer;
import org.johnnei.javatorrent.torrent.peer.PeerDirection;
import org.johnnei.javatorrent.torrent.frame.TorrentFrame;
import org.johnnei.javatorrent.utils.StringFormatUtils;

public class PeerTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private static final String[] headers = new String[] {
		"IP",
		"Client",
		"Down Speed",
		"Up Speed",
		"Time idle",
		"Pieces",
		"Requests",
		"Flags"
	};

	private static final int COL_IP = 0;
	private static final int COL_CLIENT = 1;
	private static final int COL_DOWN = 2;
	private static final int COL_UP = 3;
	private static final int COL_IDLE = 4;
	private static final int COL_PIECES = 5;
	private static final int COL_REQUESTS = 6;
	private static final int COL_FLAGS = 7;

	private TorrentFrame torrentFrame;

	public PeerTableModel(TorrentFrame torrentFrame) {
		this.torrentFrame = torrentFrame;
	}

	@Override
	public int getRowCount() {
		Torrent torrent = torrentFrame.getSelectedTorrent();

		if (torrent == null) {
			return 0;
		}

		return torrent.getPeers().size();
	}

	@Override
	public int getColumnCount() {
		return headers.length;
	}

	@Override
	public String getColumnName(int column) {
		return headers[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Torrent torrent = torrentFrame.getSelectedTorrent();
		Peer peer = torrent.getPeers().get(rowIndex);
		Duration duration = Duration.between(peer.getLastActivity(), LocalDateTime.now());

		switch(columnIndex) {
			case COL_IP:
				return peer.toString();
			case COL_CLIENT:
				return peer.getClientName();
			case COL_DOWN:
				String download = StringFormatUtils.compactByteSize(peer.getBitTorrentSocket().getDownloadRate());
				return String.format("%s/s", download);
			case COL_UP:
				String upload = StringFormatUtils.compactByteSize(peer.getBitTorrentSocket().getUploadRate());
				return String.format("%s/s", upload);
			case COL_IDLE:
				return StringFormatUtils.timeToString(duration.toMillis() / 1000);
			case COL_PIECES:
				return peer.countHavePieces();
			case COL_REQUESTS:
				return String.format("%d/%d | %d", peer.getWorkQueueSize(PeerDirection.Download), peer.getRequestLimit(), peer.getWorkQueueSize(PeerDirection.Upload));
			case COL_FLAGS:
				return getFlagsFor(peer);
			default:
				throw new IllegalArgumentException(String.format("Column %d is outside of the column range", columnIndex));
		}
	}

	private String getFlagsFor(Peer peer) {
		String socketType = peer.getBitTorrentSocket().getSocketName();
		if (socketType.length() > 0) {
			socketType = socketType.substring(0, 1);
		}
		String flags = socketType;
		if (peer.isInterested(PeerDirection.Upload)) {
			// TODO Why is this supposed to be upload?
			flags += "I";
		}
		if (peer.isChoked(PeerDirection.Download)) {
			flags += "C";
		}
		return flags;
	}

}
