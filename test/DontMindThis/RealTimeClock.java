package DontMindThis;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RealTimeClock extends JFrame {

    private JLabel jLabelDateTime;
    

    public RealTimeClock() {
        initComponents();
        startClock();
    }

    private void initComponents() {
        jLabelDateTime = new JLabel();
        JPanel panel = new JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Real-Time Date & Time");
        setSize(400, 150);
        setLocationRelativeTo(null); // Center window

        
        jLabelDateTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        panel.setLayout(new java.awt.BorderLayout());
        panel.add(jLabelDateTime, java.awt.BorderLayout.CENTER);

        add(panel);
    }

    private void startClock() {
    Timer timer = new Timer(1000, e -> {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        jLabelDateTime.setText(formattedDateTime);

        // Print to terminal
        System.out.println("Current Date and Time: " + formattedDateTime);
    });
    timer.start();
}


    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new RealTimeClock().setVisible(true);
        });
    }
}

