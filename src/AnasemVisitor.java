import org.antlr.v4.runtime.tree.ParseTree;

public class AnasemVisitor extends AnasintBaseVisitor<Integer> {

    public Integer visitPrograma(Anasint.ProgramaContext ctx){
        visit(ctx.variables());
        return 0;
    }

    public Integer visitVariables(Anasint.VariablesContext ctx){
        System.out.println(ctx.decl_var(0).tipo_de_dato().tipo_elemental().LOG());
        return 0;
    }

    public Integer visitDecl_Var(Anasint.Decl_varContext ctx) {
        System.out.println(ctx.identificador_declaracion().getText());
        return 0;
    }
}