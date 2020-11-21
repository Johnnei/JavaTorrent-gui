package org.johnnei.javatorrent.torrent;

import java.io.File;
import java.util.concurrent.Executors;

import org.johnnei.javatorrent.network.socket.NioTcpSocket;
import org.johnnei.javatorrent.tracker.NioPeerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.johnnei.javatorrent.TorrentClient;
import org.johnnei.javatorrent.magnetlink.MagnetLink;
import org.johnnei.javatorrent.module.UTMetadataExtension;
import org.johnnei.javatorrent.network.ConnectionDegradation;
import org.johnnei.javatorrent.phases.PhaseData;
import org.johnnei.javatorrent.phases.PhaseMetadata;
import org.johnnei.javatorrent.phases.PhasePreMetadata;
import org.johnnei.javatorrent.phases.PhaseRegulator;
import org.johnnei.javatorrent.phases.PhaseSeed;
import org.johnnei.javatorrent.protocol.extension.ExtensionModule;
import org.johnnei.javatorrent.torrent.algos.requests.RateBasedLimiter;
import org.johnnei.javatorrent.torrent.frame.TorrentFrame;
import org.johnnei.javatorrent.tracker.UdpTrackerModule;
import org.johnnei.javatorrent.tracker.UncappedDistributor;
import org.johnnei.javatorrent.utils.config.Config;

public class JavaTorrent {

	private static final Logger LOGGER = LoggerFactory.getLogger(JavaTorrent.class);

	public static void main(String[] args) {
		final int maxConccurentConnectingPeers = Config.getConfig().getInt("peer-max_concurrent_connecting");
		final File downloadFolder = new File(Config.getConfig().getString("download-output_folder"));

		try {
			TorrentClient torrentClient = new TorrentClient.Builder()
				.setConnectionDegradation(new ConnectionDegradation.Builder()
					.registerDefaultConnectionType(NioTcpSocket.class, NioTcpSocket::new)
					.build())
				.registerModule(new ExtensionModule.Builder()
					.registerExtension(new UTMetadataExtension(new File(Config.getConfig().getTempFolder()), downloadFolder))
					.build())
				.registerModule(new UdpTrackerModule.Builder()
					.setPort(Config.getConfig().getInt("download-port"))
					.build())
				.setPhaseRegulator(new PhaseRegulator.Builder()
					.registerInitialPhase(PhasePreMetadata.class, PhasePreMetadata::new, PhaseMetadata.class)
					.registerPhase(PhaseMetadata.class, PhaseMetadata::new, PhaseData.class)
					.registerPhase(PhaseData.class, PhaseData::new, PhaseSeed.class)
					.registerPhase(PhaseSeed.class, PhaseSeed::new)
					.build())
				.setRequestLimiter(new RateBasedLimiter())
				.setPeerDistributor(UncappedDistributor::new)
				.setPeerConnector(client -> new NioPeerConnector(client, maxConccurentConnectingPeers))
				.setExecutorService(Executors.newScheduledThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1)))
				.setDownloadPort(Config.getConfig().getInt("download-port"))
				.build();

			TorrentFrame frame = new TorrentFrame(torrentClient);
			boolean showGui = true;
			for (String arg : args) {
				if (arg.startsWith("magnet")) {
					MagnetLink magnet = new MagnetLink(arg, torrentClient);
					if (magnet.isDownloadable()) {
						Torrent torrent = magnet.getTorrent();
						torrentClient.download(torrent, magnet.getTrackerUrls());
					} else {
						LOGGER.warn("Magnet link error occured");
					}
					frame.addTorrent(magnet.getTorrent());
				} else if (arg.startsWith("-no-gui")) {
					showGui = false;
				}
			}
			if (showGui) {
				frame.setVisible(true);
			} else {
				frame.dispose();
			}
		} catch (Exception e) {
			LOGGER.error("Failed to instantiate torrent client.", e);
		}
	}

}
