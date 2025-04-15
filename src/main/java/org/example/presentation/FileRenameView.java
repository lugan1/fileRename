package org.example.presentation;

import org.example.intent.AddFilesIntent;
import org.example.intent.PatternChangedIntent;
import org.example.intent.RenameFilesIntent;
import org.example.intent.StartNumberChangedIntent;
import org.example.state.FileRenameState;
import org.example.viewmodel.FileRenameViewModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.awt.dnd.DnDConstants.ACTION_COPY;

/**
 * FileRenameView 클래스는 Swing 기반의 UI(View) 역할을 하며,
 * 사용자의 입력을 받아 해당 인텐트를 FileRenameViewModel에 전달하고,
 * ViewModel에서 전달받은 상태(FileRenameState)에 따라 UI를 갱신합니다.
 */
public class FileRenameView extends JFrame implements FileRenameViewModel.StateListener {

    // ViewModel: 상태 관리와 인텐트 처리를 담당하는 객체 (DI로 주입됨)
    private final FileRenameViewModel viewModel;

    // 파일 목록을 관리하는 리스트 모델과 JList
    private final DefaultListModel<File> fileListModel = new DefaultListModel<>();
    private final JList<File> fileList = new JList<>(fileListModel);

    // 새 파일명 패턴과 시작 번호를 입력받는 텍스트 필드
    private final JTextField newNamePatternField = new JTextField(30);
    private final JTextField startNumberField = new JTextField("1", 5);

    // 로그 메시지를 출력할 텍스트 영역
    private final JTextArea logTextArea = new JTextArea(10, 40);


    /**
     * 생성자: FileRenameView 객체를 생성하고, ViewModel을 DI하여 연결합니다.
     * 또한, ViewModel에 자신(StateListener)를 등록하고 UI 초기화(initComponents)를 수행합니다.
     *
     * @param viewModel 파일 이름 변경 관련 비즈니스 로직과 상태 관리하는 ViewModel 객체
     */
    public FileRenameView(FileRenameViewModel viewModel) {
        super("파일 이름 일괄 변경 프로그램 (MVI)");
        this.viewModel = viewModel;
        viewModel.setListener(this);
        initComponents();
    }

    /**
     * initComponents() 메서드는 UI의 각 패널을 구성하고,
     * 각 컴포넌트를 배치한 후 JFrame의 컨텐츠 페인으로 설정합니다.
     */
    private void initComponents() {
        // JFrame 기본 속성 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 550);
        setLocationRelativeTo(null);

        // 1. 파일 목록 패널 생성
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("파일 목록 (드래그 앤 드롭 또는 파일 추가)"));
        fileList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        filePanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        // 파일 추가 버튼 생성 및 액션 리스너 등록
        JButton addFileButton = new JButton("파일 추가");
        addFileButton.addActionListener(e -> {
            // JFileChooser를 통해 파일을 선택함
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fc.getSelectedFiles();
                List<File> files = new ArrayList<>();
                Collections.addAll(files, selectedFiles);
                // 파일 추가 인텐트를 ViewModel에 전달
                viewModel.processIntent(new AddFilesIntent(files));
            }
        });
        filePanel.add(addFileButton, BorderLayout.SOUTH);

        // 드래그 앤 드롭을 위한 DropTarget 리스너 등록
        new DropTarget(fileList, new FileDropTargetListener());

        // 2. 패턴 및 시작 번호 입력 패널 생성 (GridLayout 사용)
        JPanel patternPanel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        patternPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("새 파일명 및 시작 번호 입력"));
        patternPanel.add(new JLabel("새 파일명 패턴 (예: A0[0-9].smi):"));
        patternPanel.add(newNamePatternField);
        patternPanel.add(new JLabel("시작 번호:"));
        patternPanel.add(startNumberField);

        addTextFieldListener();

        // 3. 이름 변경 실행 패널 생성
        JPanel renamePanel = new JPanel();
        JButton renameButton = new JButton("이름 변경 실행");
        renameButton.addActionListener(e -> {
            // 입력 필드에서 새 파일명 패턴과 시작 번호를 읽어옴
            String pattern = newNamePatternField.getText().trim();
            int startNumber;
            try {
                startNumber = Integer.parseInt(startNumberField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "시작 번호는 유효한 숫자여야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 이름 변경 인텐트를 생성하여 ViewModel에 전달
            viewModel.processIntent(new RenameFilesIntent(pattern, startNumber));
        });
        renamePanel.add(renameButton);

        // 4. 로그 출력 패널 생성
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("작업 로그"));
        logTextArea.setEditable(false);
        logPanel.add(new JScrollPane(logTextArea), BorderLayout.CENTER);

        // 5. 모든 패널을 하나의 메인 패널에 수직(BoxLayout.Y_AXIS)으로 배치
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(filePanel);
        mainPanel.add(patternPanel);
        mainPanel.add(renamePanel);
        mainPanel.add(logPanel);
        setContentPane(mainPanel);
    }

    /**
     * onStateChanged() 메서드는 ViewModel에서 상태가 변경되었을 때 호출됩니다.
     * 전달받은 FileRenameState 객체의 데이터(파일 목록 및 로그 메시지)에 따라
     * UI의 파일 목록과 로그 텍스트 영역을 업데이트합니다.
     *
     * @param state 최신 FileRenameState 객체
     */
    @Override
    public void onStateChanged(FileRenameState state) {
        // 파일 목록 비교 후 다를 경우만 갱신
        List<File> newFileList = state.getFileList();
        if (!fileListModelEquals(newFileList)) {
            fileListModel.clear();
            for (File file : newFileList) {
                fileListModel.addElement(file);
            }
        }

        // 로그 텍스트 비교 후 다를 경우만 갱신
        List<String> newLogs = state.getLogMessages();
        String newLogText = String.join("\n", newLogs) + "\n";
        if (!logTextArea.getText().equals(newLogText)) {
            logTextArea.setText(newLogText);
        }
    }

    private boolean fileListModelEquals(List<File> newFiles) {
        if (fileListModel.size() != newFiles.size()) return false;
        for (int i = 0; i < fileListModel.size(); i++) {
            File existing = fileListModel.get(i);
            File incoming = newFiles.get(i);
            if (!existing.equals(incoming)) return false;
        }
        return true;
    }

    private void addTextFieldListener() {
        newNamePatternField.getDocument().addDocumentListener(new DocumentListener() {
            final String text = newNamePatternField.getText().trim();
            @Override
            public void insertUpdate(DocumentEvent e) {

                // 이전 값과 다를 경우에만 처리하도록 할 수도 있음
                viewModel.processIntent(new PatternChangedIntent(text));
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                // 이전 값과 다를 경우에만 처리하도록 할 수도 있음
                viewModel.processIntent(new PatternChangedIntent(text));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // 설명: 속성(attribute) 이 바뀌었을 때 호출됩니다. 즉, 내용(text) 은 바뀌지 않지만, 서식, 스타일 등이 바뀔 때 사용됩니다.
                //
                //예시 상황:
                //
                //JTextPane에서 글자에 bold, italic, color 등 속성 변경
            }
        });

        // 비슷하게 시작번호 입력에 대해서도 디바운싱 적용 (단, 숫자 파싱 에러 처리 필요)
        startNumberField.getDocument().addDocumentListener(new DocumentListener() {
            final int startNumber = Integer.parseInt(startNumberField.getText().trim());
            @Override public void insertUpdate(DocumentEvent e) {
                viewModel.processIntent(new StartNumberChangedIntent(startNumber));
            }
            @Override public void removeUpdate(DocumentEvent e) {
                viewModel.processIntent(new StartNumberChangedIntent(startNumber));
            }
            @Override public void changedUpdate(DocumentEvent e) { }
        });
    }

    /**
     * FileDropTargetListener 클래스는 파일을 드래그 앤 드롭할 때 발생하는 이벤트를 처리합니다.
     * 파일이 드롭되면, 해당 파일들을 AddFilesIntent로 ViewModel에 전달합니다.
     */
    private class FileDropTargetListener implements DropTargetListener {
        @Override public void dragEnter(DropTargetDragEvent dtde) { }
        @Override public void dragOver(DropTargetDragEvent dtde) { }
        @Override public void dropActionChanged(DropTargetDragEvent dtde) { }
        @Override public void dragExit(DropTargetEvent dte) { }

        /**
         * drop() 메서드는 드래그 앤 드롭된 데이터를 받아 파일 목록으로 변환한 후,
         * AddFilesIntent를 생성하여 ViewModel에 전달합니다.
         *
         * @param dtde 드롭 이벤트 정보가 담긴 객체
         */
        @Override
        public void drop(DropTargetDropEvent dtde) {
            // 파일 복사 작업을 위해 ACTION_COPY 모드로 설정
            dtde.acceptDrop(ACTION_COPY);
            try {
                // 드롭된 데이터에서 파일 리스트를 추출
                Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                List<File> files = new ArrayList<>();
                if (transferData instanceof List<?>) {
                    for (Object item : (List<?>) transferData) {
                        if (item instanceof File) {
                            files.add((File) item);
                        }
                    }
                }
                if (!files.isEmpty()) {
                    // 파일 추가 인텐트를 생성하여 ViewModel에 전달
                    viewModel.processIntent(new AddFilesIntent(files));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            dtde.dropComplete(true);
        }
    }
}
