package org.johnnei.javatorrent.torrent.frame.table;

import javax.swing.table.AbstractTableModel;

import org.johnnei.javatorrent.torrent.AbstractFileSet;
import org.johnnei.javatorrent.torrent.FileInfo;
import org.johnnei.javatorrent.torrent.Torrent;
import org.johnnei.javatorrent.torrent.frame.TorrentFrame;
import org.johnnei.javatorrent.utils.StringFormatUtils;

public class FilesTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private static final int COL_NAME = 0;
	private static final int COL_SIZE = 2;
	private static final int COL_PIECES = 1;

	private static final String[] headers = {
		"Filename",
		"Progress",
		"Size"
	};

	private TorrentFrame torrentFrame;

	public FilesTableModel(TorrentFrame torrentFrame) {
		this.torrentFrame = torrentFrame;
	}

	@Override
	public int getRowCount() {
		Torrent torrent = torrentFrame.getSelectedTorrent();

		if (torrent == null || torrent.getFileSet() == null) {
			return 0;
		}

		return torrent.getFileSet().getFiles().size();
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
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == COL_PIECES) {
			return Double.class;
		}
		return super.getColumnClass(columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Torrent torrent = torrentFrame.getSelectedTorrent();
		FileInfo file = torrent.getFileSet().getFiles().get(rowIndex);

		switch (columnIndex) {
			case COL_NAME:
				return file.getFileName();
			case COL_PIECES:
				return getHavePieceCountForFile(torrent, file) * 100d / file.getPieceCount();
			case COL_SIZE:
				return StringFormatUtils.compactByteSize(file.getSize());
			default:
				throw new IllegalArgumentException(String.format("Column %d is outside of the column range", columnIndex));
		}
	}

	private int getHavePieceCountForFile(Torrent torrent, FileInfo fileInfo) {
		AbstractFileSet fileSet = torrent.getFileSet();

		int firstPiece = (int) (fileInfo.getFirstByteOffset() / fileSet.getPieceSize());
		int pieceCount = fileInfo.getPieceCount();

		int doneCount = 0;
		for (int i = 0; i < pieceCount; i++) {
			if (fileSet.hasPiece(firstPiece + i)) {
				doneCount++;
			}
		}

		return doneCount;
	}

}
