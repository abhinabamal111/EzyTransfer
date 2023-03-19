package com.fileTransfer.EzYTransfer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class EzyTransfer
{
  public static void main(String[] args)
  {
    JFrame f = new JFrame("ET-EzyTransfer");
    final JTextField tf = new JTextField();
    tf.setBounds(70, 50, 150, 20);
    JButton send = new JButton("Send");
    send.setBounds(70, 100, 95, 30);
    JButton receive = new JButton("Receive");
    receive.setBounds(70, 160, 95, 30);

    JProgressBar singleFile = new JProgressBar();

    singleFile.setBounds(50, 200, 180, 20);

    singleFile.setStringPainted(true);
    f.add(singleFile);
    final JProgressBar multiFile = new JProgressBar();
    multiFile.setBounds(10, 240, 250, 20);
    multiFile.setValue(0);

    f.add(multiFile);

    multiFile.setStringPainted(true);

    send.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ac)
      {
        try {
          int BUFFERSIZE = 40960;
          InetAddress inetAddress = InetAddress.getLocalHost();

          String ip = inetAddress.getHostAddress();
          Socket s = new Socket(ip, 8999);
          System.out.println("will read");
          JFileChooser fileChooser = new JFileChooser();
          fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

          fileChooser.setMultiSelectionEnabled(true);
          fileChooser.showOpenDialog(null);
          File[] selectedFiles = fileChooser.getSelectedFiles();

          DataOutputStream dfileselectors = new DataOutputStream(s.getOutputStream());
          DataOutputStream dos = null;
          DataOutputStream dout = null;
          DataInputStream handSaking = null;
          DataOutputStream handSakingFromClientSide = null;

          System.out.println("Total number of files selected:---" + selectedFiles.length);

          String totalFiles = Integer.toString(selectedFiles.length);
          dfileselectors.writeUTF(totalFiles);
          dfileselectors.flush();
          for (int i = 0; i < selectedFiles.length; i++)
          {
           // EzyTransfer.this.setValue(0);
            File flimg = new File(selectedFiles[i].getAbsolutePath());
            System.out.println("image read");
            FileInputStream fin = new FileInputStream(flimg);

            System.out.println("image buffered");
            dos = new DataOutputStream(s.getOutputStream());

            long imagesize = flimg.length();
            int loopcount = (int)(imagesize / 40960L);

            String fileName = selectedFiles[i].getName();

            String count = Integer.toString(loopcount) + "%" + fileName;
            if (i != 0)
            {
              handSakingFromClientSide = new DataOutputStream(s.getOutputStream());
              handSakingFromClientSide.writeUTF("Ready");
            }

            System.out.println(count);
            dos.writeUTF(count);
            dos.flush();

            byte[] buffer = new byte[40960];

            int size = 0;
            int readNum;
            while ((readNum = fin.read(buffer)) != -1)
            {
              //int readNum;
              dout = new DataOutputStream(s.getOutputStream());

              String imageString = Base64.getEncoder().encodeToString(buffer);

              dout.writeUTF(imageString);
              dout.flush();
              size += readNum;
              int done = (int)(size * 100 / imagesize);

              //EzyTransfer.this.setValue(done);
              System.gc();
            }

            System.out.println("writen done");

            multiFile.setValue((i + 1) * 100 / selectedFiles.length);
            if (selectedFiles.length != 1) {
              System.out.println("Inside handsaking if");
              for (int j = 0; j < 1000; j++)
              {
                handSaking = new DataInputStream(s.getInputStream());
                System.out.println("Inside handsaking for");
                String signal = handSaking.readUTF();
                System.out.println(signal);

                if (signal.equalsIgnoreCase("Send Another one"))
                {
                  break;
                }
              }

            }

          }

          System.out.println("All Files sent..............");
          dout.close();
          dos.close();
          handSaking.close();
          handSakingFromClientSide.close();

          dfileselectors.close();
          s.close();
          tf.setText("File is sent"); } catch (Exception e) {
          System.out.println("Client Side: " + e);
        }
      }
    });
    receive.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ac)
      {
        try {
          JFileChooser chooser = new JFileChooser();
          chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
          File pathToSave = chooser.getCurrentDirectory();
          String path = pathToSave.getAbsolutePath();
          System.out.println(path);
          ServerSocket ss = new ServerSocket(8999);
          Socket s = ss.accept();
          System.out.println("Connection established.");
          DataInputStream totalFile = new DataInputStream(s.getInputStream());
          System.out.println("datainputstream is on");
          String count = totalFile.readUTF();
          System.out.println("Total count of files being sent is received: -- " + count);
          DataInputStream is = null;
          DataInputStream dis = null;
          DataOutputStream handSaking = null;
          DataInputStream handSakingFromClientSide = null;

          for (int i = 0; i < Integer.parseInt(count); i++)
          {
            //EzyTransfer.this.setValue(0);
            if (i != 0) {
              System.out.println("Inside handsaking if");
              for (int j = 0; j < 1000; j++)
              {
                handSakingFromClientSide = new DataInputStream(s.getInputStream());
                System.out.println("Inside handSakingFromClientSide for");
                String signal = handSakingFromClientSide.readUTF();
                System.out.println(signal);

                if (signal.equalsIgnoreCase("Ready"))
                {
                  break;
                }
              }
            }
            is = new DataInputStream(s.getInputStream());
            String file = is.readUTF();

            String[] arr = file.split("%");
            System.out.println("File split");

            int loopCount = Integer.parseInt(arr[0]);

            String fileName = arr[1];
            System.out.println(fileName);

            File video = new File(path + "//" + fileName);

            FileOutputStream fw = new FileOutputStream(video);
            byte[] videoBytefull = null;

            int looCountforProgressbar = loopCount;
            while (loopCount != 0) {
              dis = new DataInputStream(s.getInputStream());
              String imageString = dis.readUTF();

              videoBytefull = Base64.getDecoder().decode(imageString);

              fw.write(videoBytefull);
              int done = loopCount;
              loopCount--;
              int complete = (done - loopCount) * 100 / looCountforProgressbar;
              //EzyTransfer.this.setValue(complete);
              System.gc();
            }

            System.out.println("byte array with image is created");

            System.out.println("Video recreated and saved");

            fw.flush();
            fw.close();
            multiFile.setValue((i + 1) * 100 / Integer.parseInt(count));
            handSaking = new DataOutputStream(s.getOutputStream());
            handSaking.writeUTF("Send Another one");
            handSaking.flush();
            System.gc();
          }

          dis.close();
          totalFile.close();
          handSaking.close();
          is.close();

          ss.close();

          tf.setText("File is Received"); } catch (Exception e) {
          System.out.println("ServerSIde: " + e);
        }
      }
    });
    f.add(send); f.add(receive); f.add(tf);
    f.setSize(400, 400);
    f.setLayout(null);
    f.setVisible(true);
  }
}