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
		
		for (int blockIndex = 0; blockIndex < piece.getBlockCount(); blockIndex++) {
			BlockStatus blockStatus = piece.getBlockStatus(blockIndex);

			switch (blockStatus) {
				case Needed:
					g.setColor(Color.GRAY);
					break;
				case Requested:
					g.setColor(Color.ORANGE);
					break;
				case Stored:
					g.setColor(Color.BLUE.brighter());
					break;
				case Verified:
					g.setColor(Color.GREEN);
					break;
			}

			g.fillRect((int) (blockIndex * pixelsPerBlock), 0, (int) ((blockIndex + 1) * pixelsPerBlock), getHeight());
		}
	}

}
