package org.johnnei.javatorrent.utils;

import org.johnnei.javatorrent.torrent.Torrent;

/**
 * Created by johnn on 26/03/2016.
 */
public class TorrentUtils {

	public static double getProgress(Torrent torrent) {
		if (torrent.isDownloadingMetadata()) {
			return 0D;
		}
		return (torrent.getFileSet().countCompletedPieces() * 100d) / torrent.getFileSet().getPieceCount();
	}


}
