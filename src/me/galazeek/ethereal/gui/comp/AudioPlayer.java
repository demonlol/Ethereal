package me.galazeek.ethereal.gui.comp;

import me.galazeek.ethereal.Main;
import me.galazeek.ethereal.utils.FormattingUtils;
import me.galazeek.ethereal.utils.SwingUtils2;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer extends JPanel {

    private SwingWorker<Void, Void> audioWorker;

    private File audioFile;

    private JLabel audioFileName;
    private JLabel timePlayed, timeTotal;
    private JButton playpause;
    private JSlider frames, volume;

    private Clip clip;
    private AudioInputStream inputStream;

    private double clipLenRatio;

    public AudioPlayer(File f) throws UnsupportedAudioFileException {
        super();
        this.audioWorker = new AudioWorker();
        loadAudioFile(f);
        initFrame();
        initComponents();
        System.out.println(f.getName());
    }

    private void loadAudioFile(File f) throws UnsupportedAudioFileException {
        this.audioFile = f;

        try {
            clip = AudioSystem.getClip();
            inputStream = AudioSystem.getAudioInputStream(audioFile);

            clip.open(inputStream);
            this.clipLenRatio = clip.getFrameLength() / ((clip.getMicrosecondLength() / 1000.0));
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initFrame() {
        setPreferredSize(new Dimension(550, 300));
        setBackground(Color.BLACK);
        setLayout(new FlowLayout());
    }

    private double specialNum = 100;


    private void initComponents() {
        setVolume(.2f);

        timePlayed = new JLabel("00:00:00");
        timeTotal = new JLabel("00:00:00");
        timeTotal.setText(FormattingUtils.getStringMs(clip.getMicrosecondLength() / 1000));
        timePlayed.setForeground(Color.WHITE);
        timeTotal.setForeground(Color.WHITE);

        audioFileName = new JLabel(audioFile.getName());
        audioFileName.setForeground(Color.WHITE);
        audioFileName.setFont(audioFileName.getFont().deriveFont(24f));

        frames = new JSlider(JSlider.HORIZONTAL, 0, (int) Math.floor(clip.getFrameLength() / specialNum), 0);
        System.out.println(clip.getFrameLength());
        frames.addChangeListener(e -> {
            JSlider slider = ((JSlider) e.getSource());
            if(slider.getValueIsAdjusting()) {
                clip.setFramePosition((int) Math.round(slider.getValue() * specialNum));
                slider.setValue(slider.getValue());

                //Update time played
                updateTimePlayed();
            }
        });
        frames.setMinorTickSpacing(25);
        frames.setMajorTickSpacing(50);
        frames.setPreferredSize(new Dimension(450, 40));

        playpause = new JButton("Play");
        playpause.setForeground(Color.WHITE);
        playpause.setBackground(Color.GRAY);
        playpause.setPreferredSize(new Dimension(100, 35));
        playpause.addActionListener(a -> {
            if(clip.isActive()) {
                pause();
                playpause.setText("Play");
            } else {
                play();
                playpause.setText("Pause");
            }
            playpause.updateUI();
        });

        clip.addLineListener(event -> {
            if(event.getFramePosition() == clip.getFrameLength()) {
                pause();
                clip.setFramePosition(0);
            }
        });



        add(audioFileName);
        add(SwingUtils2.CreateBox(500, 0));
        add(frames);
        Box timeContainer = Box.createHorizontalBox();
        timeContainer.setPreferredSize(new Dimension(475, 30));
        timeContainer.add(timePlayed);
        timeContainer.add(Box.createRigidArea(new Dimension(/*425*/375, 30)));
        timeContainer.add(timeTotal);
        add(timeContainer);
        add(SwingUtils2.CreateBox(500, 0));
        add(playpause);
    }

    private void play() {
        playpause.setText("Pause");
        if((frames.getMaximum() - specialNum) <= (frames.getValue() - specialNum)) {
            frames.setValue(0);
            clip.setFramePosition(0);
        }
        clip.start();
        audioWorker.execute();
        playpause.updateUI();
    }
    private void pause() {
        playpause.setText("Play");
        clip.stop();
        playpause.updateUI();
    }

    private void updateTimePlayed() {
        //Update play time
        //Loses accuracy as time goes on...? its accurate enough for its purpose
        timePlayed.setText(FormattingUtils.getStringMs(Math.round((clip.getFramePosition() * clipLenRatio) / 2000.0)));
    }

    private class AudioWorker extends SwingWorker<Void, Void> {

        private boolean adjusting = false;

        @Override
        protected Void doInBackground() {
            while(!isCancelled()) {
                while(clip.isOpen()) {
                    if(adjusting && !frames.getValueIsAdjusting()) {
                        clip.setFramePosition((int) Math.round(frames.getValue() * specialNum));
                        play();
                        adjusting = false;
                    }

                    while(clip.isRunning()) {
                        if (!frames.getValueIsAdjusting()) {
                            frames.setValue((int) Math.round(clip.getFramePosition() / specialNum));

                            //Update time played (somehow I made it work idk)
                            updateTimePlayed();

                            sleep(50);
                        } else {
                            adjusting = true;
                            pause();
                        }
                    }
                    pause();
                    sleep(200);
                }
                sleep(200);
            }
            return null;
        }
        private void sleep(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public float getVolume() {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        return (float) Math.pow(10f, gainControl.getValue() / 20f);
    }

    public void setVolume(float volume) {
        if (volume < 0f || volume > 1f)
            throw new IllegalArgumentException("Volume not valid: " + volume);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20f * (float) Math.log10(volume));
    }

    public void destroy() {
        if(clip.isOpen()) {
            if(clip.isRunning()) clip.stop();
            clip.close();
        }

        try {
            inputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        audioWorker.cancel(true);
    }

}
