package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import Common.Configuration;
import Common.FileOperations;
import Common.UserList;
import Audio.*;
/**
 * Main page GUI.
 */
public class MainPage extends javax.swing.JFrame {

    /**
     * Creates new form GUI.MainPage
     */
    public MainPage() {
        initComponents();
        StatusComboBox.setSelectedItem(Configuration.status);
        //Starting the threads
        new Thread(MainPage::refreshTablesMainPage).start();
        new Thread(MainPage::refreshConversations).start();
        checkNotifications();
    }

    /**
     * Refreshes Online users and all users tables every 5 second
     * If they are same it does not refreshes them
     */
    private static void refreshTablesMainPage() {
        while (true) {

            try {
                //getting the models for each table
                DefaultTableModel onlineUserTableModel = (DefaultTableModel) OnlineUserTable.getModel();
                DefaultTableModel allUsersTableModel = (DefaultTableModel) AllUsersTable.getModel();

                //reading the user list
                File file = new File(Configuration.InfoFolderName + File.separator + Configuration.UserListFileName + ".txt");
                BufferedReader br = new BufferedReader(new FileReader(file));

                //user online in the past
                Set<String> exonlineList = new HashSet<>();
                //users online now
                Set<String> newOnlineList = new HashSet<>();
                //user online in the past
                Set<String> exUsersList = new HashSet<>();
                //users online now
                Set<String> newUsersList = new HashSet<>();

                //true if online list changed
                boolean onlineListChanged = false;
                boolean userListChanged = false;
                //gets all the online users in the past in the list
                int onlineRowCount = onlineUserTableModel.getRowCount();
                for (int i = 0; i < onlineRowCount; i++) {
                    exonlineList.add((String) onlineUserTableModel.getValueAt(i, 0));
                }
                //gets all the users in the past in the list
                int allUsersCount = allUsersTableModel.getRowCount();
                for (int i = 0; i < allUsersCount; i++) {
                    exUsersList.add((String) allUsersTableModel.getValueAt(i, 0));
                }

                String st;
                while ((st = br.readLine()) != null) {
                    try {
                        String[] userInfo = st.split(",");
                        if (userInfo.length > 2) {


                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
                            Date lastSeen = sdf.parse(userInfo[0]);


                            Date date = new Date();
                            long diffInMillies = Math.abs(date.getTime() - lastSeen.getTime());
                            if (userInfo[2].equals("online") && diffInMillies < 8000) {

                                newOnlineList.add(userInfo[1]);
                                if (!exonlineList.contains(userInfo[1].trim())) {
                                    onlineListChanged = true;
                                }


                            }

                            newUsersList.add(userInfo[1]);
                            if (!exUsersList.contains(userInfo[1].trim())) {
                                userListChanged = true;
                            }

                        }

                        if (onlineListChanged) {
                            onlineUserTableModel.setRowCount(0);
                            for (String userName : newOnlineList) {
                                onlineUserTableModel.addRow(new Object[]{userName});
                            }

                        }
                        if (userListChanged) {
                            allUsersTableModel.setRowCount(0);
                            for (String userName : newUsersList) {
                                allUsersTableModel.addRow(new Object[]{userName});
                            }
                        }
                    } catch (ParseException e) {
                        //pass the element
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * Refreshes conversations table every 8 second
     */
    private static void refreshConversations() {

        while (true) {
            try {
                //getting the table model of last conversations
                DefaultTableModel lastConversationsTableModel = (DefaultTableModel) LastConversationsTable.getModel();

                //reading file that contains last conversations
                String[] lines = FileOperations.readFile(Configuration.InfoFolderName, Configuration.LastConversationsFileName);
                //deleting the last conversations table
                lastConversationsTableModel.setRowCount(0);
                //Setting all of the last conversations
                for (String line : lines) {
                    //Object[] originalRow = returnRow(index);
                    Object[] conversationLine = createLastConversationFromLine(line);
                    if (conversationLine != null) {
                        lastConversationsTableModel.addRow(conversationLine);
                    }
                }
            } catch (Exception e) {
                //not important
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //not important
            }
        }

    }

    /**
     * This method turns a conversation line to a row object
     *
     * @param line last conversation line
     * @return Object[] row of conversation line
     */
    private static Object[] createLastConversationFromLine(String line) {
        //splits the line
        String[] lineSplitted = line.split(",");
        if (lineSplitted.length == 3) {
            String status = UserList.checkStatus(lineSplitted[1].trim());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
            String messageDatestr = "";
            try {
                Date messageDate = sdf.parse(lineSplitted[0]);
                messageDatestr = messageDate.toString();
                messageDatestr = messageDatestr.replace("GMT 2018", "");
            } catch (ParseException e) {
                //can not parse the message
            }
            if (status.equals("online")) {
                return new Object[]{lineSplitted[1], status, lineSplitted[2], messageDatestr, ""};
            } else {
                return new Object[]{lineSplitted[1], status, lineSplitted[2], messageDatestr, UserList.getLastSeen(lineSplitted[1].trim()).replace("GMT 2018", "")};

            }
        }
        return null;
    }

    /**
     * message button in online users tab
     */
    private void MessageOnlineUserActionPerformed(java.awt.event.ActionEvent evt) {
        message(OnlineUserTable);
        dispose();
    }

    /**
     * message button in last Conversation tab
     */
    private void LastConversationsMessageButtonActionPerformed(java.awt.event.ActionEvent evt) {
        message(LastConversationsTable);
        dispose();
    }

    /**
     * message button in all users
     */
    private void AllUsersMessageButtonActionPerformed(java.awt.event.ActionEvent evt) {
        message(AllUsersTable);
        dispose();
    }

    /**
     * status combo box clicked
     */
    private void StatusComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        String status = (String) StatusComboBox.getSelectedItem();
        Configuration.status = status;
    }

    /**
     * Selects the user in the table.
     *
     * @param table the table (it can be Conversation,Online,all users table)
     */
    private static void message(JTable table) {
        int selectedRow = table.getSelectedRow();
        String UserId = (String) table.getValueAt(selectedRow, 0);
        MessagePage mp = new MessagePage(UserId);
        mp.setVisible(true);
    }

    /**
     * Notifies the user.
     *
     * @param text the text to be notified
     */
    public static void Notify(String text) {

        Configuration.notifications.add(text + "\n");
        String notificationsStr = "";
        for (String line : Configuration.notifications) {
            notificationsStr = notificationsStr + line;
        }
        jTextArea1.setText(notificationsStr);
    }

    /**
     * Checks notifications.
     */
    private static void checkNotifications() {
        String notificationsStr = "";
        for (String line : Configuration.notifications) {
            notificationsStr = notificationsStr + line;
        }
        jTextArea1.setText(notificationsStr);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    //This is the auto generated code by NetBeans I have added and modified some stuff
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        LastConversationsTable = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        LastConversationsMessageButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        OnlineUserTable = new javax.swing.JTable();
        MessageOnlineUser = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        AllUsersTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        AllUsersMessageButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        StatusComboBox = new javax.swing.JComboBox<>();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Last Conversations");

        LastConversationsTable.setAutoCreateRowSorter(true);
        LastConversationsTable.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        LastConversationsTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                        "UserID", "Status", "Last Message", "Last Time Messaged", "Last Time Seen"
                }
        ) {
            Class[] types = new Class[]{
                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                    false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        LastConversationsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        LastConversationsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(LastConversationsTable);

        jLabel3.setText("Choose conversation to continue messaging");

        LastConversationsMessageButton.setText("Message");
        LastConversationsMessageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LastConversationsMessageButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(307, 307, 307)
                                                .addComponent(jLabel1))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(39, 39, 39)
                                                .addComponent(jLabel3)
                                                .addGap(93, 93, 93)
                                                .addComponent(LastConversationsMessageButton)))
                                .addContainerGap(285, Short.MAX_VALUE))
                        .addComponent(jScrollPane1)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(11, 11, 11)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(LastConversationsMessageButton))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Latest Messages", jPanel1);

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel4.setText("Online Users");

        OnlineUserTable.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        OnlineUserTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "User ID"
                }
        ) {
            Class[] types = new Class[]{
                    java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                    false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        OnlineUserTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        OnlineUserTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(OnlineUserTable);

        MessageOnlineUser.setText("Message");
        MessageOnlineUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MessageOnlineUserActionPerformed(evt);
            }
        });

        jLabel7.setText("Select the user you want to send a message to");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addGap(347, 347, 347)
                                                .addComponent(jLabel4))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                                .addGap(105, 105, 105)
                                                                .addComponent(MessageOnlineUser))
                                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                                .addGap(38, 38, 38)
                                                                .addComponent(jLabel7)))))
                                .addContainerGap(83, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                .addGap(39, 39, 39))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(MessageOnlineUser)
                                                .addContainerGap(171, Short.MAX_VALUE))))
        );

        jTabbedPane1.addTab("Online Users", jPanel3);

        AllUsersTable.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        AllUsersTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "User ID"
                }
        ) {
            Class[] types = new Class[]{
                    java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                    false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        AllUsersTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        AllUsersTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(AllUsersTable);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("List Of All Users");

        AllUsersMessageButton.setText("Message");
        AllUsersMessageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AllUsersMessageButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Select the user you want to send a message to");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel2)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 434, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel6))
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addGap(99, 99, 99)
                                                .addComponent(AllUsersMessageButton)))
                                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addContainerGap(23, Short.MAX_VALUE)
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addGap(18, 18, 18)
                                                .addComponent(AllUsersMessageButton))
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(20, 20, 20))
        );

        jTabbedPane1.addTab("List of All Users", jPanel4);

        jLabel8.setText("Status:");

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel9.setText("Welcome hbk3");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setEditable(false);
        jScrollPane4.setViewportView(jTextArea1);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel5.setText("Notifications:");

        StatusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"online", "offline", "unavailable"}));
        StatusComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StatusComboBoxActionPerformed(evt);
            }
        });

        VoiceChatOnlineOflline = new JButton("Enable Voice Chat");
        VoiceChatOnlineOflline.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (!VoiceSpeakListen.isEnabled()) {
                    VoiceSpeakListen.setEnabled(true);
                    VoiceChatOnlineOflline.setText("Disable Voice Chat");
                    Configuration.voiceChatDisabled = false;
                    new Thread(AudioReceive::startVoiceChatListen).start();
                } else {
                    VoiceSpeakListen.setEnabled(false);
                    Configuration.voiceChatDisabled = true;
                    VoiceSpeakListen.setText("Press to Speak");
                    VoiceChatOnlineOflline.setText("Enable Voice Chat");
                }

            }
        });
        VoiceSpeakListen = new JButton("Press to Speak");
        VoiceSpeakListen.setEnabled(false);
        VoiceSpeakListen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (VoiceSpeakListen.getText().equals("Press to Speak")) {
                    VoiceSpeakListen.setText("Press to Listen");
                    Configuration.voiceChatSpeakDisabled = false;
                    new Thread(AudioSender::startVoiceChatSpeak).start();
                } else {
                    Configuration.voiceChatSpeakDisabled = true;
                    VoiceSpeakListen.setText("Press to Speak");

                }
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(jSeparator1, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(21)
                                                .addComponent(VoiceChatOnlineOflline)
                                                .addGap(18)
                                                .addComponent(VoiceSpeakListen)
                                                .addGap(128)
                                                .addComponent(jLabel9)
                                                .addGap(100)
                                                .addComponent(jLabel8)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(StatusComboBox, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)
                                                .addGap(32))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(jTabbedPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(6)
                                                                .addComponent(jLabel5)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                                .addContainerGap())))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(jLabel9)
                                                .addComponent(jLabel8)
                                                .addComponent(StatusComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(VoiceChatOnlineOflline)
                                                .addComponent(VoiceSpeakListen)))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(jTabbedPane1, GroupLayout.PREFERRED_SIZE, 286, GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(18)
                                                .addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(32)
                                                .addComponent(jLabel5)))
                                .addContainerGap(43, Short.MAX_VALUE))
        );
        getContentPane().setLayout(layout);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    //Auto generated code finish


    // Variables declaration
    private javax.swing.JButton AllUsersMessageButton;
    private static javax.swing.JTable AllUsersTable;
    private javax.swing.JButton LastConversationsMessageButton;
    private static javax.swing.JTable LastConversationsTable;
    private javax.swing.JButton MessageOnlineUser;
    private static javax.swing.JTable OnlineUserTable;
    private javax.swing.JComboBox<String> StatusComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private static javax.swing.JTextArea jTextArea1;
    private JButton VoiceChatOnlineOflline;
    private JButton VoiceSpeakListen;
    // End of variables


}
