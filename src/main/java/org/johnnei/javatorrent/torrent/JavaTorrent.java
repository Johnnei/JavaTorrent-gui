package org.johnnei.javatorrent.torrent;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.johnnei.javatorrent.TorrentClient;
import org.johnnei.javatorrent.bittorrent.phases.PhaseRegulator;
import org.johnnei.javatorrent.download.algos.PhaseMetadata;
import org.johnnei.javatorrent.download.algos.PhasePreMetadata;
import org.johnnei.javatorrent.magnetlink.MagnetLink;
import org.johnnei.javatorrent.network.protocol.ConnectionDegradation;
import org.johnnei.javatorrent.network.protocol.TcpSocket;
import org.johnnei.javatorrent.protocol.extension.ExtensionModule;
import org.johnnei.javatorrent.protocol.messages.ut_metadata.UTMetadataExtension;
import org.johnnei.javatorrent.torrent.download.Torrent;
import org.johnnei.javatorrent.torrent.download.algos.BurstPeerManager;
import org.johnnei.javatorrent.torrent.download.algos.PhaseData;
import org.johnnei.javatorrent.torrent.download.algos.PhaseSeed;
import org.johnnei.javatorrent.torrent.frame.TorrentFrame;
import org.johnnei.javatorrent.torrent.tracker.PeerConnectorPool;
import org.johnnei.javatorrent.utils.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTorrent extends Thread {

	private static Logger LOGGER = LoggerFactory.getLogger(JavaTorrent.class);

	public static void main(String[] args) {
		final int maxConccurentConnectingPeers = Config.getConfig().getInt("peer-max_concurrent_connecting");
		final int maxConnectingPeers = Config.getConfig().getInt("peer-max_connecting");
		final File downloadFolder = new File(Config.getConfig().getString("download-output_folder"));

		try {
			TorrentClient torrentClient = new TorrentClient.Builder()
					.setConnectionDegradation(new ConnectionDegradation.Builder()
							.registerDefaultConnectionType(TcpSocket.class, TcpSocket::new, Optional.empty())
							.build())
					.registerModule(new ExtensionModule.Builder()
							.registerExtension(new UTMetadataExtension())
							.build())
					.setPhaseRegulator(new PhaseRegulator.Builder()
							.registerInitialPhase(
									PhasePreMetadata.class,
									(client, torrent) -> new PhasePreMetadata(client, torrent, Config.getConfig().getTorrentFileFor(torrent)),
									Optional.of(PhaseMetadata.class))
							.registerPhase(
									PhaseMetadata.class,
									(client, torrent) -> new PhaseMetadata(client, torrent, Config.getConfig().getTorrentFileFor(torrent), downloadFolder),
									Optional.of(PhaseData.class))
							.registerPhase(PhaseData.class, PhaseData::new, Optional.of(PhaseSeed.class))
							.registerPhase(PhaseSeed.class, PhaseSeed::new, Optional.empty())
							.build())
					.setPeerConnector((client) -> new PeerConnectorPool(client, maxConccurentConnectingPeers, maxConnectingPeers))
					.setExecutorService(Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1)))
					.setPeerManager(new BurstPeerManager(Config.getConfig().getInt("peer-max"), Config.getConfig().getFloat("peer-max_burst_ratio")))
					.setDownloadPort(Config.getConfig().getInt("download-port"))
					.build();

			TorrentFrame frame = new TorrentFrame(torrentClient);
			boolean showGui = true;
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.startsWith("magnet")) {
					MagnetLink magnet = new MagnetLink(arg, torrentClient);
					if (magnet.isDownloadable()) {
						Torrent torrent = magnet.getTorrent();
						torrent.start();
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
