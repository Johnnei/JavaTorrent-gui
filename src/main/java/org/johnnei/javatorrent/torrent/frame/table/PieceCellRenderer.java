package org.johnnei.javatorrent.torrent.frame.table;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import org.johnnei.javatorrent.torrent.files.BlockStatus;
import org.johnnei.javatorrent.torrent.files.Piece;

public class PieceCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private Piece piece;
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		piece = (Piece) value;
		
		return this;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		float pixelsPerBlock = (float) getWidth() / piece.getBlockCount();
		
		boolean isDone = piece.getBlockStatus(0) == BlockStatus.Verified;
		boolean isRequested = isRequested(piece, 0);
		boolean render = false;
		int startIndex = 0;
		
		for (int i = 0; i < piece.getBlockCount(); i++) {
			render = piece.getBlockStatus(i) == BlockStatus.Verified || isRequested(piece, i) != isRequested;
			
			if (render || i + 1 >= piece.getBlockCount()) {
				if (isDone) {
					g.setColor(Color.GREEN);
				} else if (isRequested) {
					g.setColor(Color.ORANGE);
				} else {
					g.setColor(Color.RED);
				}
				
				int blockCount = 1 + i - startIndex;
				g.fillRect((int) (startIndex * pixelsPerBlock), 0, (int) (blockCount * pixelsPerBlock), getHeight());
				
				startIndex = i;
				isDone = piece.getBlockStatus(i) == BlockStatus.Verified;
				isRequested = isRequested(piece, i);
				render = false;
			}
		}
	}

	private boolean isRequested(Piece piece, int blockIndex) {
		return piece.getBlockStatus(blockIndex) == BlockStatus.Requested || piece.getBlockStatus(blockIndex) == BlockStatus.Stored;
	}

}
