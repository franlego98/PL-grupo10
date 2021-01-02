import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.swing.*;
import java.util.Arrays;

public class Principal {
    public static void main(String[] args) throws Exception{
        //Leer archivo
        CharStream input = CharStreams.fromFileName(args[0]);

        //Analizador lexico
        Analex analex = new Analex(input);
        CommonTokenStream tokens = new CommonTokenStream(analex);

        //Analizador sintactico
        Anasint anasint = new Anasint(tokens);

        //Mostrar arbol
        ParseTree tree = anasint.programa();
        JFrame frame = new JFrame("Árbol de Análisis");
        JPanel panel = new JPanel();
        TreeViewer viewr = new TreeViewer(Arrays.asList(anasint.getRuleNames()),tree);
        viewr.setScale(1);
        panel.add(viewr);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,400);
        frame.setVisible(true);

        //Analizador semantico
        AnasemVisitor anasem = new AnasemVisitor();
        anasem.visit(tree);

        //Interprete
        Interprete interprete = new Interprete();
        interprete.visit(tree);
    }
}
