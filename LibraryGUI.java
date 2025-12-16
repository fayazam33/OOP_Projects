

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;


interface StorageInterface {
    ArrayList<Book> load();
    void save(ArrayList<Book> books);
}


class FileStorage implements StorageInterface {
    private final String filename = "library_data.txt";

    public ArrayList<Book> load() {
        ArrayList<Book> books = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2)
                    books.add(new Book(parts[0], parts[1]));
            }
        } catch (IOException ignored) {}
        return books;
    }

    public void save(ArrayList<Book> books) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Book b : books) {
                pw.println(b.getTitle() + "," + b.getAuthor());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

abstract class LibraryItem {
    protected String title;
    protected String author;

    public LibraryItem(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public abstract String display();
}


class Book extends LibraryItem {
    public Book(String title, String author) {
        super(title, author);
    }

    public String display() {
        return "📘  " + title + "   |   ✍ " + author;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
}


class LibraryManager {
    private ArrayList<Book> books;
    private StorageInterface storage;

    public LibraryManager(StorageInterface storage) {
        this.storage = storage;
        this.books = storage.load();
    }

    public boolean isDuplicate(String title, String author) {
        for (Book b : books) {
            if (b.getTitle().equalsIgnoreCase(title) &&
                b.getAuthor().equalsIgnoreCase(author)) return true;
        }
        return false;
    }

    public void addBook(String title, String author) {
        books.add(new Book(title, author));
        storage.save(books);
    }

    public void updateBook(int index, String title, String author) {
        books.set(index, new Book(title, author));
        storage.save(books);
    }

    public void deleteBook(int index) {
        books.remove(index);
        storage.save(books);
    }

    public ArrayList<Book> search(String keyword) {
        ArrayList<Book> result = new ArrayList<>();
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                b.getAuthor().toLowerCase().contains(keyword.toLowerCase()))
                result.add(b);
        }
        return result;
    }

    public ArrayList<Book> getBooks() {
        return books;
    }
}


public class LibraryGUI extends JFrame {
    private JTextField titleField, authorField, searchField;
    private JList<String> bookList;
    private DefaultListModel<String> listModel;
    private LibraryManager manager;

    private final Color BG = new Color(245, 247, 250);
    private final Color BTN = new Color(33, 150, 243);
    private final Color BTN_DANGER = new Color(220, 53, 69);
    private final Font FONT = new Font("Segoe UI Emoji", Font.PLAIN, 16);

    public LibraryGUI() {
        manager = new LibraryManager(new FileStorage());

        setTitle(" Library Management System");
        setSize(700, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));
        getContentPane().setBackground(BG);

        JLabel header = new JLabel(
            "<html><div style='text-align: center;'>Library Management System.<br>"
            + "A book is not just ink on paper, it is a doorway to countless worlds and endless wisdom.</div></html>",
            JLabel.CENTER
        );
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(255, 51, 51));
        add(header, BorderLayout.NORTH);

        

JPanel form = new JPanel(new GridLayout(3,2,10,10));
form.setBackground(new Color(230, 240, 255));
form.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));

titleField = createField();
authorField = createField();
searchField = createField();

form.add(new JLabel("Enter The Book Title")); form.add(titleField);
form.add(new JLabel("<html>Please Give the Author Name</html>")); form.add(authorField);
form.add(new JLabel("<html>Search---> Please Enter Author Name <br> or Book title</html>")); form.add(searchField);


JPanel leftPanel = new JPanel(new BorderLayout());
leftPanel.setBackground(new Color(230, 240, 255));
leftPanel.add(form, BorderLayout.NORTH);
add(leftPanel, BorderLayout.WEST);

listModel = new DefaultListModel<>();
bookList = new JList<>(listModel);
bookList.setFont(FONT);
bookList.setSelectionBackground(new Color(51,255,255));
bookList.setBackground(new Color(245,245,245)); 

JScrollPane scrollPane = new JScrollPane(bookList);
scrollPane.getViewport().setBackground(new Color(245,245,245)); 

JPanel rightPanel = new JPanel(new BorderLayout());
rightPanel.setBackground(new Color(245,245,245)); 
rightPanel.add(scrollPane, BorderLayout.CENTER);
add(rightPanel, BorderLayout.CENTER);

        

        JPanel buttons = new JPanel(new GridLayout(1,5,10,10));
        buttons.setBackground(BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JButton addBtn = createButton("Add", BTN);
        JButton updateBtn = createButton("Update", BTN);
        JButton deleteBtn = createButton("Delete", BTN_DANGER);
        JButton clearBtn = createButton("Clear", Color.GRAY);
        JButton searchBtn = createButton("Search", new Color(40,167,69));

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);
        buttons.add(searchBtn);
        add(buttons, BorderLayout.SOUTH);

        refreshList(manager.getBooks());

        
        addBtn.addActionListener(e -> handleAdd());
        updateBtn.addActionListener(e -> handleUpdate());
        deleteBtn.addActionListener(e -> handleDelete());
        clearBtn.addActionListener(e -> clearFields());
        searchBtn.addActionListener(e -> handleSearch());
    }
    private JTextField createField() {
        JTextField f = new JTextField();
        f.setFont(FONT);
        f.setPreferredSize(new Dimension(180, 30));
        f.setBackground(new Color(230, 240, 255));
        f.setForeground(Color.BLACK);             
        f.setCaretColor(Color.BLUE);              
        return f;
    }

    private JButton createButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(FONT);
        b.setFocusPainted(false);
        return b;
    }


    private void handleAdd() {
        String t = titleField.getText();
        String a = authorField.getText();
        if (t.isEmpty() || a.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty"); return;
        }
        if (manager.isDuplicate(t, a)) {
            JOptionPane.showMessageDialog(this, "Duplicate book not allowed"); return;
        }
        manager.addBook(t, a);
        refreshList(manager.getBooks());
    }

    private void handleUpdate() {
        int i = bookList.getSelectedIndex();
        if (i != -1) {
            manager.updateBook(i, titleField.getText(), authorField.getText());
            refreshList(manager.getBooks());
        }
    }

    private void handleDelete() {
        int i = bookList.getSelectedIndex();
        if (i != -1) {
            manager.deleteBook(i);
            refreshList(manager.getBooks());
        }
    }

    private void handleSearch() {
        String k = searchField.getText();
        if (k.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter search keyword"); return;
        }
        new SearchResultWindow(manager.search(k), k);
    }

    private void clearFields() {
        titleField.setText(""); authorField.setText(""); searchField.setText("");
        refreshList(manager.getBooks());
    }

    private void refreshList(ArrayList<Book> books) {
        listModel.clear();
        for (Book b : books) listModel.addElement(b.display());
    }


    class SearchResultWindow extends JFrame {
        public SearchResultWindow(ArrayList<Book> results, String keyword) {
            setTitle("Search Result: " + keyword);
            setSize(400,300);
            setLocationRelativeTo(LibraryGUI.this);

            DefaultListModel<String> m = new DefaultListModel<>();
            JList<String> l = new JList<>(m);
            l.setFont(FONT);

            if (results.isEmpty()) m.addElement("No result found");
            else for (Book b : results) m.addElement(b.display());

            add(new JScrollPane(l));
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryGUI().setVisible(true));
    }
}
