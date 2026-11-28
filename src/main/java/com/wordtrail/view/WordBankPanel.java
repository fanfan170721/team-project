package com.wordtrail.view;
import com.wordtrail.adapter.StudyController;
import com.wordtrail.adapter.PronunciationController;
import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.regex.Pattern;

/** Search the complete bundled curriculum without adding every word to private progress. */
final class WordBankPanel extends JPanel {
    WordBankPanel(StudyController study, PronunciationController pronunciation) {
        super(new BorderLayout(6, 6));
        setOpaque(false);
        DefaultTableModel rows=new DefaultTableModel(new String[]{"Word","Level","English definition"},0) {
            @Override public boolean isCellEditable(int row,int column) { return false; }
            @Override public Class<?> getColumnClass(int column) { return column==1?Integer.class:String.class; }
        };
        study.curriculum().forEach(rows::addRow);
        JTable table=new JTable(rows); table.setName("bank.table"); Ui.styleTable(table);
        table.getColumnModel().getColumn(1).setMaxWidth(65);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter=new TableRowSorter<>(rows); table.setRowSorter(sorter);
        JTextField search=Ui.field("bank.search",22);
        search.getAccessibleContext().setAccessibleName("Search all English curriculum words");
        JLabel count=Ui.muted(rows.getRowCount()+" English words · 15 learning levels",12);
        JPanel filters=new JPanel(new BorderLayout(8,0)); filters.setOpaque(false);
        filters.add(search,BorderLayout.CENTER); filters.add(count,BorderLayout.EAST);
        add(filters,BorderLayout.NORTH);
        JScrollPane scroll=Ui.scroll(table); scroll.setColumnHeaderView(table.getTableHeader()); add(scroll);
        JTextArea meaning=Ui.display("Select a word to read its definition or play its recorded pronunciation.");
        meaning.setName("bank.meaning"); meaning.setRows(2);
        PronunciationControls audio=new PronunciationControls(pronunciation);
        JPanel detail=new JPanel(new BorderLayout(0,4)); detail.setOpaque(false);
        detail.add(Ui.scroll(meaning)); detail.add(audio,BorderLayout.SOUTH); add(detail,BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener(event->{
            int selected=table.getSelectedRow();
            if(selected<0) { meaning.setText("Select a word."); audio.word(""); return; }
            int row=table.convertRowIndexToModel(selected);
            String term=(String)rows.getValueAt(row,0);
            meaning.setText(term+" · L"+rows.getValueAt(row,1)+"\n"+rows.getValueAt(row,2));
            meaning.setCaretPosition(0); audio.word(term);
        });
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)"+Pattern.quote(search.getText().strip()),0,2));
                count.setText(table.getRowCount()+" / "+rows.getRowCount()+" words");
            }
            public void insertUpdate(DocumentEvent event) { apply(); }
            public void removeUpdate(DocumentEvent event) { apply(); }
            public void changedUpdate(DocumentEvent event) { apply(); }
        });
    }
}
