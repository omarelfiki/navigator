package closureAnalysis;

import models.Stop;
import db.TDSImplement;
import map.ClosureHeatMapPainter;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.DefaultTileFactory;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.util.List;

import static util.DebugUtil.*;

public class ClosureApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            init();
            JFrame frame = new JFrame("Closure Heat Map");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 800);

            JXMapViewer map = new JXMapViewer();
            map.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));
            map.setZoom(4);
            map.setAddressLocation(new GeoPosition(41.9028, 12.4964)); // Rome

            MouseInputListener panListener = new PanMouseInputListener(map);
            map.addMouseListener(panListener);
            map.addMouseMotionListener(panListener);
            map.addMouseListener(new CenterMapListener(map));
            map.addMouseWheelListener(new ZoomMouseWheelListenerCursor(map));
            map.addKeyListener(new PanKeyListener(map));

            frame.add(new JScrollPane(map), BorderLayout.CENTER);
            frame.setVisible(true);

            JDialog loadingDialog = new JDialog(frame, "Loading Heatmap...", true);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Loading heatmap, please wait...", JLabel.CENTER), BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(300, 80));
            loadingDialog.setUndecorated(true);
            loadingDialog.getContentPane().add(panel);
            loadingDialog.pack();
            loadingDialog.setLocationRelativeTo(frame);

            SwingWorker<ClosureHeatMapPainter, Void> worker = new SwingWorker<>() {
                @Override
                protected ClosureHeatMapPainter doInBackground() {
                    List<StopData> stopDataList;
                    try {
                        List<Stop> allStops = new TDSImplement().getAllStops();
                        stopDataList = FinalScore.calculateFinalScore(allStops);
                        sendInfo("Final scores calculated for " + stopDataList.size() + " stops.");
                        System.out.println("Top 5 stops to consider for closure:");
                        for (int i = 0; i < 5; i++) {
                            StopData s = stopDataList.get(i);
                            System.out.printf("Stop ID: %s | Name: %s | Score: %.4f | Fs: %.4f | Ps: %.4f | Es: %.4f | Ds: %.4f%n",
                                    s.getStopId(), s.getStopName(), s.getScore(), s.getFs(), s.getPs(), s.getEs(), s.getDs());

                        }
                    } catch (Exception e) {
                        sendError("Failed to load stop data: " + e.getMessage());
                        stopDataList = java.util.Collections.emptyList();
                    }
                    return new ClosureHeatMapPainter(stopDataList);
                }
                @Override
                protected void done() {
                    try {
                        map.setOverlayPainter(get());
                        sendInfo("Closure Heat Map loaded successfully.");
                    } catch (Exception e) {
                        sendError("Failed to set painter: " + e.getMessage());
                    }
                    loadingDialog.dispose();
                }
            };
            new Thread(() -> loadingDialog.setVisible(true)).start();
            worker.execute();
        });
    }
}