import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;

public class TextEditor extends JFrame implements ActionListener{
    ImageIcon icon =new ImageIcon(getClass().getResource("icon.png"));
    JFileChooser fileChooser;
    JTextArea textArea;
    JSpinner fontSizeSpinner;
    JPanel controlPanel;
    JLabel fontSizeLabel;
    JButton fontColorButton;
    JComboBox fontBox;
    File currentFile;
    JPanel drawingPanel;
    private String selectedShape = "Rectangle"; // Default shape
    private Shape currentShape = null;
    private ArrayList<Shape> shapes = new ArrayList<>();

    TextEditor(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500,500);
        this.setTitle("Text-Editor");


        //adding the text area to the editor
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText("Type here your are writing in the text editor made by Eklavya");
        textArea.setFont(new Font("Segoe UI",Font.PLAIN,16));
        textArea.setMargin(new Insets(10,10,10,10));


        // adding the scroll for choosing the size of the font
        fontSizeSpinner = new JSpinner();
        fontSizeSpinner.setPreferredSize(new Dimension(50,20));
        fontSizeSpinner.setValue(20);
        fontSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                textArea.setFont(new Font(textArea.getFont().getFamily(),Font.PLAIN,(int)fontSizeSpinner.getValue()));
            }
        });

        controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fontSizeLabel = new JLabel("Font Size:");
        controlPanel.add(fontSizeLabel);
        controlPanel.add(fontSizeSpinner);

        //adding the scrollbar to the textArea
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        //choosing the file
        fileChooser = new JFileChooser();

        // Drawing panel
        JComboBox<String> shapeSelector = new JComboBox<>(new String[]{"Rectangle", "Oval", "Line","Polygon", "Curve"});
        shapeSelector.addActionListener(e -> selectedShape = (String) shapeSelector.getSelectedItem());
        controlPanel.add(shapeSelector);

        // Add Clear Button
        JButton clearButton = new JButton("Clear Drawing");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shapes.clear(); // Clears all the shapes in the list
                drawingPanel.repaint(); // Repaints the drawing panel
            }
        });
        controlPanel.add(clearButton);

        setupDrawingPanel();

        // Split pane for text editor and drawing panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, drawingPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.7);
        this.add(splitPane, BorderLayout.CENTER);

        //adding the menubar to the frame
        this.setJMenuBar(createMenuBar());

        //adding the font menu to choose which font to add
        controlPanel.add(changeFont());


        //adding the button to change the button for changing the color
        fontColorButton = new JButton("Change Text Color");
        fontColorButton.addActionListener(this);
        controlPanel.add(fontColorButton);

        //adding scrollPane to the frame
//        this.add(scrollPane,BorderLayout.CENTER);
        this.add(controlPanel,BorderLayout.NORTH);
        this.add(splitPane,BorderLayout.CENTER);
        this.setIconImage(icon.getImage());
        this.setVisible(true);
    }

    //adding the menuBar
    private JMenuBar createMenuBar(){
        JMenuBar menuBar = new JMenuBar();

        //adding the menu items
        JMenu file = new JMenu("file");
        JMenu edit = new JMenu("edit");
        JMenu exit = new JMenu("exit");
        JMenu view = new JMenu("view");
        JMenu count = new JMenu("count");

        //adding the file items
        JMenuItem save = new JMenuItem("save as");
        save.addActionListener(e-> saveFile());

        JMenuItem open = new JMenuItem("open new file");
        open.addActionListener(e-> openFile());

        //find and replace functionality
        JMenuItem findAndReplace = new JMenuItem("find and replace");
        findAndReplace.addActionListener(e -> findAndReplace());

        JMenuItem yesExit = new JMenuItem("YES");
        yesExit.addActionListener(e-> System.exit(0));

        JMenuItem noExit = new JMenuItem("NO");
        //noExit.addActionListener(e-> stay());

        JMenuItem saveChanges = new JMenuItem("save");
        saveChanges.addActionListener(e-> saveChanges());

        //cut the text
        JMenuItem cutText = new JMenuItem("Cut");
        cutText.addActionListener(e-> textArea.cut());

        //copy the text
        JMenuItem copyText = new JMenuItem("Copy");
        copyText.addActionListener(e->textArea.copy());

        //paste the text copied
        JMenuItem pasteText = new JMenuItem("Paste");
        pasteText.addActionListener(e-> textArea.paste());

        //to upperCase
        JMenuItem upperCase = new JMenuItem("toUpperCase");
        upperCase.addActionListener(e-> touppercase());

        //to lowerCase
        JMenuItem lowerCase = new JMenuItem("toLowerCase");
        lowerCase.addActionListener(e-> tolowercase());

        JMenuItem countWords = new JMenuItem("Words and Characters");
        countWords.addActionListener(e-> CountWords());

        view.add(findAndReplace);

        edit.add(cutText);
        edit.add(copyText);
        edit.add(pasteText);
        edit.add(upperCase);
        edit.add(lowerCase);

        file.add(save);
        file.add(saveChanges);
        file.add(open);

        exit.add(yesExit);
        exit.add(noExit);

        count.add(countWords);
//        file.add(add);

        //adding the menubar to the textEditor
        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(view);
        menuBar.add(exit);
        menuBar.add(count);

        return menuBar;
    }

    // BufferWriter and Bufferreader are use to read and write a file

    //save the file functionality
    private void saveFile(){
        int response = fileChooser.showSaveDialog(this);

        if(response==JFileChooser.APPROVE_OPTION){
                currentFile = fileChooser.getSelectedFile();
                // we can use getSelectedPath() also but for absolute path only
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                    writer.write(textArea.getText());
                    JOptionPane.showMessageDialog(this,"File has been saved successfully!");
            }
            catch (IOException e){
                    JOptionPane.showMessageDialog(this,"Error saving in the file" + e.getMessage());
            }
        }
    }

    //using bufferReader to open the file and storing the preExisted content in the textarea
    private void openFile(){
        int response = fileChooser.showOpenDialog(this);

        if(response==JFileChooser.APPROVE_OPTION){
            currentFile = fileChooser.getSelectedFile();

            try(BufferedReader reader = new BufferedReader(new FileReader(currentFile))){
                StringBuilder content = new StringBuilder();
                String fileContent;

                while ((fileContent = reader.readLine()) != null){
                    content.append(fileContent).append("\n");
                }

                textArea.setText(content.toString());
                JOptionPane.showMessageDialog(this,"File opened Successfully!");
            }catch (IOException e){
                JOptionPane.showMessageDialog(this,"Error opening in the file");
            }
        }

    }

    private JComboBox changeFont(){
        String[] font = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontBox = new JComboBox(font);
        fontBox.setSelectedItem("Aerial");
        fontBox.setPreferredSize(new Dimension(100,25));
        fontBox.addActionListener(this);
        return fontBox;
    }

    private void touppercase(){
        String selectedText = textArea.getSelectedText();

        if(selectedText!=null){
            textArea.replaceSelection(selectedText.toUpperCase());
        }
    }

    private void tolowercase(){
        String selectedText = textArea.getSelectedText();

        if(selectedText!=null){
            textArea.replaceSelection(selectedText.toLowerCase());
        }
    }

    private void saveChanges(){
        if(currentFile!= null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))){
                writer.write(textArea.getText());
                JOptionPane.showMessageDialog(this,"saved changes successfully");
            }catch (IOException e){
                JOptionPane.showMessageDialog(this,"Error saving changes:"+ e.getMessage());
            }
        }else{
            JOptionPane.showMessageDialog(this,"No selected.Use save as to save the file");
        }
        }

    private void findAndReplace(){
        //we will create and dialog for find , replace and replace all

        //1) created the dialog
        JDialog dialog = new JDialog(this,"Find and Replace",true);
        dialog.setLayout(new GridLayout(4,2,10,10));
        dialog.setSize(400,200);
        dialog.setLocationRelativeTo(this);

        //2)adding labels
        JLabel findLabel = new JLabel("Find:");
        JTextArea findText = new JTextArea();

        JLabel replaceLabel = new JLabel("Replace with:");
        JTextArea replaceText = new JTextArea();

        JButton findButton = new JButton("Find");
        JButton replaceButton = new JButton("Replace");
        JButton replaceAll = new JButton("Replace all");
        JButton close = new JButton("Close");

        //adding components to the dialog
        dialog.add(findLabel);
        dialog.add(findText);

        dialog.add(replaceLabel);
        dialog.add(replaceText);

        dialog.add(findButton);
        dialog.add(replaceButton);
        dialog.add(replaceAll);
        dialog.add(close);

        // word find highlight
        Highlighter highlight = textArea.getHighlighter();
        Highlighter.HighlightPainter paint = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

        //find button will find the text inputed and will highlight it
        findButton.addActionListener(e -> {
            String text = textArea.getText();
            String wordToFind = findText.getText();

            if(wordToFind.isEmpty()){
                JOptionPane.showMessageDialog(this,"Please enter word to find.");
                return;
            }

            highlight.removeAllHighlights();

            String[] words = text.split("\\s+"); // Split text into words
            for (String word : words) {
                if (word.contains(wordToFind)) {
                    try {
                        int index = text.indexOf(word);
                        highlight.addHighlight(index, index + word.length(), paint);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Replace the word
        replaceButton.addActionListener(e -> {
            String wordToFind = findText.getText();
            String replaceWord = replaceText.getText();

            if (wordToFind.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the word to find.");
                return;
            }
            try {
                String text = textArea.getText();
                int index = text.indexOf(wordToFind);

                if (index == -1) {
                    JOptionPane.showMessageDialog(this, "Word not found!");
                    return;
                }

            String beforeWord = text.substring(0, index);
            String afterWord = text.substring(index + wordToFind.length());
            String updatedText = beforeWord + replaceWord + afterWord;

            textArea.setText(updatedText);
            highlight.removeAllHighlights();

            //re-highlight the replaced word
            highlight.addHighlight(index, index + replaceWord.length(),
                    new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        });

        //replace all functionality
        replaceAll.addActionListener(e -> {
            String wordToFind = findText.getText();
            String replaceWord = replaceText.getText();

            if(wordToFind.isEmpty()){
                JOptionPane.showMessageDialog(this,"Please enter the word to replace!");
                return;
            }

            try{
                String text = textArea.getText();
                String[] words = text.split("\\s+");

                StringBuilder updateText = new StringBuilder();

                for(String word : words){
                    if (word.contains(wordToFind)){
                        updateText.append(word.replace(wordToFind,replaceWord)).append(" ");
                    }else {
                        updateText.append(word).append(" ");
                    }
                }

                textArea.setText(updateText.toString().trim());
                highlight.removeAllHighlights();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });

        close.addActionListener(e-> {
            highlight.removeAllHighlights();
            dialog.setVisible(false);
        });

        dialog.setVisible(true);
    }

    private void CountWords() {
//        try {
            String selectedText = textArea.getSelectedText();

            if (selectedText == null || selectedText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No text has been selected!Please select an text.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] words = selectedText.trim().split("\\s+");
            int wordsCount = words.length;

            int characterCount = selectedText.length();

            JOptionPane.showMessageDialog(this, "Selected Text:\nWords: " + wordsCount + "\nCharacter Count: " + characterCount, "Selection Count", JOptionPane.INFORMATION_MESSAGE);
//        }catch (Exception e){
//            // Handle any unexpected exceptions
//            JOptionPane.showMessageDialog(this,
//                    "An error occurred while processing your request. Please try again.",
//                    "Error",
//                    JOptionPane.ERROR_MESSAGE);
//
//            // Optionally, print the exception to the console for debugging
////            e.printStackTrace();
//        }
    }
    private void setupDrawingPanel() {
        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Set a visible stroke for shapes
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.setColor(Color.BLACK);

                // Draw existing shapes
                for (Shape shape : shapes) {
                    if (shape instanceof Rectangle2D) {
                        g2d.draw((Rectangle2D) shape);
                    } else if (shape instanceof Ellipse2D) {
                        g2d.draw((Ellipse2D) shape);
                    } else if (shape instanceof Line2D) {
                        g2d.draw((Line2D) shape);
                    } else if (shape instanceof Polygon) {
                        g2d.draw((Polygon) shape);
                    } else if (shape instanceof QuadCurve2D) {
                        g2d.draw((QuadCurve2D) shape);
                    }
                }

                // Draw the current shape being created
                if (currentShape != null) {
                    g2d.draw(currentShape);
                }
            }
        };

        drawingPanel.setPreferredSize(new Dimension(200, 500));
        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point start = e.getPoint();
                if ("Rectangle".equals(selectedShape)) {
                    currentShape = new Rectangle2D.Double(start.x, start.y, 0, 0);
                } else if ("Oval".equals(selectedShape)) {
                    currentShape = new Ellipse2D.Double(start.x, start.y, 0, 0);
                } else if ("Line".equals(selectedShape)) {
                    currentShape = new Line2D.Double(start.x, start.y, start.x, start.y);
                } else if ("Polygon".equals(selectedShape)) {
                    // Start a polygon with a single point, add more points on drag
                    Polygon polygon = new Polygon();
                    polygon.addPoint(start.x, start.y);
                    currentShape = polygon;
                } else if ("Curve".equals(selectedShape)) {
                    // Start with a simple quadratic curve
                    currentShape = new QuadCurve2D.Double(start.x, start.y, start.x + 50, start.y + 50, start.x + 100, start.y);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentShape != null) {
                    if (currentShape instanceof Polygon) {
                        Polygon polygon = (Polygon) currentShape;
                        polygon.addPoint(e.getX(), e.getY()); // Add the final point to the polygon
                    }
                    shapes.add(currentShape); // Add to the shapes list
                    currentShape = null; // Reset the current shape
                    drawingPanel.repaint(); // Repaint the panel
                }
            }
        });

        drawingPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentShape != null) {
                    Point end = e.getPoint();
                    if (currentShape instanceof Rectangle2D) {
                        Rectangle2D rect = (Rectangle2D) currentShape;
                        rect.setFrameFromDiagonal(rect.getX(), rect.getY(), end.x, end.y);
                    } else if (currentShape instanceof Ellipse2D) {
                        Ellipse2D ellipse = (Ellipse2D) currentShape;
                        ellipse.setFrameFromDiagonal(ellipse.getX(), ellipse.getY(), end.x, end.y);
                    } else if (currentShape instanceof Line2D) {
                        Line2D line = (Line2D) currentShape;
                        line.setLine(line.getX1(), line.getY1(), end.x, end.y);
                    } else if (currentShape instanceof Polygon) {
                        // For polygon, we do nothing in mouseDragged but add points on mousePressed
                    } else if (currentShape instanceof QuadCurve2D) {
                        QuadCurve2D curve = (QuadCurve2D) currentShape;
                        curve.setCurve(curve.getX1(), curve.getY1(), e.getX(), e.getY(), curve.getX2(), curve.getY2());
                    }
                    drawingPanel.repaint();
                }
            }
        });
    }

    //changing the font
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==fontColorButton){
            JColorChooser colorChooser = new JColorChooser();

            Color color = JColorChooser.showDialog(null,"Pick Font Color",Color.black);
            textArea.setForeground(color);
        }

        if(e.getSource()==fontBox){
            textArea.setFont(new Font((String) fontBox.getSelectedItem(),Font.PLAIN,textArea.getFont().getSize()));
        }
    }
}
