package org.johnnei.javatorrent.torrent.frame.table;

import javax.swing.table.AbstractTableModel;

import org.johnnei.javatorrent.TorrentClient;
import org.johnnei.javatorrent.bittorrent.tracker.ITracker;
import org.johnnei.javatorrent.torrent.Torrent;
import org.johnnei.javatorrent.torrent.frame.TorrentFrame;

public class TrackerTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private static final int COL_NAME = 0;
	private static final int COL_STATUS = 1;
	private static final int COL_SEEDERS = 2;
	private static final int COL_LEECHERS = 3;
	private static final int COL_COMPLETED = 4;

	private static final String[] headers = new String[] {
		"Name",
		"Status",
		"Seeders",
		"Leechers",
		"Completed"
	};

	private TorrentFrame torrentFrame;
	private TorrentClient torrentClient;

	public TrackerTableModel(TorrentFrame torrentFrame, TorrentClient torrentClient) {
		this.torrentFrame = torrentFrame;
		this.torrentClient = torrentClient;
	}

	@Override
	public int getRowCount() {
		Torrent torrent = torrentFrame.getSelectedTorrent();

		if (torrent == null) {
			return 0;
		}

		return torrentClient.getTrackersFor(torrent).size();
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
		ITracker item = torrentClient.getTrackersFor(torrentFrame.getSelectedTorrent()).get(rowIndex);
		switch(columnIndex) {
			case COL_NAME:
				return item.getName();
			case COL_STATUS:
				return item.getStatus();
			case COL_SEEDERS:
				return item.getInfo(torrentFrame.getSelectedTorrent()).get().getSeeders();
			case COL_LEECHERS:
				return item.getInfo(torrentFrame.getSelectedTorrent()).get().getLeechers();
			case COL_COMPLETED:
				return item.getInfo(torrentFrame.getSelectedTorrent()).get().getDownloadCount();
			default:
				throw new IllegalArgumentException(String.format("Column %d is outside of the column range", columnIndex));
		}
	}

}
