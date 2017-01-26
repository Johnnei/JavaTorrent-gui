package org.johnnei.javatorrent.torrent.frame.table;

import javax.swing.table.AbstractTableModel;

import java.util.List;

import org.johnnei.javatorrent.torrent.Torrent;
import org.johnnei.javatorrent.utils.StringFormatUtils;
import org.johnnei.javatorrent.utils.TorrentUtils;

public class TorrentTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private static final int COL_NAME = 0;
	private static final int COL_PROGRESS = 1;
	private static final int COL_DOWNLOAD_SPEED = 2;
	private static final int COL_UPLOAD_SPEED = 3;
	private static final int COL_SEEDERS = 4;
	private static final int COL_LEECHERS = 5;

	private static String[] headers = new String[] {
		"Name",
		"Progress",
		"Download speed",
		"Upload speed",
		"Seeders",
		"Leechers"
	};

	private List<Torrent> torrents;

	public TorrentTableModel(List<Torrent> torrents) {
		this.torrents = torrents;
	}

	@Override
	public int getRowCount() {
		return torrents.size();
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
		Torrent torrent = torrents.get(rowIndex);

		switch (columnIndex) {
			case COL_NAME:
				return torrent.getDisplayName();
			case COL_PROGRESS:
				return TorrentUtils.getProgress(torrent);
			case COL_DOWNLOAD_SPEED:
				return String.format("%s/s", StringFormatUtils.compactByteSize(torrent.getDownloadRate()));
			case COL_UPLOAD_SPEED:
				return String.format("%s/s", StringFormatUtils.compactByteSize(torrent.getUploadRate()));
			case COL_SEEDERS:
				return torrent.getSeedCount();
			case COL_LEECHERS:
				return torrent.getLeecherCount();
			default:
				throw new IllegalArgumentException(String.format("Column %d is outside of the column range", columnIndex));
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == COL_PROGRESS) {
			return Double.class;
		}

		return super.getColumnClass(columnIndex);
	}

}
