package org.johnnei.javatorrent.torrent.frame.table;

import javax.swing.table.AbstractTableModel;

import java.util.List;
import java.util.stream.Collectors;

import org.johnnei.javatorrent.torrent.Torrent;
import org.johnnei.javatorrent.torrent.files.BlockStatus;
import org.johnnei.javatorrent.torrent.files.Piece;
import org.johnnei.javatorrent.torrent.frame.TorrentFrame;
import org.johnnei.javatorrent.utils.StringFormatUtils;

public class PiecesTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private static final int COL_PIECE_NUM = 0;
	private static final int COL_SIZE = 1;
	private static final int COL_BLOCKS = 2;
	private static final int COL_PROGRESS =3;

	private static final String[] headers = new String[] {
		"Piece #",
		"Size",
		"Blocks (Need | Req | Saved)",
		"Progress"
	};

	private TorrentFrame torrentFrame;

	private List<Piece> pieces;

	public PiecesTableModel(TorrentFrame torrentFrame) {
		this.torrentFrame = torrentFrame;
	}

	@Override
	public int getRowCount() {
		Torrent torrent = torrentFrame.getSelectedTorrent();

		if (torrent == null || torrent.getFileSet() == null) {
			return 0;
		}

		// Cache this information to prevent race conditions
		pieces = torrent.getFileSet().getNeededPieces().filter(p -> p.isStarted()).collect(Collectors.toList());
		return pieces.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == COL_PROGRESS) {
			return Piece.class;
		}

		return super.getColumnClass(columnIndex);
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
		Piece piece = pieces.get(rowIndex);

		switch (columnIndex) {
		case COL_PIECE_NUM:
			return piece.getIndex();
		case COL_BLOCKS:
			return String.format(
					"%d | %d | %d",
					piece.countBlocksWithStatus(BlockStatus.Needed),
					piece.countBlocksWithStatus(BlockStatus.Requested),
					piece.countBlocksWithStatus(BlockStatus.Stored));
		case COL_PROGRESS:
			return piece;
		case COL_SIZE:
			return StringFormatUtils.compactByteSize(piece.getSize());
		default:
			throw new IllegalArgumentException(String.format("Column %d is outside of the column range", columnIndex));
		}
	}

}
